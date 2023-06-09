package com.aircode.network.udp.intf;

public interface UdpPacketHandler extends Runnable {
    void onReceived(byte[] data);
    void onReceiveFinished();
    default void onReceiveFailed(Throwable e) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
    }
    void onExceptionOccurred(Throwable e);
    void onRetryRequest(int retryCount);
}
