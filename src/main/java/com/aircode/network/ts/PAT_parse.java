package com.aircode.network.ts;

import java.util.Arrays;
import java.util.zip.CRC32;

public class PAT_parse {
    private byte _table_id;
    private boolean _section_syntax_indicator;
    private short _section_len;
    private short _ts_id;
    private byte _version_num;
    private boolean _curr_next_indicator;
    private byte _section_num;
    private byte _last_section_num;
    private short _program_num[];
    private short _PMT_PID[];
    private int _crc32;

    public PAT_parse(byte[] packetBuffer) {
//        for (int i=0; i<packetBuffer.length; i++) {
//            System.out.printf(" %02X", packetBuffer[i]);
//        }
//        System.out.println(" .. PAT parsing..");

        _table_id = packetBuffer[0];
        if (_table_id != 0x00) {      // PAT테이블인지 확인.
            System.out.println("[WARNING] this is not PAT !! ");
            return;
        }
        _section_syntax_indicator = ((packetBuffer[1]&0x80)!=0);
        if ( (packetBuffer[1]&0x40)!=0 ) {   // bit 가 0 인지 체크해 본다.
            System.out.println("[WARNING] need to check zero bit is not ZERO. !! ");
            return;
        }
        _section_len = (short) (((packetBuffer[1]<<8)|packetBuffer[2])&0x0FFF);
        _ts_id = (short) ((packetBuffer[3]<<8)|packetBuffer[4]);;
        _version_num = (byte) ((packetBuffer[5]>>1)&0x1F);
        _curr_next_indicator = ((packetBuffer[5]&0x01)!=0);
        _section_num = packetBuffer[6];
        _last_section_num = packetBuffer[7];
        int count = (_section_len-5-4) / 4;       // program 하나 당 4바이트.  -5는 header 부분, -4 는 CRC값.
        _program_num = new short[count];
        _PMT_PID = new short[count];
        int offset = 8;
        for (int i=0; i<count; i++) {
            _program_num[i] = (short) ((packetBuffer[offset]<<8)|packetBuffer[offset+1]&0xFF);
            _PMT_PID[i] = (short) (((packetBuffer[offset+2]<<8)|packetBuffer[offset+3]&0xFF)&0x1FFF);
            System.out.printf(">> PAT - %d] program_num=%d(0x%X), pid=%d(0x%x)\n", i, _program_num[i]&0xFFFF,_program_num[i]&0xFFFF, _PMT_PID[i]&0x1FFF, _PMT_PID[i]&0x1FFF);
            offset += 4;
        }
        _crc32 = (((packetBuffer[offset]&0xFF)<<24)|((packetBuffer[offset+1]&0xFF)<<16)|((packetBuffer[offset+2]&0xFF)<<8)|(packetBuffer[offset+3]&0xFF) );

////      TODO: CRC32 checksum 계산은 따로 확인해 보기로 함. 나중엔 반드시 들어가야 할 내용.
        // TODO: calculate CRC-32 and check with _crc32.
        //System.out.printf("... need to check PAT CRC. _crc32 = %d(%08X) \n", _crc32, _crc32);
//        if (calculateCRC(packetBuffer)!=0) {
//            System.out.printf("[WARNING] This PAT table has Error !! - because CRC checksum is NOT 0 !! (%08X)\n", calculateCRC(packetBuffer) );
//        }
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

    public short get_PMT_PID(int index) {
        return (short)(_PMT_PID[index]&0x1FFF);
    }

    public short get_PMT_PID(short program_num) {
        for (int i=0; i<_PMT_PID.length; i++) {
            if (_program_num[i] == program_num) {
                return (short)(_PMT_PID[i]&0x1FFF);
            }
        }
        return -1;
    }

}
