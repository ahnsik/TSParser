package com.aircode.network.ts;

import java.util.Arrays;

public class DsmccAddressable_parse {
    private byte _table_id;
    private boolean _error_detection_type;
    private short _addressable_section_len;
    private byte _deviceId_7_0;
    private byte _deviceId_15_8;
    private byte _payload_scrambling_control;
    private byte _address_scrambling_control;
    private boolean _LLCSNAP_flag;
    private byte _section_num;
    private byte _last_section_num;
    private byte _deviceId_23_16;
    private byte _deviceId_31_24;
    private byte _deviceId_39_32;
    private byte _deviceId_47_40;

    private byte[] _data_byte;
    private int _crc32;

    public DsmccAddressable_parse(byte[] packetBuffer) {
//        System.out.printf("==== DSMCC packet parser dump:\n\t");
//        for (int i=0; i<packetBuffer.length; i++) {
//            System.out.printf(" %02X", packetBuffer[i]);
//        }
//        System.out.println("\n==== end of DSMCC packet parser dump ==============");

        _table_id = packetBuffer[0];
        if (_table_id != 0x3F) {
            System.out.printf("[WARNING][WARNING] this is not DSMCC (%02X) !! [WARNING][WARNING]\n", _table_id);
        }
        if ( (packetBuffer[1]&0x80)!=0 ) {   // bit 가 0 인지 체크해 본다.
            System.out.println("[WARNING] need to check zero bit is not ZERO. !! ");
            return;
        }
        _error_detection_type = ((packetBuffer[1]&0x40)!=0);
        _addressable_section_len = (short) (((packetBuffer[1]<<8)|packetBuffer[2])&0x0FFF);
        _deviceId_7_0 = packetBuffer[3];
        _deviceId_15_8 = packetBuffer[4];
        _payload_scrambling_control = (byte) ((packetBuffer[5]>>4)&0x03);
        _address_scrambling_control = (byte) ((packetBuffer[5]>>2)&0x03);
        _LLCSNAP_flag = ((packetBuffer[5]&0x02)!=0);
        if ( (packetBuffer[5]&0x01)!=1 ) {   // bit 가 0 인지 체크해 본다.
            System.out.println("[WARNING] need to check one bit is not ONE. !! ");
            return;
        }
        _section_num = packetBuffer[6];
        _last_section_num = packetBuffer[7];
        _deviceId_23_16 = packetBuffer[8];
        _deviceId_31_24 = packetBuffer[9];
        _deviceId_39_32 = packetBuffer[10];
        _deviceId_47_40 = packetBuffer[11];
        int remain_len = packetBuffer.length -12-((_error_detection_type)?4:0);
        System.out.printf("_section_len = %d, remain_len = %d \n", _addressable_section_len, remain_len );

//        if (_data_byte==null) {
            _data_byte = Arrays.copyOf(packetBuffer, remain_len);
//        } else {
//            System.arraycopy(packetBuffer, 12, _data_byte, 0 , remain_len );
//        }
        _crc32 = ((packetBuffer[remain_len-4]<<24)|(packetBuffer[remain_len-3]<<16)|(packetBuffer[remain_len-2]<<8)|packetBuffer[remain_len-1] );
    }

    public byte[] get_data_byte() {
        return _data_byte;
    }

}
