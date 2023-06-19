package com.aircode.network.ts;

import com.aircode.network.udp.packet.standard.DVBSTP_parse;
import com.aircode.network.udp.packet.standard.DvbStpListener;
import com.aircode.network.udp.packet.standard.packet.DvbStp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

public class TsPacketParser {

    private DVBSTP_parse dvbstp_Parser;

    public void setDocumentReceivedListener(DvbStpListener custom_Listener) {
        this.custom_DvbStpListener = custom_Listener;
        dvbstp_Parser.setDocumentReceivedListener( this.custom_DvbStpListener );
    }

    private DvbStpListener custom_DvbStpListener;


        private static short _pmt_pid = 0x7FFF;              // default 값 0x7FFF는 미설정 상태.
    private static TsPacketCollector[] collector;

    public short getPmtPid() {
        return _pmt_pid;
    }
    public TsPacketParser() {
        _pmt_pid = 0x7FFF;
        collector = null;
        dvbstp_Parser = new DVBSTP_parse();
        custom_DvbStpListener = new DvbStpListener() {
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
        };
        dvbstp_Parser.setDocumentReceivedListener( custom_DvbStpListener );
    }

    /** to Fast Collection :
        PMT PID 를 알고 있는 경우엔, PAT 를 찾아서 설정하지 않고, 직접 PID 값을 설정해서 곧바로 진행하도록 한다.
        parameter:
            pid     PMT_PID
     */
    public static void set_pmt_pid(short pid) {
        _pmt_pid = pid;
    }

    public void appendTsPacket(TsPacket tsp) {
        if (_pmt_pid == 0x7FFF) {           // PMT_PID 를 모른다 == PAT 를 받은 적이 없다. --> PAT를 수신해야 함.
//                    System.out.printf("[][] DEBUG [][] PMT not received yet. %x\n", _pmt_pid );
            if (tsp.getPID() != 0x000) {    // PAT 가 아니므로 통과.
                return;
            } else {                        // PAT를 수신했다면, parsing 하고 pmt_pid 를 설정할 것.
                PAT_parse _pat = new PAT_parse(tsp.getPayload());
                _pmt_pid = _pat.get_PMT_PID(0);    // 첫번째 program 의 PID 를 그냥 가져와서 사용.
                System.out.printf(">>  I GOT PAT : PMT PID is %d (0x%04x)\n", _pmt_pid, _pmt_pid);
            }
//        } else {
//            System.out.printf("_pmt_pid = %x\n", _pmt_pid );
        }

        byte[] payload = tsp.getPayload();
        // PMT를 수신 해서
        if ( tsp.isPMT() ) {       // if (payload[0] == 0x02) {       // PMT table 이면..           **** PMT 파싱하는 부분에 버그가 있는 듯 하다.
            PMT_parse pmt_parse = new PMT_parse(payload);
            int numPid = pmt_parse.get_number_of_PID();
//            System.out.printf(">> PMT received. ( %d pids exist)\n", numPid );

            if (collector==null) {
//            System.out.printf(" Not ready. : DSMCC PID unknown. waiting PMT.. (PMT PID=%d (0x%04x))\n", _pmt_pid, _pmt_pid );
                collector = new TsPacketCollector[numPid];
                for (int i=0; i<numPid; i++) {
                    collector[i] = new TsPacketCollector(pmt_parse.get_ElementPid(i));
                    collector[i].setOnPayloadUnitCompleteListener(new PayloadUnitCompleteListener() {
                        @Override
                        public void onComplete(PayloadUnitComplete event, byte[] data, int section_length, int received_PID) {
                            DsmccAddressable_parse dsmcc = new DsmccAddressable_parse(data);
                            byte[] dvbstp_packet = dsmcc.get_data_byte();
//                            System.out.printf("\n==== Dvbstp bytes (%d bytes) ===============================\n", dvbstp_packet.length );
//                            for (int i=0; i<dvbstp_packet.length; i++) {
//                                System.out.printf("%02X ", dvbstp_packet[i]);
//                            }
//                            System.out.printf("\n==== end of dvbstp. ===============================\n" );

                            DvbStp parsing_data = new DvbStp( dvbstp_packet );
//                            System.out.printf("=>=>=> dvbstp packet: segment_version=%d, section_num=(%d of %d) ===============================\n",  parsing_data.getSegmentVersion(), parsing_data.getSectionNumber() ,parsing_data.getLastSectionNumber() );
                            dvbstp_Parser.append_data(parsing_data);
                        }
                        @Override
                        public void onInvalidContinuity(PayloadUnitComplete event) {
                            System.out.printf("==== Section InvalidContinuity. ========================\n" );
                        }
                    });
                }
            } else {
                if (collector.length == 0) {
                    System.out.printf("[WARNING][WARNING] collector.length is zero !! initializer weird.\n" );
                }
            }
            return;
//        } else {        // PMT table 이면 아래 루틴을 계속 진행.
//            System.out.printf(">>  this is NOT PMT table\n" );
        }

        if (collector != null) {
            for (int i=0; i<collector.length; i++) {
                if (tsp.getPID()==collector[i].getPID() ) {
                    collector[i].append_packet( tsp );
                }
            }
        }

    }

}
