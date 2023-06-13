package com.aircode.network.ts;

import java.util.EventObject;

public class PayloadUnitComplete extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    private short _PID;

    public PayloadUnitComplete(Object source) {
        super(source);
    }
}
