package com.aircode.network.ts;

public class TsPacketParser {
    //    private static TsPacketCollector _PAT;
    private static short _pmt_pid = 0x7FFF;              // default 값 0x7FFF는 미설정 상태.
    private static TsPacketCollector _PMT;
    private static short _dsmcc_addressable_pid = 0x7FFF;   // default 값 0x7FFF는 미설정 상태.
    private static TsPacketCollector _DSMCC_addr;

    public TsPacketParser() {
//        _PAT = null;
        _pmt_pid = 0x7FFF;
        _PMT = null;
        _dsmcc_addressable_pid = 0x7FFF;
        _DSMCC_addr = null;
    }

    public static void set_pmt_pid(short pid) {
        _pmt_pid = pid;
    }

    public void appendTsPacket(TsPacket tsp) {
        System.out.printf(" <> _pmt_pid = %04X, _dsmcc_pid = %04x\n", _pmt_pid, _dsmcc_addressable_pid );
        if (_pmt_pid == 0x7FFF) {   // PMT_PID 를 모른다 == PAT 를 받은 적이 없다. --> PAT를 수신해야 함.
            System.out.println(" Not ready. : PMT PID unknwon");
            if (tsp.getPID() != 0x000) {    // PAT 가 아니므로 통과.
                System.out.printf(">>>>>>> PAT 가 아니다.%04x\n", _pmt_pid);
                return;
            } else {                        // PAT를 수신했다면, parsing 하고 pmt_pid 를 설정할 것.
                PAT_parse _pat = new PAT_parse(tsp.getPayload());
                _pmt_pid = _pat.get_PMT_PID(0);    // 첫번째 program 의 PID 를 그냥 가져와서 사용.
                System.out.printf(">>  I GOT PAT : PMT PID is %d(0x%04x)\n", _pmt_pid, _pmt_pid);
            }
        } else {
            System.out.printf("_pmt_pid = %x\n", _pmt_pid );
        }
        System.out.printf("[][] TRACE [][]\n");

        byte[] payload = tsp.getPayload();

        if (_dsmcc_addressable_pid == 0x7FFF) {     // DSMCC_addressable 의 PID 를 아직 모른다. ==> PMT_PID 를 수신하지 못했다.
            System.out.println(" Not ready. : DSMCC PID unknown.");
            if (payload[0] == 0x02) {       // PMT table 이면..
                PMT_parse pmt_parse = new PMT_parse(payload);
                _dsmcc_addressable_pid = pmt_parse.get_dmscc_addressable_PID();      //getPidWithTableId(0x3F);
                System.out.printf(">>  I GOT PMT : DSMCC PID is %d(0x%04x)\n", _dsmcc_addressable_pid, _dsmcc_addressable_pid);
            } else {
                System.out.printf(">>  this is NOT PMT table\n" );
            }
            return;
        }

        if (tsp.getPID() != _dsmcc_addressable_pid) {
            System.out.printf(" Not a DSMCC packet.(pid=%d(0x%04X))\n", tsp.getPID(), tsp.getPID() );
            return;
        }

        // TODO: 본격적으로 DSMCC 패킷을 수집해 본다.
        System.out.printf("==== DSMCC parser ========================");
        for (int i=0; i<payload.length; i++) {
            System.out.printf("%02X ", payload[i]);
        }
        System.out.printf("\n==== end of DSMCC parser ===============");
    }

}
