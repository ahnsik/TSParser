package com.aircode.network.ts;

import java.util.ArrayList;
import java.util.zip.CRC32;

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

    private ArrayList<Byte> _streamTypeList;
    private ArrayList<Short> _elemPidList;
//    private short _dmscc_addressable_PID = 0x7FFF;
    private int _crc32;

    public PMT_parse(byte[] packetBuffer) {
//        System.out.println("[] TRACE.. PMT parsing !! ");
        _streamTypeList = new ArrayList<Byte>();
        _elemPidList = new ArrayList<Short>();
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
        byte temp_type = 0;
        short temp_PID = 0x7FFF;
        int temp_es_len = 0;
        do {
            temp_type = packetBuffer[offset];
            temp_PID = (short) (((packetBuffer[offset+1]&0x1F)<<8)|(packetBuffer[offset+2]&0xFF));
            temp_es_len =  (short) (((packetBuffer[offset+3]&0x0F)<<8)|(packetBuffer[offset+4]&0xFF))&0x0FFF;
            _streamTypeList.add(temp_type);
            _elemPidList.add(temp_PID);
//            System.out.printf(">> PMT : stream type=%02X, PID=%04X len=%d bytes\n", temp_type, temp_PID, temp_es_len );
            offset +=(5+temp_es_len);
        } while (offset < _section_len-5);

        _crc32 = (packetBuffer[offset]<<24)|(packetBuffer[offset+1]<<16)|(packetBuffer[offset+2]<<8)|(packetBuffer[offset+3]);
        // TODO: calculate CRC-32 and check with _crc32.
        //System.out.printf("... need to check PMT CRC. _crc32 = %d(%08X) \n", _crc32, _crc32);
    }

    public int calculateCRC(byte[] buffer) {
        int crc_calc;
        if (buffer==null) {
            return -1;
        } else {
            CRC32 crc = new CRC32();
            crc.update(buffer);
            crc_calc = (int) crc.getValue();
            System.out.printf("[][] CRC32 calculated : %08X !! \n", crc_calc);
        }
        return (int)crc_calc & 0xFFFFFFFF;
    }

    public int get_number_of_PID() {
        return _streamTypeList.size();
    }

    public byte get_streamType(int index) {
        if (index >= _streamTypeList.size())
            return -1;
        return _streamTypeList.get(index);
    }
    public short get_ElementPid(int index) {
        if (index >= _elemPidList.size())
            return -1;
        return _elemPidList.get(index);
    }
//    public short get_dmscc_addressable_PID() {
//        for (int i=0; i<_streamTypeList.size(); i++ ) {
//            if (_streamTypeList.get(i)==0x0D) {
//                return _elemPidList.get(i);
//            }
//        }
//        return -1;
//    }
}
