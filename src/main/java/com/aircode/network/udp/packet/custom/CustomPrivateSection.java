package com.aircode.network.udp.packet.custom;

import java.util.Arrays;

import com.aircode.network.udp.packet.standard.section.PrivateSection;

public class CustomPrivateSection extends PrivateSection {
    private DSTSection dstSection;

    public CustomPrivateSection (byte[] data) throws Exception{
        super(data);
    }

    public void parseDSTPrivateData () throws Exception {
        //------------Check VendorId, HardwareVersionId--------------
        //Make Filter (vendorId Length : 3, hardwareVersionId Length : 4)
        byte[] filter = Arrays.copyOfRange(getPrivateData(), 0, 15);
        int offset = 0;
        long vendorId = (long) ((filter[offset] & 0x00FF) << 16) + (long) ((filter[offset + 1] & 0x00FF) << 8) + (long) (filter[offset + 2] & 0x00FF);
        offset = offset + 3;
        long hardwareVersionId = (long) ((filter[offset] & 0x00FF) << 24) + (long) ((filter[offset + 1] & 0x00FF) << 16) + (long) ((filter[offset + 2] & 0x00FF) << 8) + (long) (filter[offset + 3] & 0x00FF);
        //Skip software version id
        offset = offset + 8;

        long stb_type = (long) ((filter[offset] & 0x00FF) << 24) + (long) ((filter[offset + 1] & 0x00FF) << 16) + (long) ((filter[offset + 2] & 0x00FF) << 8) + (long) (filter[offset + 3] & 0x00FF);

        //--------------Check DST Version Number---------------
        Integer version = getVersionNumber();

        logger.info("DST Parsing - TableId : " + getTableId() + ", Version : " + version + ", Length : " + " bytes");

        Integer dst_LastSectionNumber = getLastSectionNumber();
        Integer dst_CurrentSectionNumber = getSectionNumber();
        dstSection.setStbType(stb_type);
    }
}
