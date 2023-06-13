package com.aircode.network.ts;

public class TsPacketParser {
    private static short _pmt_pid = 0x7FFF;              // default 값 0x7FFF는 미설정 상태.
    private static TsPacketCollector[] collector;

    public short getPmtPid() {
        return _pmt_pid;
    }
    public TsPacketParser() {
        _pmt_pid = 0x7FFF;
        collector = null;
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
            //System.out.println(" Not ready. : PMT PID unknown. waiting PAT..");
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
        if (payload[0] == 0x02) {       // PMT table 이면..
            PMT_parse pmt_parse = new PMT_parse(payload);
            int numPid = pmt_parse.get_number_of_PID();
            System.out.printf(">> PMT received. ( %d pids exist)\n", numPid );

            if (collector==null) {
//            System.out.printf(" Not ready. : DSMCC PID unknown. waiting PMT.. (PMT PID=%d (0x%04x))\n", _pmt_pid, _pmt_pid );
                collector = new TsPacketCollector[numPid];
                for (int i=0; i<numPid; i++) {
                    collector[i] = new TsPacketCollector(pmt_parse.get_ElementPid(i));
                    collector[i].setOnPayloadUnitCompleteListener(new PayloadUnitCompleteListener() {
                        @Override
                        public void onComplete(PayloadUnitComplete event, byte[] data, int section_length) {
                            System.out.printf("\n==== Section Collection Completed. (length=%d / %d) ==============", section_length, data.length );
                            for (int i=0; i<data.length; i++) {
                                if (i%30==0) {
                                    System.out.printf("\n\t");
                                }
                                System.out.printf("%02X ", data[i]);
                            }
                            System.out.printf("\n==== End of Section Completed.===============================" );
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

//        // TODO: 본격적으로 DSMCC 패킷을 수집해 본다.
//        System.out.printf("==== DSMCC packet dump [pid:0x%04X]========================\n", tsp.getPID() );
//        for (int i=0; i<payload.length; i++) {
//            System.out.printf(" %02X", payload[i]);
//        }
//        System.out.printf("\n==== end of DSMCC packet dump ===============\n");
//        DsmccAddressable_parse parse = new DsmccAddressable_parse(payload);

        if (collector != null) {
            for (int i=0; i<collector.length; i++) {
//            if ( collector[i]==null )     // 해당 PID가 DSMCC 가 아니다.
//                continue;
                if (tsp.getPID()==collector[i].getPID() ) {
//                if (parse.get_data_byte()!=null) {
//                    System.out.printf("DSMCC: payload append. %x\n", parse.get_data_byte() );
                    collector[i].append_packet( tsp );
//                }
                }
            }
        }
    }

}
