import com.aircode.network.ts.TsPacket;
import com.aircode.network.ts.TsPacketParser;
import com.aircode.network.udp.PacketReceiver;
import com.aircode.network.udp.intf.UdpPacketHandler;
import com.aircode.util.LogManager;
import com.aircode.util.NetworkUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class App {

    private static TsPacketParser parser;

    public static void main(String[] args) throws Exception {

        Logger logger = LogManager.getInstance().getLogger(App.class);

        ConcurrentLinkedQueue<byte[]> dataQueue = new ConcurrentLinkedQueue<byte[]>();
        boolean isRunningStatus = true;        
        UdpPacketHandler uph = new UdpPacketHandler() {
            public void onReceived(byte[] data) {
                synchronized(dataQueue){
                    dataQueue.offer(data);
                    dataQueue.notify();
                }
            }
            public void onReceiveFinished() {}
            public void onExceptionOccurred(Throwable e) {}
            public void onRetryRequest(int retryCount) {}
            public void run () {

                parser = new TsPacketParser();

                while(isRunningStatus){
                    synchronized(dataQueue){
                        //Try loading data from dataQueue
                        byte[] data = dataQueue.poll();
                        if(data == null){
                            try {
                                //blocked until dataQueue.notify() called
                                dataQueue.wait();
                            } catch (InterruptedException e) {
                            }
                            continue;
                        }

                        try {
                            // Multicast 로 부터 읽어 온 byteArray 를 InputStream 으로 만들고,
                            ByteArrayInputStream inS = new ByteArrayInputStream(data);
                            // 거기에서 부터 188byte 씩 읽어 온다.
                            byte[] buff = new byte[188];
                            int readBytes = -1;
                            int remainBytes = 0;
                            do {
                                readBytes = inS.read(buff);
                                System.out.printf("\nbyte read : %d. (remained:%d).\n", readBytes, (remainBytes = inS.available()) );
                                if ( (buff[0]==71)&&(buff[1]==31)&&(buff[2]==-1)&&(buff[3]==16)&&(buff[4]==-1)&&(buff[5]==-1) ) {
                                    continue;
                                } else {
                                    for (int i=0; i<188; i++) {
                                        System.out.printf("%02X ", buff[i]);
                                    }
                                    TsPacket newPacket = new TsPacket(buff);
                                    parser.appendTsPacket(newPacket);
                                }
                            } while (remainBytes >= 188);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
        };
        // -- DVBSTP packet parse
        // Thread rt = new Thread(new PacketReceiver("239.1.1.1", 15210, null, NetworkUtils.getNetworkInterfaceByIp(), uph), "PacketReceiverThread");
        // -- MPEG-TS 수신
        Thread rt = new Thread(new PacketReceiver("239.10.10.10", 14200, null, NetworkUtils.getNetworkInterfaceByIp(), uph), "PacketReceiverThread");
        Thread ht = new Thread(uph, "PacketHandlerThread");
        rt.start();
        ht.start();
    }

}
