package ts_parser;

import java.util.EventListener;

public interface PayloadUnitCompleteListener extends EventListener {

    void onComplete(PayloadUnitComplete event);
    void onInvalidContinuity(PayloadUnitComplete event);
}

