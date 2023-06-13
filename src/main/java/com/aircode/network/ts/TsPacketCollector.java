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
        _continuity_counter = 0;
        _section_len = 0;
        _payloadUnit = null;     //new byte[];
        _completedPayload = null;     //new byte[];
    }

    public short getPID() {
        return _PID;
    }

    public boolean append_packet(TsPacket packet) {
        System.out.println("[] TRACE [] Section Collecting..");
        if ( packet.hasError() )
            return false;
        if ( packet.getPID() != _PID )
            return false;
        if ( (packet.getContinuityCounter()+1)%0x0F == _continuity_counter)
            this.onInvalidContinuity(new PayloadUnitComplete(this) );
        if (packet.getPUSI()) {
            System.out.printf("[][] TRACE .. #2 - pusi is set. [][]\n" );
            byte[] packetBuff = packet.getPayload();
            if (packetBuff != null) {
                System.out.printf("[][] TRACE .. #3 [][] \n" );
                if (_payloadUnit != null) {
                    byte[] joinedArray;
                    System.out.printf("\npayloadUnit.length=%d, packetBuff.length=%d\n", _payloadUnit.length , packetBuff.length);
                    joinedArray = Arrays.copyOf(_payloadUnit, _payloadUnit.length + packetBuff.length);
                    System.arraycopy(packetBuff, 0, joinedArray, _payloadUnit.length, packetBuff.length);
                    _payloadUnit = joinedArray;
                } else {
                    _section_len = (((packetBuff[1]&0xFF)<<8)|(packetBuff[2]&0xFF))&0x0FFF;
                    System.out.printf("payloadUnit is NULL - new Start to Collection (section_len = %d).\n", _section_len );
                    _payloadUnit = packetBuff;
                }

                if (_payloadUnit.length >= _section_len) {
                    _completedPayload = _payloadUnit;       // 완성 버퍼로 옮겨 놓고,
                    this.onComplete(new PayloadUnitComplete(this), _completedPayload, _section_len);    // 이벤트리스너 호출
//                    dump_buffer(_completedPayload);
                    _payloadUnit = packet.getNewSectionPayload();
                    System.out.printf("\n[][] TRACE .. #4-1 (_payloadUnit=%X)[][] \n", _payloadUnit );
                }
            }
        } else {        // there are No newSection_payload()
            if (_payloadUnit==null) {
                System.out.println("new section 이 아닌데,  수집 중인 packet 이 아직 없다. - 무시할 것.");
                return false;
            }
            System.out.printf("[][] TRACE .. #1  collecting size=%d (section_len=%d) [][]\n", _payloadUnit.length, _section_len ); ;
            byte[] packetBuff = packet.getPayload();
            byte[] joinedArray = Arrays.copyOf(_payloadUnit, _payloadUnit.length + packetBuff.length);
            System.arraycopy(packetBuff, 0, joinedArray, _payloadUnit.length, packetBuff.length);
            _payloadUnit = joinedArray;

            if (_payloadUnit.length >= _section_len) {
                _completedPayload = _payloadUnit;       // 완성 버퍼로 옮겨 놓고,
                this.onComplete(new PayloadUnitComplete(this), _completedPayload, _section_len );    // 이벤트리스너 호출
//                dump_buffer(_completedPayload);
                _payloadUnit = packet.getNewSectionPayload();
                System.out.printf("\n[][] TRACE .. #4 [][] completed.length=%d\n", _completedPayload.length );
                if (_payloadUnit != null)
                    System.out.printf("\n[][] TRACE .. #5 new _payloadUnit.length=%d [][] \n", _payloadUnit.length );
            }

        }
        return true;
    }

    public void setOnPayloadUnitCompleteListener(PayloadUnitCompleteListener listener) {
        completelistener = listener;
    }
    @Override
    public void onComplete(PayloadUnitComplete event, byte[] data, int section_length) {
        System.out.println(" Payload Unit is completed. : ");
        completelistener.onComplete(event, data, section_length);
    }

    @Override
    public void onInvalidContinuity(PayloadUnitComplete event) {
        System.out.printf("Continuity Counter is mismatched. latest was %d \n", _continuity_counter );
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
