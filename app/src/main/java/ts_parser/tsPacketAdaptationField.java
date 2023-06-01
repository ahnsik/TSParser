package ts_parser;

import java.util.Arrays;

public class tsPacketAdaptationField {
    private byte adaptation_field_len;
    private byte adaptation_flags;
    private long PCR;
    private long OPCR;
    private byte splice_countdown;
    private byte ts_priv_data_length;
    private byte[] ts_priv_data;
    private byte adapField_ext_len;
    private byte optional_flags;
//    private boolean ltw_valid_flag;
//    private short ltw_offset;
//    private int piecewise_rate;
//    private byte splice_type;
//    private long TDS_next_au;

    public tsPacketAdaptationField(byte[] buf ) {
        adaptation_field_len = buf[0];
        if (adaptation_field_len <= 0) {
            return;
        }
        adaptation_flags = buf[1];
        PCR = (((buf[2])<<40)|(buf[3]<<32)|(buf[4]<<24)|(buf[5]<<16)|(buf[6]<<8)|(buf[7]) );
        OPCR = (((buf[8])<<40)|(buf[9]<<32)|(buf[10]<<24)|(buf[11]<<16)|(buf[12]<<8)|(buf[13]) );
        splice_countdown = buf[14];
        ts_priv_data_length = buf[15];
        int offset = 16+ts_priv_data_length;
/*        ts_priv_data = Arrays.copyOfRange(buf, 16,offset);
        offset++;
        adapField_ext_len = buf[offset++];
        optional_flags = buf[offset++];
*/
        //  ....
    }


    public byte getLength() {
        System.out.printf("[][] adaptation_field_len=%d [][] \n", adaptation_field_len );
        return adaptation_field_len;
    }

}

