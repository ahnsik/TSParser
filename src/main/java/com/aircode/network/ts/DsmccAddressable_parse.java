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
        _addressable_section_len = (short) ((((packetBuffer[1]&0x0F)<<8)|(packetBuffer[2])&0xFF)&0x0FFF);
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
//        int remain_len = packetBuffer.length -12-((_error_detection_type)?4:0);
        int _end_position = 3+_addressable_section_len;
//        System.out.printf("[][] _section_num=(%d/%d) [][] _payload_scrambling_control=%d, _address_scrambling_control=%d, \n", _section_num, _last_section_num, _payload_scrambling_control, _address_scrambling_control);
//        System.out.printf("_section_len = %d, _end_position = %d \n", _addressable_section_len, _end_position );
        _data_byte = Arrays.copyOfRange(packetBuffer, 12, _end_position-4 );        // 12는 Header Size, -4 는 checksum 값 또는 CRC32.

        int offset = _end_position-4;
        _crc32 = (((packetBuffer[offset]&0xFF)<<24)|((packetBuffer[offset+1]&0xFF)<<16)|((packetBuffer[offset+2]&0xFF)<<8)|(packetBuffer[offset+3]&0xFF) );
//        System.out.printf("_crc32 position: %d CRC32=%08x\n", offset+3, _crc32 );
    }

    public byte[] get_data_byte() {
        return _data_byte;
    }

}
