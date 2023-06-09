package com.aircode.network.udp.packet.standard.section;

import java.util.Arrays;

import org.slf4j.Logger;

import com.aircode.util.LogManager;

public class PrivateSection {
    
    private Integer tableId = null;
    private Integer sectionSyntaxIndicator = null;
    private Integer privateIndicator = null;
    private Integer privateSectionLength = null;
    private Integer tableIdExtension = null;
    private Integer versionNumber = null;
    private Integer currentNextIndicator = null;
    private Integer sectionNumber = null;
    private Integer lastSectionNumber = null;
    private Long crc32 = null;
    
    private byte[] privateData = null;
    protected Logger logger = null;
    //Parsing data when constructed
    public PrivateSection(byte[] data) throws Exception{
        parse(data);
        logger = LogManager.getInstance().getLogger(getClass());
    }

    private void parse(byte[] data) throws Exception{
        int offset = 0;
        tableId = (int)(data[offset] & 0x00FF);
        offset = offset + 1;

        sectionSyntaxIndicator = (int)((data[offset] >> 7 & 0x01));
        privateIndicator = (int)((data[offset] >> 6) & 0x01);
        privateSectionLength = (int)((data[offset] & 0x0F) << 8) + (data[offset + 1] & 0x00FF);
        offset = offset + 2;

        if(sectionSyntaxIndicator.equals(0)){
            privateData = Arrays.copyOfRange(data, offset, offset + privateSectionLength);
        }else{
            tableIdExtension = (int)((data[offset] << 8 & 0x00FF)) + (data[offset + 1] & 0x00FF);
            offset = offset + 2;

            versionNumber = (int)((data[offset] >> 1) & 0x001F);
            currentNextIndicator = (int)(data[offset] & 0x01);
            offset = offset + 1;

            sectionNumber = (int)(data[offset] & 0x00FF);
            offset = offset + 1;

            lastSectionNumber = (int)(data[offset] & 0x00FF);
            offset = offset + 1;

            privateData = Arrays.copyOfRange(data, offset, offset + (privateSectionLength - 9));
            offset = offset + (privateSectionLength - 9);

            crc32 = (long)((data[offset] & 0x00FF) << 24) + (long)((data[offset + 1] & 0x00FF) << 16) + (long)((data[offset + 2] & 0x00FF) << 8) + (long)(data[offset + 3] & 0x00FF);
            offset = offset + 4;
        }

    }

    @Override
    public String toString() {
        return "PrivateSection {" +
                "tableId=" + tableId +
                ", sectionSyntaxIndicator=" + sectionSyntaxIndicator +
                ", privateIndicator=" + privateIndicator +
                ", privateSectionLength=" + privateSectionLength +
                ", tableIdExtension=" + tableIdExtension +
                ", versionNumber=" + versionNumber +
                ", currentNextIndicator=" + currentNextIndicator +
                ", sectionNumber=" + sectionNumber +
                ", lastSectionNumber=" + lastSectionNumber +
                ", crc32=" + crc32 +
                '}';
    }

    public Integer getTableId() {
        return tableId;
    }

    public Integer getSectionSyntaxIndicator() {
        return sectionSyntaxIndicator;
    }

    public Integer getPrivateIndicator() {
        return privateIndicator;
    }

    public Integer getPrivateSectionLength() {
        return privateSectionLength;
    }

    public Integer getTableIdExtension() {
        return tableIdExtension;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public Integer getCurrentNextIndicator() {
        return currentNextIndicator;
    }

    public Integer getSectionNumber() {
        return sectionNumber;
    }

    public Integer getLastSectionNumber() {
        return lastSectionNumber;
    }

    public Long getCrc32() {
        return crc32;
    }

    public byte[] getPrivateData() {
        return privateData;
    }
}
