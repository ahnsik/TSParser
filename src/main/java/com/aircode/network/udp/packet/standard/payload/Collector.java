package com.aircode.network.udp.packet.standard.payload;

import com.aircode.network.udp.packet.standard.DocumentCompleted;
import com.aircode.network.udp.packet.standard.DocumentCompletedListener;
import com.aircode.network.udp.packet.standard.packet.DvbStp;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

public class Collector implements DocumentCompletedListener {
    private final byte _payloadId;
    private short _segmentId;
    private short _latest_segmentVersion;
    private short _segmentVersion;
    private int _totalSegmentSize;
    private int _lastSectionNumber;
    private byte _compressionType;
    private int _serviceProviderId;
    private boolean _sectionReceivedFlag[];
    public byte get_payloadId() {
        return _payloadId;
    }

    public short get_segmentId() {
        return _segmentId;
    }

//    protected EventListenerList listenerList;       // = new EventListenerList();
    private DocumentCompletedListener listener = null;

    public byte[] getPayload() {
        if ( ! isCompleted() )
            return null;
        ByteArrayOutputStream collected = new ByteArrayOutputStream();
        for (byte[] payload : _payloads) {
            collected.write(payload, 0, payload.length);
        }
        return collected.toByteArray();
    }

    private byte[][] _payloads;         // PACKET 들을 SESSION 번호 별로 따로 따로 저장해 두어야 나중에 하나로 모을 수 있음.
    private long _crc;

    public void set_crc_checked(boolean _crc_checked) {
        this._crc_checked = _crc_checked;
    }

    public boolean is_crc_checked() {
        return _crc_checked;
    }

    private boolean _crc_checked;

    public Collector(byte payloadId) {
        this._payloadId = payloadId;
        _crc = -1;
        _crc_checked = false;
        _sectionReceivedFlag = null;
        _latest_segmentVersion = -1;
//        listenerList = new EventListenerList();
//        listener = new DocumentCompletedListener() {
//            @Override
//            public void onComplete(DocumentCompleted event) {
//            }
//
//            @Override
//            public void onInvalidVersion(DocumentCompleted event) {
//            }
//        }
    }

    public void setPropertiesFrom(DvbStp packet) {
        if (_payloadId != packet.getPayloadId()) {
            throw new NullPointerException();
        }
        _segmentId = packet.getSegmentId();
        _segmentVersion = packet.getSegmentVersion();
        _totalSegmentSize = packet.getTotalSegmentSize();
        _lastSectionNumber = packet.getLastSectionNumber();
//        System.out.printf(" [CHECK] New Collector - lastSectionNum : %d(0x%X)\n", _lastSectionNumber, _lastSectionNumber);
        _compressionType = packet.getCompression();
        _serviceProviderId = packet.getServiceProviderId();
        if (_payloads == null) {
            _payloads = new byte[_lastSectionNumber+1][];
        }
    }

//    public void print_wierd_packet_header(DvbStp packet) {
//        System.out.printf("[DVBSTP] version=%d(0x%02X), encryption=%d(0x%02X) \n", packet.getSegmentVersion(),);
//        packet.getSegmentId()
//    }

