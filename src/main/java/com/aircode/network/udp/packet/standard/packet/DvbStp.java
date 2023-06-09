package com.aircode.network.udp.packet.standard.packet;

import java.util.Arrays;

import org.slf4j.Logger;

import com.aircode.network.udp.packet.intf.Packet;
import com.aircode.util.LogManager;

public class DvbStp implements Packet{

    public int getTotalSegmentSize() {
        return totalSegmentSize;
    }

    public short getSegmentId() {
        return (short) segmentId;
    }

    public short getSectionNumber() {
        return sectionNumber;
    }

    public short getLastSectionNumber() {
        return lastSectionNumber;
    }

    public byte[] getPrivateHeaderData() {
        return privateHeaderData;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getCrc() {
        if (crcFlag != 0) {
            return crc;
        } else {
            return 0xFFFFFFFF;
        }
    }

    private byte version;
    private byte encryption;
    private byte crcFlag;
    private int totalSegmentSize;

    public byte getPayloadId() {
        return (byte)payloadId;
    }
    public String getPayloadType() {
        switch(payloadId) {
            case 0x01:
                return "ServiceProviderDiscovery";
            case 0x02:
                return "LinearTVDiscovery";
            case 0x03:
                return "ContentGuideDiscovery";
            case 0x05:
                return "PackageDiscovery";
            case 0xA3:
                return "ScheduleDiscovery";
            case 0xF0:
                return "SystemTimeDiscovery";
            default:
                return "UNKNOWN";
        }
    }

    private short payloadId;
    private int segmentId;

    public byte getSegmentVersion() {
        return (byte)segmentVersion;
    }

    private short segmentVersion;
    private short sectionNumber;
    private short lastSectionNumber;

    public String getCompressionType() {
        switch(compression) {
            case 0:
                return "Not compressed.";
            case 1:
                return "BiM (Binary Format of MPEG-7)";
            case 2:
                return "GZIP";
            case 7:
                return "User defined";
            default:
                return "UNKNOWN";
        }
    }

    public byte getCompression() {
        return compression;
    }

    private byte compression;
    private byte providerIdFlag;
    private byte privateHeaderLength;

    public int getServiceProviderId() {
        if (providerIdFlag != 0)
            return serviceProviderId;
        else
            return 0;
    }

    private int serviceProviderId;
    private byte[] privateHeaderData;
    private byte[] payload;
    private int crc;
    protected final Logger logger = LogManager.getInstance().getLogger(DvbStp.class);

    public DvbStp (byte[] data) {
        int offset = 0;
        byte reserved;
        version = (byte)((data[offset] & 0b11000000) >> 6);
        reserved =  (byte)((data[offset] & 0b00111000) >> 3);
        encryption = (byte)((data[offset] & 0b00000110) >> 1);
        crcFlag = (byte)(data[offset] & 0b00000001);
        totalSegmentSize = ((data[++offset] & 0xFF) << 16) + ((data[++offset] & 0xFF) << 8) + (data[++offset] & 0xFF);
        payloadId = (short)(data[++offset] & 0xFF);
        segmentId = ((data[++offset] & 0xFF) << 8) + (data[++offset] & 0xFF);
        segmentVersion = (short)(data[++offset] & 0xFF);
        sectionNumber = (short)(((data[++offset] & 0xFF) << 4) + ((data[++offset] & 0xF0) >> 4));
        lastSectionNumber = (short)(((data[offset] & 0x0F) << 4) + (data[++offset] & 0xFF));
        compression = (byte)((data[++offset] & 0b11100000) >> 5);
        providerIdFlag = (byte)((data[offset] & 0b00010000) >> 4);
        privateHeaderLength = (byte)(data[offset] & 0x0F);
        if (providerIdFlag != 0) {
            serviceProviderId = ((data[++offset] & 0xFF) << 24) + ((data[++offset] & 0xFF) << 16) + ((data[++offset] & 0xFF) << 8) + (data[++offset] & 0xFF);
        } else {
            serviceProviderId = 0;
        }
        if (privateHeaderLength > 0) privateHeaderData = Arrays.copyOfRange(data, ++offset, offset + privateHeaderLength);
        offset++;
        logger.info("\t\t offset="+offset+", privateHeaderLength="+privateHeaderLength );
        offset = offset + privateHeaderLength;
        if (crcFlag==0) {
            crc = -1;
            payload = Arrays.copyOfRange(data, offset, data.length);
        } else {
            crc = ((data[data.length-4]&0xFF)<<24) + ((data[data.length-3]&0xFF)<<16) + ((data[data.length-2]&0xFF)<<8) + (data[data.length-1]&0xFF);
            payload = Arrays.copyOfRange(data, offset, (data.length-4) );
        }
        //logger.info("totalSegmentSize : " + totalSegmentSize);
    }
    public int getNumbering() {
        return this.sectionNumber;
    }
    public byte[] getData() {
        return this.payload;
    }
}
