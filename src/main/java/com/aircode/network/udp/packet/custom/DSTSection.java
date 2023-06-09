package com.aircode.network.udp.packet.custom;
import java.util.ArrayList;

import com.aircode.network.udp.packet.custom.descriptor.Descriptor;

public class DSTSection {
    private Integer     packageType = null;
    private Long        stbType = null;
    private Integer     protocolId = null;
    private Integer     downloadCommand = null;

    private ArrayList<Descriptor> descriptorList;

    public Integer getPackageType() {
        return packageType;
    }

    public void setPackageType(Integer packageType) {
        this.packageType = packageType;
    }

    public Long getStbType() {
        return stbType;
    }

    public void setStbType(Long stbType) {
        this.stbType = stbType;
    }

    public Integer getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Integer protocolId) {
        this.protocolId = protocolId;
    }

    public Integer getDownloadCommand() {
        return downloadCommand;
    }

    public void setDownloadCommand(Integer downloadCommand) {
        this.downloadCommand = downloadCommand;
    }

    public ArrayList<Descriptor> getDescriptorList() {
        return descriptorList;
    }

    public void setDescriptorList(ArrayList<Descriptor> descriptorList) {
        this.descriptorList = descriptorList;
    }
}
