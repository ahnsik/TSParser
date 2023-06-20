import com.aircode.network.ts.TsPacket;
import com.aircode.network.ts.TsPacketParser;
import com.aircode.network.udp.PacketReceiver;
import com.aircode.network.udp.intf.UdpPacketHandler;
import com.aircode.network.udp.packet.standard.DvbStpListener;
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
                parser.setDocumentReceivedListener ( new DvbStpListener() {
                    @Override
                    public void onServiceProviderDiscoveryReceived(byte[] received_data) {
                        String xmlFilename = "ServiceProviderDiscovery.xml";
                        File xmlFile = new File( "./" + xmlFilename ) ;
                        System.out.println("write to (XML FileName):" + xmlFilename );
                        try (FileOutputStream outputStream = new FileOutputStream(xmlFile)) {
                            outputStream.write( received_data );
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLinearTVDiscoveryReceived(byte[] received_data) {
                        String xmlFilename = "LinearTVDiscovery.xml";
                        File xmlFile = new File( "./" + xmlFilename ) ;
                        System.out.println("write to (XML FileName):" + xmlFilename );
                        try (FileOutputStream outputStream = new FileOutputStream(xmlFile)) {
                            outputStream.write( received_data );
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onContentGuideDiscoveryReceived(byte[] received_data) {
                        String xmlFilename = "ContentGuideDiscovery.xml";
                        File xmlFile = new File( "./" + xmlFilename ) ;
                        System.out.println("write to (XML FileName):" + xmlFilename );
                        try (FileOutputStream outputStream = new FileOutputStream(xmlFile)) {
                            outputStream.write( received_data );
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPackageDiscoveryReceived(byte[] received_data) {
                        String xmlFilename = "PackageDiscovery.xml";
                        File xmlFile = new File( "./" + xmlFilename ) ;
                        System.out.println("write to (XML FileName):" + xmlFilename );
                        try (FileOutputStream outputStream = new FileOutputStream(xmlFile)) {
                            outputStream.write( received_data );
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onScheduleIndexDiscoveryReceived(byte[] received_data) {
                        String xmlFilename = "ScheduleIndexDiscovery.xml";
                        File xmlFile = new File( "./" + xmlFilename ) ;
                        System.out.println("write to (XML FileName):" + xmlFilename );
                        try (FileOutputStream outputStream = new FileOutputStream(xmlFile)) {
                            outputStream.write( received_data );
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onScheduleDiscoveryReceived(byte[] received_data) {
                        String xmlFilename = "ScheduleDiscovery.xml";
                        File xmlFile = new File( "./" + xmlFilename ) ;
                        System.out.println("write to (XML FileName):" + xmlFilename );
                        try (FileOutputStream outputStream = new FileOutputStream(xmlFile)) {
                            outputStream.write( received_data );
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSystemTimeDiscoveryReceived(byte[] received_data) {
                        String xmlFilename = "SystemTimeDiscovery.xml";
                        File xmlFile = new File( "./" + xmlFilename ) ;
                        System.out.println("write to (XML FileName):" + xmlFilename );
                        try (FileOutputStream outputStream = new FileOutputStream(xmlFile)) {
                            outputStream.write( received_data );
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } );


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
                                remainBytes = inS.available();
//                                System.out.printf("\nbyte read : %d. (remained:%d).\n", readBytes, remainBytes );
                                // TsPacket 구조체로 분석한 후,
                                TsPacket newPacket = new TsPacket(buff);

//                                if (newPacket.getPID()!=0x1FFF) {
////                                if ( (newPacket.getPID() == 0x03EA ) ) {            // && newPacket.getPUSI() ) {        // Linear 만 뽑아 본다. && PUSI가 1 인 것 만.
//                                    System.out.printf("TS:\t");
//                                    for (int i=0; i<188; i++) {             // for (int i=0; i<4; i++) {    //
//                                        System.out.printf("%02X ", buff[i]);
//                                    }
//                                    System.out.println(".");
//                                }

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
        ////// -- DVBSTP packet parse
        // Thread rt = new Thread(new PacketReceiver("239.1.1.1", 15210, null, NetworkUtils.getNetworkInterfaceByIp(), uph), "PacketReceiverThread");
        // rt.start();
        // ht.start();
        ////// -- MPEG-TS 수신
        Thread rt_sdt = new Thread(new PacketReceiver("239.10.10.10", 14200, null, NetworkUtils.getNetworkInterfaceByIp(), uph), "PacketReceiverThread_SDT");
        Thread ht_sdt = new Thread(uph, "PacketHandlerThread_SDT");
        rt_sdt.start();
        ht_sdt.start();
        Thread rt_cg = new Thread(new PacketReceiver("239.10.10.11", 14200, null, NetworkUtils.getNetworkInterfaceByIp(), uph), "PacketReceiverThread_CG");
        Thread ht_cg = new Thread(uph, "PacketHandlerThread_CG");
        rt_cg.start();
        ht_cg.start();
    }

}
