package com.aircode.network.udp.packet.custom.descriptor;

public abstract class Descriptor {
    public static final int APPLICATION_NAME_TAG = 0x01;
    public static final int BROADCAST_TAG = 0xC0;
    public static final int UNICAST_TAG = 0xC1;
    public static final int MULTICAST_TAG = 0xC3;
    public static final int APPLICATION_PROP_TAG = 0xC5;
    public static final int STB_MODEL_TAG = 0xC6;
    public static final int STATISTICS_TAG = 0xC7;
    public static final int FLUTE_CONFIGURATION_TAG = 0xC8;
    public static final int FLUTE_VERSION_TAG = 0xC9;
    public static final int STB_MAPPING_TAG = 0xCA;
    public static final int FW_LINKAGE_TAG = 0xCB;
    public static final int ADV_MULTICAST_TAG = 0xCC;

    byte[] data;
    int descriptorTag;
    int descriptorLength;

    public Descriptor(byte[] data){
        this.data = data;
    }

    abstract void decode() throws Exception;

    public int getDescriptorTag() {
        return descriptorTag;
    }
    public int getDescriptorLength() {
        return descriptorLength;
    }
}
