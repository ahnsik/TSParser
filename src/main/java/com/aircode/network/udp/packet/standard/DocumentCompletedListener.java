package com.aircode.network.udp.packet.standard;

import java.util.EventListener;

public interface DocumentCompletedListener extends EventListener {
    /*
        수집 중인 Collector 와 version 이 맞지 않는다. !!
        --> 그동안 수집한 섹션들을 모두 작제해야 한다. 그래야 다음 영화가 완성 될 것.
    */
    void onComplete(DocumentCompleted event);

    void onInvalidVersion(DocumentCompleted event);

}

