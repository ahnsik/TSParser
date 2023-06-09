package com.aircode.network.udp.packet.standard;

import com.aircode.network.udp.packet.standard.payload.Collector;

import java.util.ArrayList;

public interface DvbStpListener {
//    void onComplete(byte[] received_data);
    void onServiceProviderDiscoveryReceived(byte[] received_data);      // payload_id = 0x01
    void onLinearTVDiscoveryReceived(byte[] received_data);             // payload_id = 0x02
    void onContentGuideDiscoveryReceived(byte[] received_data);         // payload_id = 0x03
    void onPackageDiscoveryReceived(byte[] received_data);              // payload_id = 0x05
    void onScheduleIndexDiscoveryReceived(byte[] received_data);        // payload_id = 0xA4
    void onScheduleDiscoveryReceived(byte[] received_data);             // payload_id = 0xA3
    void onSystemTimeDiscoveryReceived(byte[] received_data);           // payload_id = 0xF0

}
