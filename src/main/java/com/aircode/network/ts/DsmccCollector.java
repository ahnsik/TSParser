package com.aircode.network.ts;

import java.util.Arrays;

public class DsmccCollector {

    private short _pid;
    private byte[] _data_byte;
    private int _section_len;

    public DsmccCollector(short pid) {
        _pid = pid;
        _data_byte = null;
        _section_len = -1;
    }

    public void append(byte[] payload) {
        if (payload==null) {
            System.out.println("DsmccCollector got Null.");
//            return;
        }
        System.out.printf("\nDSMCC collector append(length=%x)\n", payload );
        if (_data_byte==null) {
            _data_byte = Arrays.copyOf(payload, payload.length);
        } else {
            byte[] joinedArray;
            System.out.printf("\njoin array.._data_byte.length = %d, payload.length = %d\n", _data_byte.length, payload.length );
            joinedArray = Arrays.copyOf(_data_byte, _data_byte.length + payload.length);
            System.arraycopy(payload, 0, joinedArray, _data_byte.length, payload.length);
            _data_byte = joinedArray;

            System.out.printf("==== DSMCC packet dump ========================\n");
            for (int i=0; i<payload.length; i++) {
                if ( (payload[i]>0x20)&&(payload[i]<0x7E) ) {
                    System.out.printf("%c", payload[i]);
                } else {
                    System.out.printf("?");
                }
            }
            System.out.printf("\n==== end of DSMCC packet dump ===============\n");
        }
    }

    public short getPID() {
        return _pid;
    }

}
