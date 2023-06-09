package com.aircode.network.udp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import com.aircode.network.udp.packet.intf.Packet;

public class PacketAssembler {
private boolean isFailed = false;
    private ArrayList<Packet> packetArray;
    private Comparator<Packet> sortFunction;
    public PacketAssembler () {
        sortFunction = Comparator.comparing(Packet::getNumbering);
    }
    public void add(Packet packet) {
        this.packetArray.add(packet);
    }
    public byte[] assemble () throws Exception {
        this.packetArray.sort(sortFunction);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.packetArray.forEach(packet -> {
            try {
                outputStream.write(packet.getData());
            } catch (IOException e) {
                // TODO Auto-generated catch block   
                this.isFailed = true;             
                e.printStackTrace();                
            }
        });
        if (this.isFailed) throw new Exception("Failed Packet Assemble");
        return outputStream.toByteArray();
    }
    public boolean check () {
        
        return false;
    }
}
