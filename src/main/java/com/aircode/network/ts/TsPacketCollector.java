package com.aircode.network.ts;

import java.util.Arrays;

public class TsPacketCollector implements PayloadUnitCompleteListener {
    private short _PID;
    private byte _continuity_counter;
    private int _section_len;

    public byte[] getCompletedPayload() {
        return _completedPayload;
    }

    private byte[] _completedPayload;
    private byte[] _payloadUnit;
    private PayloadUnitCompleteListener completelistener;

    public TsPacketCollector(short PID) {
        _PID = PID;
        _continuity_counter = -1;
        _section_len = 0;
        _payloadUnit = null;     //new byte[];
        _completedPayload = null;     //new byte[];
    }

    public short getPID() {
        return _PID;
    }

    public boolean append_packet(TsPacket packet) {
        if ( packet.hasError() ) {
            System.out.printf("\n[][][][] PID=0x%04X: packet.hasError() [][][][]\n", _PID );
            return false;
        }
        if ( packet.getPID() != _PID ) {
            System.out.printf("\n[][][][] packet.getPID(=0x%04X) is not PID=0x%04X [][][][]\n", packet.getPID(), _PID );
            return false;
        }
        if ( packet.getContinuityCounter() != ((_continuity_counter+1)&0x0F) ) {
            System.out.printf("\n[][][][] PID=0x%04X: continuity counter !! (new=%d, prev+1=%d) [][][][]\n\n", _PID, packet.getContinuityCounter(), (_continuity_counter+1)&0x0F );
            this.onInvalidContinuity(new PayloadUnitComplete(this));
            _payloadUnit = null;        // 모으던 payload 들을 몽땅 없애 버리고 처음부터 새로 받아 오도록 한다.
            _section_len = 0;
        }
        _continuity_counter = packet.getContinuityCounter();

        if (packet.getPUSI()) {
            byte[] packetBuff = packet.getPayload();
            if (packetBuff != null) {
                if (_payloadUnit != null) {
                    byte[] joinedArray;
                    System.out.printf("[#1] Array Join. _payloadUnit(%d bytes, PID=0x%04X) plus packetBuff(%d bytes, PID=0x%04X) = total:%d bytes.\n", _payloadUnit.length, _PID, packetBuff.length, packet.getPID(), _payloadUnit.length + packetBuff.length);
                    joinedArray = Arrays.copyOf(_payloadUnit, _payloadUnit.length + packetBuff.length);
                    System.arraycopy(packetBuff, 0, joinedArray, _payloadUnit.length, packetBuff.length);
                    _payloadUnit = joinedArray;
                } else {
                    _section_len = (((packetBuff[1]&0xFF)<<8)|(packetBuff[2]&0xFF))&0x0FFF;
                    _payloadUnit = packetBuff;
                }

                if (_payloadUnit.length >= (_section_len+3) ) {
                    _completedPayload = _payloadUnit;       // 완성 버퍼로 옮겨 놓고,
//                    _completedPayload = Arrays.copyOf(_payloadUnit, _payloadUnit.length);                   // 완성 버퍼로 옮겨 놓고,
                    this.onComplete(new PayloadUnitComplete(this), _completedPayload, _section_len, _PID);    // 이벤트리스너 호출
                    // TODO: 1 개의 TS packet 에 여러개의 PayloadUnit 이 있는 경우에 대하여 고려하여  수정할 것.
//                    if ( packet.getNumPayloadUnit() > 1 ) {
////                    _payloadUnit = packet.getNewSectionPayload();
//                    } else {
                        _payloadUnit = null;
//                    }

                }
            }
        } else {        // there are No newSection_payload()
            if (_payloadUnit==null) {
//                System.out.println("new section 이 아닌데,  수집 중인 packet 이 아직 없다. - 무시할 것.");
                return false;
            }
            byte[] packetBuff = packet.getPayload();
//            System.out.printf("[#2] Array Join. _payloadUnit(%d bytes, PID=0x%04X) plus packetBuff(%d bytes, PID=0x%04X) = total:%d bytes.\n", _payloadUnit.length, _PID, packetBuff.length, packet.getPID(), _payloadUnit.length + packetBuff.length);
            byte[] joinedArray = Arrays.copyOf(_payloadUnit, _payloadUnit.length + packetBuff.length);
            System.arraycopy(packetBuff, 0, joinedArray, _payloadUnit.length, packetBuff.length);
            _payloadUnit = joinedArray;

            if (_payloadUnit.length >= (_section_len+3) ) {
                _completedPayload = _payloadUnit;       // 완성 버퍼로 옮겨 놓고,
                this.onComplete(new PayloadUnitComplete(this), _completedPayload, _section_len, _PID );    // 이벤트리스너 호출

                _payloadUnit = packet.getNewSectionPayload();
//                System.out.printf("\n[][] TRACE .. #4 [][] completed.length=%d\n", _completedPayload.length );
//                if (_payloadUnit != null)
//                    System.out.printf("\n[][] TRACE .. #5 new _payloadUnit.length=%d [][] \n", _payloadUnit.length );
            }

        }
        return true;
    }

    public void setOnPayloadUnitCompleteListener(PayloadUnitCompleteListener listener) {
        completelistener = listener;
    }
    @Override
    public void onComplete(PayloadUnitComplete event, byte[] data, int section_length, int pid) {
//        System.out.println("**** CEHCK WIERD 0xFF(s).. **** !! Payload Unit is completed. : ");
//                System.out.printf("[][] Payload Unit is completed.  PID=0x%04X (section_length= %d bytes), Payload Unit is completed.\n", _PID, _section_len );
//                for (int i=0; i<_completedPayload.length; i++)
//                    System.out.printf("%02X ", _completedPayload[i] );
//                System.out.printf("\n=========== end of Payload Unit is completed.(completed) =========================\n" );

        completelistener.onComplete(event, data, section_length, pid);
    }

    @Override
    public void onInvalidContinuity(PayloadUnitComplete event) {
        completelistener.onInvalidContinuity(event);
    }

//    public void dump_buffer (byte[] buf) {
//        System.out.printf("==== Section Completed. (%04x)================================\n", _PID );
//        for (int i=0; i<buf.length; i++) {
//            System.out.printf(" %02x", buf[i] );
//            if (i%30==0) {
//                System.out.printf("\n\t");
//            }
//        }
//        System.out.printf("==== Section Completed. (length=%d)================================", buf.length );
//    }
}
