package com.aircode.network.udp.packet.standard.payload;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class CRI_container {
    private int num_struct;
    private byte[] struct_type;
    private byte[] struct_id;
    private int[] struct_ptr;
    private int[] struct_len;
    private byte[][] struct_body;

    private byte[] result_string;

    public CRI_container(byte[] data) {
        int compression_method = data[0];
        result_string = null;

        switch(compression_method) {
            case 0:
                System.out.println("[][] container()로 구성되어 분석.. [][] --> 관련 Spec: TS 102 822 - 4.5.2.1 Container syntax 참고.");
                container_parse( Arrays.copyOfRange(data, 1, data.length) );
                break;
            case 1:
                System.out.println("[][] compression_structure(). [][] --> 관련 Spec: TS 102 822 - 4.5.2.1 Container syntax 참고.");
                return;     // break;
            default:
                if (compression_method < 0x7F) {
                    System.out.printf("[][] CRI_container: DVB reserved. [][] --> 관련 Spec: TS 102 822 - 4.5.2.1 Container syntax 참고. (%X)\n", compression_method);
                } else {
                    System.out.printf("[][] CRI_container: User private. [][] --> 관련 Spec: TS 102 822 - 4.5.2.1 Container syntax 참고. (%X)\n", compression_method);
                }
                return;     // break;
        }
    }

    public void container_parse(byte[] data) {
        num_struct = data[0];
        int offset = 1;
        struct_id = new byte[num_struct];
        struct_type = new byte[num_struct];
        struct_ptr = new int[num_struct];
        struct_len = new int[num_struct];
        struct_body = new byte[num_struct][];
        for (int i=0; i<num_struct; i++) {
//            System.out.printf("[] before Container_Parse []: 0x%02X,0x%02X,0x%02X,0x%02X,0x%02X,0x%02X,0x%02X,0x%02X\n", data[offset+0], data[offset+1], data[offset+2], data[offset+3], data[offset+4], data[offset+5], data[offset+6], data[offset+7]);
            struct_type[i] = data[offset+0];
            struct_id[i] = data[offset+1];
            struct_ptr[i] = (((data[offset+2]&0xFF)<<16)|((data[offset+3]&0xFF)<<8)|(data[offset+4]&0xFF));
            struct_len[i] = (((data[offset+5]&0xFF)<<16)|((data[offset+6]&0xFF)<<8)|(data[offset+7]&0xFF));
            System.out.printf("[] Container_Parse []: index=%d, type=0x%02X, id=0x%02X, start: %d(0x%06X), length=%d(0x%06X)", i, struct_type[i], struct_id[i], struct_ptr[i], struct_ptr[i], struct_len[i], struct_len[i]);
            System.out.printf("[] DEBUG data.length=%d, str_ptr=%d, str_len=%d, (ptr+len)=%d \n", data.length, struct_ptr[i], struct_len[i], (struct_ptr[i]+struct_len[i]) );
            struct_body[i] = Arrays.copyOfRange(data, struct_ptr[i], struct_ptr[i]+struct_len[i]);
            offset+=8;

            if (struct_type[i]== 2) {       // Data Repository 인 경우에,
                try {
//                    catchFragmentIdAndWriteFile(  decompress(struct_body[i]) );
                    result_string = decompress(struct_body[i]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String filename = "container_type"+struct_type[i]+"_id"+struct_id[i]+"_len"+struct_len[i];
                File fileGz = new File( "./" + filename+".bin" ) ;
                try (FileOutputStream gzFileOut = new FileOutputStream(fileGz)) {
                    gzFileOut.write( struct_body[i] );
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /* BiM 문자열을 압축 풀어주는 ZLib 함수 */
/*    public static void decompressFile(final byte[] compressedData, File raw)
            throws IOException
    {
        InputStream in =
                new InflaterInputStream(new ByteArrayInputStream(compressedData) );
        OutputStream out = new FileOutputStream(raw);
        shovelInToOut(in, out);
        in.close();
        out.close();
    }

    private static void shovelInToOut(InputStream in, OutputStream out)
            throws IOException
    {
        byte[] buffer = new byte[1000];
        int len;
        while((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
*/

    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
    public static byte[] decompress(final byte[] compressed) throws IOException {
        if ((compressed == null) || (compressed.length == 0)) {
            return null;
        }

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outStr = new ByteArrayOutputStream( );
        if (isCompressed(compressed)) {
            final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            int read_len = 0;
            do {
                read_len = gis.read(buffer);
                if (read_len<=0)
                    break;
                outStr.write(buffer, 0, read_len);
            } while( read_len > 0);
        } else {
            outStr.write(compressed);
        }
        return outStr.toByteArray();
    }


    /* 파일로 분리 저장하기 위해, <Schedule fragmentId=" 를 찾아서 id 를 얻고, 그걸 파일 이름으로 하여 xml 로 저장하는 함수. */
/*
    public int catchFragmentIdAndWriteFile(byte[] data) {
        String SEARCH_STR = "fragmentId=";
        int start = indexOfArray(data, SEARCH_STR.getBytes() ) + SEARCH_STR.length()+1;
        int end=0;
        for (int i=start; i<data.length; i++) {
            if (data[i]=='\"') {
                end = i;
                break;
            }
        }
        String fragIdStr = new String( Arrays.copyOfRange(data, start, end) );
        System.out.println("fragmentID = "+fragIdStr);
        int id = Integer.parseInt(fragIdStr);

        String filename = "data_"+id;
        File file = new File( "./" + filename+".xml" ) ;
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            String xml_header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<IPTVContentGuide>\n";
            byte[] xml_head = xml_header.getBytes();
            byte[] xml_tail = "\n</IPTVContentGuide>\n".getBytes();
            byte[] result_xml = addAll( xml_head, data, xml_tail );
            outputStream.write( result_xml );

//            XML_dom dom = new XML_dom(result_xml);
//            System.out.println("\n [][] XML doc has..."+ dom.getNumOfDom("Schedule") + " items of Schedule Data\n" );
//            outputStream.write( data );
        } catch (Exception e){
            e.printStackTrace();
        }
        return id;
    }

    public int indexOfArray(byte[] outerArray, byte[] smallerArray) {
        for(int i = 0; i < outerArray.length - smallerArray.length+1; i++) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; j++) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    public static byte[] addAll(final byte[] array1, byte[] array2, byte[] array3) {
        byte[] allByteArray = new byte[array1.length + array2.length + array3.length];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(array1);
        buff.put(array2);
        buff.put(array3);
        return buff.array();
    }
*/
    public byte[] getResult_string() {
        return result_string;
    }

}
