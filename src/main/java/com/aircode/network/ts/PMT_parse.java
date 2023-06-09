package com.aircode.network.ts;

public class PMT_parse {
    private byte _table_id;
    private boolean _section_syntax_indicator;
    private short _section_len;
    private short _program_num;
    private byte _version_num;
    private boolean _curr_next_indicator;
    private byte _section_num;
    private byte _last_section_num;
    private short _PCR_PID;
    private short _program_info_length;

    //    private byte[] _streamTypeList;
//    private short[] _elemPidList;
    private short _dmscc_addressable_PID = 0x7FFF;
    private int _crc32;

    public PMT_parse(byte[] packetBuffer) {
        System.out.println("[] TRACE.. PMT parsing !! ");
        _table_id = packetBuffer[0];
        if (_table_id != 0x02) {      // PAT테이블인지 확인.
            System.out.println("[WARNING] this is not PMT !! ");
            return;
        }
        _section_syntax_indicator = ((packetBuffer[1]&0x80)!=0);
        if ( (packetBuffer[1]&0x40)!=0 ) {   // bit 가 0 인지 체크해 본다.
            System.out.println("[WARNING] need to check zero bit is not ZERO. !! ");
            return;
        }
        _section_len = (short) (((packetBuffer[1]<<8)|packetBuffer[2])&0x0FFF);
        _program_num = (short) ((packetBuffer[3]<<8)|packetBuffer[4]);;
        _version_num = (byte) ((packetBuffer[5]>>1)&0x1F);
        _curr_next_indicator = ((packetBuffer[5]&0x01)!=0);
        _section_num = packetBuffer[6];
        _last_section_num = packetBuffer[7];
        _PCR_PID = (short) (((packetBuffer[8]<<8)|packetBuffer[9])&0x1FFF);
        _program_info_length = (short) (((packetBuffer[10]<<8)|packetBuffer[11])&0x0FFF);
        int offset = 12 + _program_info_length;
        int index = 0;
        byte temp_type = 0;
        short temp_PID = 0x7FFF;
        int temp_es_len = 0;
        do {
            temp_type = packetBuffer[offset];
            temp_PID = (short) (((packetBuffer[offset+1]&0x1F)<<8)|(packetBuffer[offset+2]&0xFF));
            System.out.printf("[][] TEMP_LENGTH: %02x,%02x,%02x,%02x,..", packetBuffer[offset+1], packetBuffer[offset+2], packetBuffer[offset+3], packetBuffer[offset+4] );
            temp_es_len =  (short) (((packetBuffer[offset+3]&0x0F)<<8)|(packetBuffer[offset+4]&0xFF))&0x0FFF;
            if (temp_type == 0x0D) {            // DSMCC 패킷은 stream type 이 0x0D,  table_id 가 0x3F
                _dmscc_addressable_PID = temp_PID;
                System.out.printf("[][] Found DSMCC (0x%02x) PID : %04x len=%d bytes\n", temp_type, temp_PID, temp_es_len );
            }
            index++;
            offset +=(5+temp_es_len);
        } while (offset < packetBuffer.length-5);

        int packetLength = packetBuffer.length;
        _crc32 = (packetBuffer[packetLength-4]<<24)|(packetBuffer[packetLength-3]<<16)|(packetBuffer[packetLength-2]<<8)|(packetBuffer[packetLength-1]);
    }

    public short getPidWithTableId(byte tid) {
        return -1;
    }

    public short get_dmscc_addressable_PID() {
        return _dmscc_addressable_PID;
    }
}