    public boolean append(DvbStp packet) {
        // Check this packet is suitable or not.
        if (_payloadId != packet.getPayloadId()) {
            System.out.printf(" [ERROR] DVBSTP packet is weird.: payloadID is not Matched ! (received=%d, collecting=%d) \n", packet.getPayloadId(), _payloadId);
            return false;
        }
        if (_segmentId != packet.getSegmentId()) {
            System.out.printf(" [ERROR] DVBSTP packet is weird.: segmentID is not Matched ! (received=%d, collecting=%d) \n", packet.getSegmentId(), _segmentId);
            return false;
        }

        byte[] paylodOnly = packet.getData();
        int sectionNum = packet.getSectionNumber();
        int lastSectionNum = packet.getLastSectionNumber();

        if (_segmentVersion!= packet.getSegmentVersion() ) {        // sectionVersion 이 안 맞으면 초기화
            onInvalidVersion(new DocumentCompleted(this, _payloadId, _segmentId, _segmentVersion) );    // 우선 Event로 알려 주고,
            // 몽땅 초기화 하고 새로 받아 옴.
            System.out.printf(" [CHECK] DVBSTP packet lastSectionNum : old=%d(0x%X), new=%d(0x%x)\n", _lastSectionNumber, _lastSectionNumber, lastSectionNum, lastSectionNum);
            _lastSectionNumber = lastSectionNum;
            _sectionReceivedFlag = null;
            if (_payloads == null) {
                _payloads = new byte[_lastSectionNumber+1][];
            }
        }
        // 같은 버전이 완성된 상태로 이미 존재하므로 패킷을 모으지 않고 버린다.
        if (_latest_segmentVersion == _segmentVersion) {
//            System.out.println("_segmentVersion is Same. wating for new version of Segment.");
//            return true;      // TODO: 디버깅을 위해 우선 version 확인은 무시.
        } else {
//            System.out.printf("_latest_segmentVersion(%d) is not same with new version(%d.\n", _latest_segmentVersion,_segmentVersion );
            _latest_segmentVersion = -1;
        }

        /* some Error checks */
        if (lastSectionNum < 0) {
            System.out.printf(" [ERROR] DVBSTP packet is weird.: lastSectionNumber(%d) is smaller than 0 \n", lastSectionNum);
            return false;
        }
        if (lastSectionNum != _lastSectionNumber) {
            System.out.printf(" [ERROR] DVBSTP packet is weird.: lastSectionNumber is not Matched ! (received=%d, collecting=%d) \n", lastSectionNum, _lastSectionNumber);
            return false;
        }
        if (sectionNum > lastSectionNum) {
            System.out.printf(" [ERROR] DVBSTP packet is weird.: sectionNumber(%d) is bigger > than lastSectionNumber(%d) ! \n", sectionNum, lastSectionNum );
            return false;
        }
        _payloads[sectionNum] = new byte[paylodOnly.length];
        System.arraycopy( paylodOnly, 0, _payloads[sectionNum], 0, paylodOnly.length );

        if (_sectionReceivedFlag==null) {
//            System.out.println(" No received sections. make new array" );
            _sectionReceivedFlag = new boolean[lastSectionNum+1];
        }
        _sectionReceivedFlag[sectionNum] = true;

        ///////////////// 완성되었는지 Check.
        if ( isCompleted() ) {
//            System.out.printf("A Segment completed. payloadId=%02X(%d), segmentID=%04X (version=%02x) \n", _payloadId, _payloadId, _segmentId, _segmentVersion );
            int packetCrc = packet.getCrc();
            if ( packetCrc!=-1 ) {
                int calculatedCrc = calculateCRC();
                if (calculatedCrc != packetCrc) {
                    System.out.printf("[][] ERROR !! [][] CRC does not matched !.... (Calculated:%08X != inPacket:%08X) \n", calculatedCrc, packetCrc);
                    System.out.print("[][]   need to clear Collector to restart again.\n");
                } else {    // CRC check 까지 완료됨.
                    set_crc_checked(true);
                }
            }
            // 완성되었음을 event 로 알려 줌.  onCompleted() 호출 될 것임.
            _latest_segmentVersion = _segmentVersion;       // 완성된 버전을 기록해 둔다. - 중복되는 버전은 처리하지 않기 위함.
            fireCompletedEvent(new DocumentCompleted(this, _payloadId, _segmentId, _segmentVersion) );
        }
        return true;
    }

    public void dump() {
        if ( isCompleted() ) {
            byte[] dumpBuff = this.getPayload();
            System.out.printf(" ------ Payload (ID=%02X, segmentID=%02X): %d bytes ------------------\n", _payloadId, _segmentId, dumpBuff.length );
            for (int i=0; i<dumpBuff.length; i++) {
                if ( i%16==0 ) {
                    System.out.printf("\n>\t");
                }
                System.out.printf("%02X,", dumpBuff[i]);
            }
            System.out.println("\n----------------------------- END of Buffer --");
        } else {
            System.out.printf(" !! Section colleced %d bytes. total %d of %d received.\n", bytesCollected(), bytesCollected(), _totalSegmentSize );
            System.out.println(" !! Section collecting is not completed.");
        }
    }

    public boolean isEmpty() {
        if (_payloads == null)
            return true;
        if (_payloads.length == 0)
            return true;
        return false;
    }

    public boolean isCompleted() {
        if (_payloads==null)
            return false;
        if (_sectionReceivedFlag==null)
            return false;
        if (_payloads.length < 1)
            return false;
        for (int i=0; i<_payloads.length; i++) {
            if ( !_sectionReceivedFlag[i] ) {
                return false;
            }
        }
        return true;
    }

    public int bytesCollected() {       // 수신한 데이터 사이즈를 %로 구할 때 사용. 다운로드 진행률
        int total_received = 0;
        if (_payloads==null)
            return 0;
        for (int i = 0; i <= _lastSectionNumber; i++) {
            if (_payloads[i] != null) {
                total_received += _payloads[i].length;
            }
        }
        return total_received;
    }

