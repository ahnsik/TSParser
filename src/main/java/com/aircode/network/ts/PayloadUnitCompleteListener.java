package com.aircode.network.ts;

import java.util.EventListener;

public interface PayloadUnitCompleteListener extends EventListener {

    void onComplete(PayloadUnitComplete event);
    void onInvalidContinuity(PayloadUnitComplete event);
}

