package com.aircode.network.ts;

import java.util.Arrays;

public class TsPacket {
    private byte _sync_byte;
    private byte _some_indicators;
    private short _PID;
    private byte _control_fields;
    private TsPacketAdaptationField _af;
    private byte[] _payload;
    private byte[] _nextSection_payload;

    public TsPacket(byte[] buf) {
        _payload = null;
        _nextSection_payload = null;

        _sync_byte = buf[0];
        if (_sync_byte != 0x47) {
            return;
        }
        if (buf.length < 188) {
            return;
        }
        _some_indicators = buf[1];
//        if ((_some_indicators & 0x80)!=0) {
//            return;
//        }
        _PID = (short)((((buf[1]&0xFF)<<8)|(buf[2]&0xFF) )&0x1FFF);
//        System.out.printf("\n[][] check - TS Packet parsing _PID: %04x\n", _PID);

        _control_fields = buf[3];
        int offset = 4;
        if ((getAdaptationFieldControl() == 2) || (getAdaptationFieldControl() == 3)) {
            offset++;           // because of Adaptation Field Length has 1 byte.
            _af = new TsPacketAdaptationField( Arrays.copyOfRange( buf, 4, 188) );
            offset += _af.getLength();
        } else {
            _af = null;
        }
        if ((getAdaptationFieldControl() == 1) || (getAdaptationFieldControl() == 3)) {     // payload 가 있을 때에만.
            if (getPUSI()) {        //
                int section_start_offset = buf[offset];
                offset++;
                if (section_start_offset==0) {
                    _payload = Arrays.copyOfRange( buf, offset+section_start_offset, 188 );
                    _nextSection_payload = null;
                } else {
                    _payload = Arrays.copyOfRange( buf, offset, offset+section_start_offset );
                    _nextSection_payload = Arrays.copyOfRange( buf, offset+section_start_offset, 188 );
                }
            } else {
//                System.out.printf("[][] no pusi. check - Arrays.copyOfRange --> offset=%d, buf.length=%d\n", offset, buf.length);
                _payload = Arrays.copyOfRange( buf, offset, 188);
            }
//        } else {
//            System.out.printf("[][] TRACE..B -- no payload packet. (TsPacket) [][] \n");
        }
    }

    public boolean getTsErrorIndicator() {
        return ((_some_indicators & 0x80)!=0);
    }
    public boolean getPUSI() {
        return ((_some_indicators & 0x40)!=0);
    }
    public boolean getTsPriority() {
        return ((_some_indicators & 0x20)!=0);
    }
    public byte getTsScramblingControl() {
        return (byte) ((_control_fields>>6)&0x03);
    }
    public byte getAdaptationFieldControl() {
        return (byte) ((_control_fields>>4)&0x03);
    }
    public byte getAdaptationFieldLength() {
        if (_af == null) return 0;
        return _af.getLength();
    }
    public byte getContinuityCounter() {
        return (byte) ((_control_fields>>4)&0x0F);
    }

    public short getPID() {
        return _PID;
    }

    public byte[] getPayload() {
        return _payload;
    }

    public byte[] getNewSectionPayload() {
        return _nextSection_payload;
    }

    public boolean hasError() {
        if (_sync_byte != 0x47) return true;
        if ((_some_indicators & 0x80)!=0) return true;
        return false;
    }

}
