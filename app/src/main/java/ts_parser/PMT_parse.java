package ts_parser;

public class PMT_parse {
    private byte _table_id;
    private boolean _section_syntax_indicator;
    private short _section_len;
    private short _program_num;
    private byte _version_num;
    private boolean _curr_next_indicator;
    private byte _section_num;
    private byte _last_section_num;

    public PMT_parse(byte[] packetBuffer) {
        _table_id = packetBuffer[0];
/*        if (_table_id != 0x01) {      // PAT테이블인지 확인.
            System.out.println("[WARNING] this is not PMT !! ");
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
*/
    }
}
