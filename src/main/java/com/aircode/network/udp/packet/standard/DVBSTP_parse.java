package com.aircode.network.udp.packet.standard;

import com.aircode.network.udp.packet.standard.packet.DvbStp;
import com.aircode.network.udp.packet.standard.payload.CRI_container;
import com.aircode.network.udp.packet.standard.payload.Collector;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class DVBSTP_parse  implements DvbStpListener{
    private static ArrayList<Collector> ServiceProviderDiscovery;       // payload_id = 0x01
    private static ArrayList<Collector>    LinearTVDiscovery;       // payload_id = 0x02
    private static ArrayList<Collector>    ContentGuideDiscovery;      // payload_id = 0x03
    private static ArrayList<Collector>    PackageDiscovery;               // payload_id = 0x05
    private static ArrayList<Collector>    ScheduleIndexDiscovery;               // payload_id = 0xA4
    private static ArrayList<Collector>  ScheduleDiscovery;       // payload_id = 0xA3
    private static ArrayList<Collector>    SystemTimeDiscovery;       // payload_id = 0xF0

    private static DvbStpListener  documentListener;

    public DVBSTP_parse() {
        ServiceProviderDiscovery = new ArrayList<Collector>();
        LinearTVDiscovery = new ArrayList<Collector>();
        ContentGuideDiscovery = new ArrayList<Collector>();
        PackageDiscovery = new ArrayList<Collector>();
        ScheduleIndexDiscovery = new ArrayList<Collector>();
        ScheduleDiscovery = new ArrayList<Collector>();
        SystemTimeDiscovery = new ArrayList<Collector>();
        documentListener = null;
    }

    public void append_data(DvbStp data) {
        switch(data.getPayloadId()) {
            case 0x01:
//                System.out.printf("[][] -> DVBSTP Service Provider\n");
                multiSegmentPacketCollect(ServiceProviderDiscovery, data);
                break;
            case 0x02:
//                System.out.printf("[][] -> DVBSTP Service Linear\n");
                multiSegmentPacketCollect(LinearTVDiscovery, data);
                break;
            case 0x03:
//                System.out.printf("[][] -> DVBSTP Service Contents Guide\n");
                multiSegmentPacketCollect(ContentGuideDiscovery, data);
                break;
            case 0x05:
//                System.out.printf("[][] -> DVBSTP Service Package\n");
                multiSegmentPacketCollect(PackageDiscovery, data);
                break;
            case (byte) 0xA3:
//                System.out.printf("[][] -> DVBSTP Schedule\n");
                multiSegmentPacketCollect(ScheduleDiscovery, data);
                break;
            case (byte) 0xA4:
//                System.out.printf("[][] -> DVBSTP Schedule Index\n");
                multiSegmentPacketCollect(ScheduleIndexDiscovery, data);

                break;
            case (byte) 0xF0:
//                System.out.printf("[][] -> DVBSTP SystemTime\n");
                multiSegmentPacketCollect(SystemTimeDiscovery, data);
                break;
            default:
//                logger.info("Unknown Payload ID " + data.getPayloadId()  );
                System.out.println("Unknown Payload ID " + data.getPayloadId()  );
                break;

        }
    }

    public  void multiSegmentPacketCollect(ArrayList<Collector> collector, DvbStp packet_data ) {
        if (collector == null) {
            System.out.println("[][] TRACE #. [][]  collector is null. making new Collector.");
            collector = new ArrayList<Collector>();
        }
        int i, numOfCollector = collector.size();
        Collector lookup_segment;
        for (i=0; i<numOfCollector; i++) {
            lookup_segment = collector.get(i);
            if (lookup_segment.get_segmentId()==packet_data.getSegmentId() ) {
                break;
            }
        }
        if ( i >= numOfCollector) {
            // 못 찾았다. 새로 생성
            lookup_segment = new Collector(packet_data.getPayloadId());
            Collector finalLookup_segment = lookup_segment;
            lookup_segment.addCompletedEventListener(new DocumentCompletedListener() {
                @Override
                public void onComplete(DocumentCompleted event) {
                    // TODO: DVBSTP packet 을 모두 모아서 Segment 가 완성되었을 때 호출되는 함수. - 수신한 segment 를 저장하거나, parsing 하는 동작을 수행 ?
//                    System.out.println("onComplete listener... "  );
                    do_payload_parse(finalLookup_segment);
                }
                @Override
                public void onInvalidVersion(DocumentCompleted event) {
                    // TODO: DVBSTP packet 을 수신 중에 segment version이 변경되는 경우에 호출되는 함수. - 몽땅 제거하고 다시 수신할 수 있도록 초기화됨
                    System.out.println("Invalid Segment Version: " + event.get_segmentVersion() );
                }
            });
            collector.add(lookup_segment);
        } else {    // 찾았으면 그걸 사용.
            lookup_segment = collector.get(i);
        }

        if (lookup_segment.isEmpty() ) {
            lookup_segment.setPropertiesFrom(packet_data);
        }
        lookup_segment.append(packet_data);

    }

    public void do_payload_parse(Collector segment) {
//        System.out.printf(" do_payload_parse() payload_id= 0x%02X\n", segment.get_payloadId() );
        try {
            switch(segment.get_payloadId()) {
                case (byte)0xA3:      // 만약, payload_id 가 0xA3 (Schedule) 이라면, CRI_container parsing 해야 함.
                    onScheduleDiscoveryReceived( decompress(segment.getPayload()) );
                    break;
                case (byte)0xA4:      // 아직 index Container 에 대해서는 분석이 필요하므로, 그냥 binary 로 저장.
                    onScheduleIndexDiscoveryReceived( decompress(segment.getPayload()) );
                    break;
                case (byte)0x01:
                    onServiceProviderDiscoveryReceived( decompress(segment.getPayload()) );
                    break;
                case (byte) 0x02:
                    onLinearTVDiscoveryReceived( decompress(segment.getPayload()) );
                    break;
                case (byte) 0x03:
                    onContentGuideDiscoveryReceived( decompress(segment.getPayload()) );
                    break;
                case (byte) 0x05:
                    onPackageDiscoveryReceived( decompress(segment.getPayload()) );
                    break;
                case (byte) 0xF0:
                    onSystemTimeDiscoveryReceived( decompress(segment.getPayload()) );
                    break;

                default:        // 그외의 경우엔 모두 GZ압축 풀어서 XML파일로 저장.
                    System.out.printf("\n[][WARNING][] UNKNOWN DvbStp Payload_ID. _payload_id=0x%X, segment_id=0x%X \n", segment.get_payloadId(), segment.get_segmentId());
//                    String xmlFilename = "pkt"+segment.get_payloadId()+"_Segment"+segment.get_segmentId() + ".xml";
//                    File xmlFile = new File( "./" + xmlFilename ) ;
//                    System.out.println("write to (XML FileName):" + xmlFilename );
//                    try (FileOutputStream outputStream = new FileOutputStream(xmlFile)) {
//                        outputStream.write( decompress(segment.getPayload()) );
//                    } catch (Exception e){
//                        e.printStackTrace();
//                    }
                    break;
            }
        } catch (IOException e) {
            System.out.println("[][] EXCEPTION when parse 0xA3.");
            byte[] dump = segment.getPayload();
            for (int j=0; j<100; j++) {
                System.out.printf("%02X,", dump[j] );
            }
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }


    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    public static byte[] decompress(final byte[] compressed) throws IOException {
        if ((compressed == null) || (compressed.length == 0)) {
            return null;
        }

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outStr = new ByteArrayOutputStream( );
        if (isCompressed(compressed)) {
            final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            int read_len = 0;
            do {
                read_len = gis.read(buffer);
                if (read_len<=0)
                    break;
                outStr.write(buffer, 0, read_len);
            } while( read_len > 0);
        } else {
            outStr.write(compressed);
        }
        return outStr.toByteArray();
    }


    public void setDocumentReceivedListener(DvbStpListener listener) {
        documentListener = listener;
    }
    @Override
    public void onServiceProviderDiscoveryReceived(byte[] received_data) {
        if (documentListener!=null)
            documentListener.onServiceProviderDiscoveryReceived(received_data);
    }

    @Override
    public void onLinearTVDiscoveryReceived(byte[] received_data) {
        if (documentListener!=null)
            documentListener.onLinearTVDiscoveryReceived(received_data);
    }

    @Override
    public void onContentGuideDiscoveryReceived(byte[] received_data) {
        if (documentListener!=null)
            documentListener.onContentGuideDiscoveryReceived(received_data);
    }

    @Override
    public void onPackageDiscoveryReceived(byte[] received_data) {
        if (documentListener!=null)
            documentListener.onPackageDiscoveryReceived(received_data);
    }

    @Override
    public void onScheduleIndexDiscoveryReceived(byte[] received_data) {
        System.out.println("Payload ID 0xA4 is not descripted yet... Sorry." );
        //  분석을 위해 segment 파일로 저장해 둔다. - 이미 저장해 두었으므로 제거할 것.
        if (documentListener!=null)
            documentListener.onScheduleIndexDiscoveryReceived(received_data);
    }

    @Override
    public void onScheduleDiscoveryReceived(byte[] received_data) {
        System.out.println("CRI_comtainer write.. 0xA3: container_parsing.." );
        CRI_container parser = new CRI_container( received_data );
        if (documentListener!=null)
            documentListener.onScheduleDiscoveryReceived( parser.getResult_string() );
    }

    @Override
    public void onSystemTimeDiscoveryReceived(byte[] received_data) {
        if (documentListener!=null)
            documentListener.onSystemTimeDiscoveryReceived(received_data);
    }

}
