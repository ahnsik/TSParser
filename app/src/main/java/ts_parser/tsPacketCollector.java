package ts_parser;

import java.util.Arrays;

public class tsPacketCollector implements PayloadUnitCompleteListener {
    private short _PID;
    private byte _continuity_counter;

    public byte[] getCompletedPayload() {
        return completedPayload;
    }

    private byte[] completedPayload;
    private byte[] payloadUnit;
    private PayloadUnitCompleteListener completelistener;

    public tsPacketCollector(short PID) {
        _PID = PID;
        _continuity_counter = 0;
        payloadUnit = null;     //new byte[];
        completedPayload = null;     //new byte[];
    }
    public void setCollectorInfoFromPacket(tsPacket packet) {
        this.set_PID(packet.getPID());
    }
    public void set_PID(short collecting_PID) {       // _PID가 바뀌면 기존에 모으던 payload 들은 몽땅 버리기로 한다.
        _PID = collecting_PID;
        payloadUnit = null;     //new byte[];
        completedPayload = null;     //new byte[];
    }
    public boolean append_packet(tsPacket packet) {
        if ( packet.hasError() )
            return false;
        if ( packet.getPID() != _PID )
            return false;
        if ( (packet.getContinuityCounter()+1)%0x0F == _continuity_counter)
            completelistener.onInvalidContinuity(new PayloadUnitComplete(this) );
        if (packet.getPUSI()) {
            byte[] packetBuff = packet.getPayload();
            if (packetBuff != null) {
                byte[] joinedArray;
                if (payloadUnit != null) {
                    System.out.printf("\npayloadUnit.length=%d, packetBuff.length=%d\n", payloadUnit.length , packetBuff.length);
                    joinedArray = Arrays.copyOf(payloadUnit, payloadUnit.length + packetBuff.length);
                    System.arraycopy(packetBuff, 0, joinedArray, payloadUnit.length, packetBuff.length);
                    payloadUnit = joinedArray;
                } else {
                    System.out.printf("\npayloadUnit is NULL\n");
//                    System.arraycopy(packetBuff, 0, joinedArray, payloadUnit.length, packetBuff.length);
                    payloadUnit = packetBuff;
                }
//                System.arraycopy(packetBuff, 0, joinedArray, payloadUnit.length, packetBuff.length);
//                payloadUnit = joinedArray;
            }
            completedPayload = payloadUnit;
            completelistener.onComplete(new PayloadUnitComplete(this) );
            payloadUnit = packet.getNewSectionPayload();
        } else {        // there are No newSection_payload()
            byte[] packetBuff = packet.getPayload();
            byte[] joinedArray = Arrays.copyOf(payloadUnit, payloadUnit.length + packetBuff.length);
            System.arraycopy(packetBuff, 0, joinedArray, payloadUnit.length, packetBuff.length);
            payloadUnit = joinedArray;
        }
        return true;
    }

    public void setOnPayloadUnitCompleteListener(PayloadUnitCompleteListener listener) {
        completelistener = listener;
    }
    @Override
    public void onComplete(PayloadUnitComplete event) {
        completelistener.onComplete(event);

        System.out.println(" Payload Unit is completed. : ");
        for (byte b : payloadUnit) {
            System.out.printf("%02X ", b);
        }
        System.out.println("-------- done.");
    }

    @Override
    public void onInvalidContinuity(PayloadUnitComplete event) {

    }
}
