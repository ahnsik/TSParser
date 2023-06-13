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
//                        System.out.printf("________ multicast_received_dataQueue (%d bytes) ________________", data.length );
//                        for (int i=0; i<data.length; i++) {
//                            if (i%47==0)
//                                System.out.println("\t");
//                            System.out.printf(" %02x", data[i] );
//                        }
//                        System.out.printf("\n^^^^^^^^ Until here. multicast received. ^^^^^^^^^^^^^^^^^^^^^^^^\n" );

                        try {
                            // Multicast 로 부터 읽어 온 byteArray 를 InputStream 으로 만들고,
                            ByteArrayInputStream inS = new ByteArrayInputStream(data);
                            // 거기에서 부터 188byte 씩 읽어 온다.     // TODO: SyncByte(0x47) 를 체크하여, TS Packet 인지 확인하는 작업이 필요.
                            byte[] buff = new byte[188];
                            int readBytes = -1;
                            int remainBytes = 0;
                            do {
                                readBytes = inS.read(buff);
                                System.out.printf("\nbyte read : %d. (remained:%d).\n", readBytes, (remainBytes = inS.available()) );
                                // TsPacket 구조체로 분석한 후,
                                TsPacket newPacket = new TsPacket(buff);
                                //if (newPacket.getPID()==0x1FFF)
                                //    continue;       // null packet 은 PASS.
                                if ( (newPacket.getPID()==0x3E9) ||
                                     (newPacket.getPID()==0x3EA) ||
                                     (newPacket.getPID()==0x3EB) ||
                                     (newPacket.getPID()==0x3ED) ) {         // DSMCC 면 Dump 해 본다.
                                    if (parser.getPmtPid() != 0x7FFF) {      // PAT를 수신한 상태라면,
                                        System.out.println("TsPacket Dump:");
                                        for (int i=0; i<188; i++) {
                                            System.out.printf("%02X ", buff[i]);
                                        }
                                        System.out.println(".");
                                    }
                                }
                                // packet Parser 에 던져 준다.
                                parser.appendTsPacket(newPacket);
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
