package ts_parser;

import java.util.Arrays;

public class tsPacketCollector implements PayloadUnitCompleteListener {
    private short _PID;
    private byte _continuity_counter;

    public byte[] getCompletedPayload() {
        return _completedPayload;
    }

    private byte[] _completedPayload;
    private byte[] _payloadUnit;
    private PayloadUnitCompleteListener completelistener;

    public tsPacketCollector(short PID) {
        _PID = PID;
        _continuity_counter = 0;
        _payloadUnit = null;     //new byte[];
        _completedPayload = null;     //new byte[];
    }
    public void setCollectorInfoFromPacket(tsPacket packet) {
        this.set_PID(packet.getPID());
    }
    public void set_PID(short collecting_PID) {       // _PID가 바뀌면 기존에 모으던 payload 들은 몽땅 버리기로 한다.
        _PID = collecting_PID;
        _payloadUnit = null;     //new byte[];
        _completedPayload = null;     //new byte[];
    }
    public boolean append_packet(tsPacket packet) {
        if ( packet.hasError() )
            return false;
        if ( packet.getPID() != _PID )
            return false;
        if ( (packet.getContinuityCounter()+1)%0x0F == _continuity_counter)
            this.onInvalidContinuity(new PayloadUnitComplete(this) );
        if (packet.getPUSI()) {
            System.out.printf("\n[][] TRACE .. #2 [][]\n" );
            byte[] packetBuff = packet.getPayload();
            if (packetBuff != null) {
                System.out.printf("\n[][] TRACE .. #3 [][] %X, %X\n", _completedPayload, _payloadUnit );
                if (_payloadUnit != null) {
                    byte[] joinedArray;
                    System.out.printf("\npayloadUnit.length=%d, packetBuff.length=%d\n", _payloadUnit.length , packetBuff.length);
                    joinedArray = Arrays.copyOf(_payloadUnit, _payloadUnit.length + packetBuff.length);
                    System.arraycopy(packetBuff, 0, joinedArray, _payloadUnit.length, packetBuff.length);
                    _payloadUnit = joinedArray;
                } else {
                    System.out.printf("\npayloadUnit is NULL\n");
                    _payloadUnit = packetBuff;
                }
//            } else {
//                System.out.printf("\n[][] TRACE .. #xx [][] %X, %X\n", _completedPayload, _payloadUnit );
//                _completedPayload = _payloadUnit;
//                packetBuff = packet.getNewSectionPayload();
//                System.out.printf("\n[][] TRACE .. #4 [][] %X, %X\n", _completedPayload, packetBuff.length );
//                if (_completedPayload != null)
//                    this.onComplete(new PayloadUnitComplete(this) );
//                _payloadUnit = packetBuff;

                _completedPayload = _payloadUnit;       // 완성 버퍼로 옮겨 놓고,
                this.onComplete(new PayloadUnitComplete(this) );    // 이벤트리스너 호출
                _payloadUnit = packet.getNewSectionPayload();
//                System.out.printf("\n[][] TRACE .. #4 [][] %X, %X\n", _completedPayload, _payloadUnit);
            }
        } else {        // there are No newSection_payload()
            System.out.printf("\n[][] TRACE .. #1 [][]\n");
            byte[] packetBuff = packet.getPayload();
            byte[] joinedArray = Arrays.copyOf(_payloadUnit, _payloadUnit.length + packetBuff.length);
            System.arraycopy(packetBuff, 0, joinedArray, _payloadUnit.length, packetBuff.length);
            _payloadUnit = joinedArray;
        }
        return true;
    }

    public void setOnPayloadUnitCompleteListener(PayloadUnitCompleteListener listener) {
        completelistener = listener;
    }
    @Override
    public void onComplete(PayloadUnitComplete event) {
        System.out.println(" Payload Unit is completed. : ");
        completelistener.onComplete(event);
    }

    @Override
    public void onInvalidContinuity(PayloadUnitComplete event) {
        System.out.printf("Continuity Counter is mismatched. latest was %d \n", _continuity_counter );
        completelistener.onComplete(event);
    }
}
