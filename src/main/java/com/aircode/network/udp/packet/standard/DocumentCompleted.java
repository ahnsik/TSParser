package com.aircode.network.udp.packet.standard;

import java.util.EventObject;

public class DocumentCompleted extends EventObject {
    public byte get_payloadId() {
        return _payloadId;
    }

    public short get_segmentId() {
        return _segmentId;
    }

    public short get_segmentVersion() {
        return _segmentVersion;
    }

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    private byte _payloadId;
    private short _segmentId;
    private short _segmentVersion;
    public DocumentCompleted(Object source) {
        super(source);
    }
    public DocumentCompleted(Object source, byte payloadId, short segmentId, short segmentVersion) {
        super(source);
        _payloadId = payloadId;
        _segmentId = segmentId;
        _segmentVersion = segmentVersion;
    }

}
