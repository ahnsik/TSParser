package com.aircode.network.ts;

import java.util.EventListener;

public interface PayloadUnitCompleteListener extends EventListener {

    void onComplete(PayloadUnitComplete event, byte[] data, int section_length, int pid);
    void onInvalidContinuity(PayloadUnitComplete event);
}

