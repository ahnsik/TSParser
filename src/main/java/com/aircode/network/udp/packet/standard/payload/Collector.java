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
//            throw new NullPointerException();
            System.out.printf(" [Collector] payload_id mismatch !! (collector payload_id=0x%X, packet payload_id=0x%X)\n", _payloadId, packet.getPayloadId() );
            return;
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
            _sectionReceivedFlag = new boolean[_lastSectionNumber+1];
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
            System.out.printf(" [CHECK] DVBSTP collector : segment Version invalid. old=%d(0x%X), new=%d(0x%x), lastSectionNum=%d\n", _segmentVersion,_segmentVersion,  packet.getSegmentVersion(),packet.getSegmentVersion(), lastSectionNum );
            onInvalidVersion(new DocumentCompleted(this, _payloadId, _segmentId, _segmentVersion) );    // 우선 Event로 알려 주고,
            // 몽땅 초기화 하고 새로 받아 옴.
//            System.out.printf(" [CHECK] DVBSTP packet lastSectionNum : old=%d(0x%X), new=%d(0x%x)\n", _lastSectionNumber, _lastSectionNumber, lastSectionNum, lastSectionNum);
            _segmentVersion = packet.getSegmentVersion();
            _totalSegmentSize = packet.getTotalSegmentSize();
            _lastSectionNumber = lastSectionNum;
            _compressionType = packet.getCompression();
            _serviceProviderId = packet.getServiceProviderId();
            _sectionReceivedFlag = null;
            _payloads = new byte[_lastSectionNumber+1][];
            _sectionReceivedFlag = new boolean[lastSectionNum+1];
        } else {
            System.out.printf(" [CHECK] DVBSTP collector : _segmentVersion is same. go ahead. (lastSectionNum is %d)\n", lastSectionNum );
        }
        // 같은 버전이 완성된 상태로 이미 존재하므로 패킷을 모으지 않고 버린다.
        if (_latest_segmentVersion == _segmentVersion) {
            System.out.printf("payload(0x%02X) _segmentVersion(%d(0x%02x)) is Same. wating for new version of Segment.\n", _payloadId, _segmentVersion, _segmentVersion );
            return true;      // TODO: 디버깅을 위해 우선 version 확인은 무시.
//        } else {
//            System.out.printf("_latest_segmentVersion(%d) is not same with new version(%d.\n", _latest_segmentVersion,_segmentVersion );
//            _latest_segmentVersion = -1;
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
//                    System.out.print("[][] DVBSTP Collector: CRC checked OK.\n");
                    set_crc_checked(true);
                }
            }
            // 완성되었음을 event 로 알려 줌.  onCompleted() 호출 될 것임.
            _latest_segmentVersion = _segmentVersion;       // 완성된 버전을 기록해 둔다. - 중복되는 버전은 처리하지 않기 위함.
            fireCompletedEvent(new DocumentCompleted(this, _payloadId, _segmentId, _segmentVersion) );
        }
        return true;
    }

    public boolean isEmpty() {
        if (_payloads == null)
            return true;
        if (_payloads.length == 0)
            return true;
        return false;
    }

    public boolean isCompleted() {
        if (_payloads==null) {
            System.out.printf("[DVBSTP collector] _payloads array is null.\n" );
            return false;
        }
        if (_sectionReceivedFlag==null) {
            System.out.printf("[DVBSTP collector] _sectionReceivedFlag array is null.\n" );
            return false;
        }
        if (_payloads.length < 1) {
            System.out.printf("[DVBSTP collector] _payloads.length is 0 or under.\n" );
            return false;
        }
        for (int i=0; i<_payloads.length; i++) {
            if ( !_sectionReceivedFlag[i] ) {
                System.out.printf("_payloadId=%04X, _segment_id=0x%04X, _section(%d of %d) is not received yet.\n", _payloadId, _segmentId, i, _payloads.length-1 );
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
        if (listener != null)
            listener.onComplete(event);
    }

    public void addCompletedEventListener(DocumentCompletedListener customListener) {
        listener = customListener;

    }

    public void removeCompletedEventListener(DocumentCompletedListener customListener) {
        listener = null;
    }

    /*
        DVBSTP 로 받아 온 패킷 Section들을 모아 segment가 완성되었을 때 호출 된다.
     */
    @Override
    public void onComplete(DocumentCompleted event) {
        listener.onComplete(event);
    }

    @Override
    public void onInvalidVersion(DocumentCompleted event) {
        // DVBSTP 로 받아들인 패킷 데이터가, 현재 수집중인 Segment 와 버전이 맞지 않는다.
        // 이 경우엔, Collector 를 새롭게 초기화 해야 할 필요가 있다.
        listener.onInvalidVersion(event);
    }

/*
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
*/
}



