package com.aircode.network.udp;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import org.slf4j.Logger;

import com.aircode.network.udp.intf.UdpPacketHandler;
import com.aircode.util.LogManager;

public class PacketReceiver implements Runnable{

    private Logger logger;
    public static final int CONTROL_CHANNEL = 0;
    public static final int DATA_CHANNEL = 1;

    protected boolean isRunningStatus = true;

    protected MulticastSocket socket = null;
    protected NetworkInterface netInt = null;
    protected int socketReceiveBuffer = 30000;

    protected int joinDelay = 1000;
    protected int retryCount = UdpConstants.INTERVAL_JOIN_DEFAULT;

    protected String      dstIp = null;
    protected Integer     dPort = null;
    protected String      srcIp = null;
    protected UdpPacketHandler udpPacketHandler = null;

    protected int type = -1;

    public PacketReceiver(String dstIp, Integer dPort, String srcIp, NetworkInterface netInt, UdpPacketHandler udpPacketHandler) throws SocketException {
        this.dstIp = dstIp;
        this.dPort = dPort;
        this.srcIp = srcIp;
        this.udpPacketHandler = udpPacketHandler;
        this.netInt = netInt;
        this.logger = LogManager.getInstance().getLogger(this.getClass());
    }

    public int getType(){
        return type;
    }
    public void stop(){
        isRunningStatus = false;
    }

    public void setSocketReceiveBuffer(int socketReceiveBuffer) {
        this.socketReceiveBuffer = socketReceiveBuffer;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;        
        this.logger.debug(getTypeString() + " RetryCount: " + retryCount);        
    }

    public void setJoinDelay(int delay){
        this.joinDelay = delay;
    }

    @Override
    public void run() {
        byte[] receiveBuf = new byte[UdpConstants.SIZE_MTU];
        DatagramPacket packet = new DatagramPacket(receiveBuf, receiveBuf.length);
        int retry = 0;

        try {
            socket = new MulticastSocket(dPort);
            socket.setReuseAddress(true);
            socket.setSoTimeout(5000);
            socket.setReceiveBufferSize(socketReceiveBuffer);
        } catch (IOException e) {
            this.logger.error(e.getMessage(), e);            
        }

        try {
            this.logger.debug("joinDelay: " + joinDelay);            
            Thread.sleep(joinDelay);
        } catch (InterruptedException e) {
            this.logger.error(e.getMessage(), e);
        }

        SocketAddress sockAddr = null;
        try {
            sockAddr = new InetSocketAddress(InetAddress.getByName(dstIp), dPort);
            socket.joinGroup(sockAddr, netInt);
            this.logger.debug("Multicast Join! addr=" + dstIp + ", port=" + dPort + ", bufferSize=" + socket.getReceiveBufferSize());            
            this.logger.info(getTypeString() + " Multicast Join!");
        } catch (IOException e) {
            this.logger.error(e.getMessage(), e);
            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
            isRunningStatus = false;
            retry = retryCount;
            udpPacketHandler.onExceptionOccurred(e);
        }

        while(isRunningStatus){
            try {
                socket.receive(packet);
                //source ip filtering
                if(srcIp == null || srcIp.equals("") || srcIp.equals(packet.getAddress().getHostAddress())){
                    byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
                    if (udpPacketHandler != null) {
                        udpPacketHandler.onReceived(data);
                    }
                }
                retry = 0;
                retryCount = UdpConstants.INTERVAL_JOIN_DEFAULT;
            } catch(IOException e){
                logger.error(e.getMessage(), e);
                retry++;

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                retry++;
                udpPacketHandler.onReceiveFailed(e);
            }

            if(retry >= retryCount){
                isRunningStatus = false;
            }

        } // End of while(isRunningStatus)

        if(retry >= retryCount){
            udpPacketHandler.onRetryRequest(retryCount);
        }else{            
            udpPacketHandler.onReceiveFinished();
        }
    }

    public void setRunningStatus(boolean runningStatus) {
        isRunningStatus = runningStatus;
    }

    public boolean isRunningStatus() {
        return isRunningStatus;
    }

    public void setNetInt(NetworkInterface netInt) {
        this.netInt = netInt;
    }

    private String getTypeString(){
        String typeString = "Unknown";
        if(type==CONTROL_CHANNEL){
            typeString = "Control Channel";
        }else if(type==DATA_CHANNEL){
            typeString = "Data Channel";
        }
        return typeString;
    }

}