    public int calculateCRC() {
        if (!isCompleted()) {
            _crc = -1;
        } else {
            byte[] buffer = this.getPayload();
            CRC32 crc = new CRC32();
            crc.update(buffer);
            _crc = crc.getValue();
//            System.out.printf("[][] CRC32 calculated : %08X !! \n", _crc);
        }
        return (int)_crc & 0xFFFFFFFF;
    }

    private void fireCompletedEvent(DocumentCompleted event) {
//        Object[] listeners = listenerList.getListenerList();
//        for (int i = listeners.length-2; i>=0; i-=2) {
//            if (listeners[i] == DocumentCompletedListener.class) {
//                ((DocumentCompletedListener)listeners[i+1]).onComplete(event);
//            }
//        }
        if (listener != null)
            listener.onComplete(event);

//        ///////// 디버깅을 위해, 일단 file 로 저장해 놓는다..
//        String filename = "pkt"+_payloadId+"_Segment"+_segmentId + ".section";
//        File file = new File( "./" + filename ) ;
//        // File fileGZ = new File( "./" + filename+".gz" );      // 이제 *.gz 파일을 저장할 필요는 없다.
//        System.out.println("write to (FileName):" + filename );
//        try (FileOutputStream outputStream = new FileOutputStream(file)) {
//        //    FileOutputStream gzFileOut = new FileOutputStream(fileGZ);        // 압축해제 하지 않은, GZ형태를 그대로 저장한다면..
//        //    gzFileOut.write( lookup_segment.getPayload() );                   // Payload 모은 것들을 그대로 파일로 저장함.
//            outputStream.write( decompress(getPayload()) );
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        /////////// 여기까지. 디버깅을 위함

    }

    public void addCompletedEventListener(DocumentCompletedListener customListener) {
//        listenerList.add(DocumentCompletedListener.class, listener);
        listener = customListener;

    }

    public void removeCompletedEventListener(DocumentCompletedListener customListener) {
//        listenerList.remove(DocumentCompletedListener.class, listener);
        listener = null;
    }

    /*
        DVBSTP 로 받아 온 패킷 Section들을 모아 segment가 완성되었을 때 호출 된다.
     */
    @Override
    public void onComplete(DocumentCompleted event) {

/*        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == DocumentCompletedListener.class) {
                ((DocumentCompletedListener)listeners[i+1]).onComplete(event);

                switch(_payloadId) {
                    case (byte)0xA3:      // 만약, payload_id 가 0xA3 (Schedule) 이라면, CRI_container parsing 해야 함.
                        try {
                            System.out.println("CRI_comtainer write.. 0xA3: container_parsing.." );
                            CRI_container parser = new CRI_container( decompress( getPayload()) );
                        } catch (IOException e) {
                            System.out.println("[][] EXCEPTION when parse 0xA3.");
                            byte[] dump = getPayload();
                            for (int j=0; j<100; j++) {
                                System.out.printf("%02X,", dump[j] );
                            }
                            throw new RuntimeException(e);
                        }
                        break;
                    case (byte)0xA4:      // 아직 index Container 에 대해서는 분석이 필요하므로, 그냥 binary 로 저장.
                        System.out.println("Payload ID 0xA4 is not descripted yet... Sorry." );
                        //break;
                    default:        // 그외의 경우엔 모두 GZ압축 풀어서 XML파일로 저장.
                        String xmlFilename = "pkt"+_payloadId+"_Segment"+_segmentId;
                        byte[] payload = getPayload();
                        try {
                            if (isCompressed(payload)) {        // 압축헤더를 찾으면 GZ 압축 풀고
                                File xmlFile = new File( "./" + xmlFilename + ".xml" ) ;
                                System.out.println("write to (XML FileName):" + xmlFilename );
                                FileOutputStream outputStream = new FileOutputStream(xmlFile);
                                outputStream.write(decompress(payload));
                            } else {
                                File xmlFile = new File( "./" + xmlFilename + ".bin" ) ;
                                System.out.println("write to (binary FileName):" + xmlFilename );
                                FileOutputStream outputStream = new FileOutputStream(xmlFile);
                                outputStream.write( payload );
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                }

            }
        }
*/
    }

    @Override
    public void onInvalidVersion(DocumentCompleted event) {
        // DVBSTP 로 받아들인 패킷 데이터가, 현재 수집중인 Segment 와 버전이 맞지 않는다.
        // 이 경우엔, Collector 를 새롭게 초기화 해야 할 필요가 있다.
//        Object[] listeners = listenerList.getListenerList();
//
//        for (int i = listeners.length-2; i>=0; i-=2) {
//            if (listeners[i] == DocumentCompletedListener.class) {
//                ((DocumentCompletedListener)listeners[i+1]).onInvalidVersion(event);
//            }
//        }
        listener.onInvalidVersion(event);
    }


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

}



