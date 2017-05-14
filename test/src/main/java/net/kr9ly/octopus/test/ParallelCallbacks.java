package net.kr9ly.octopus.test;

import net.kr9ly.octopus.Callback;

import java.util.concurrent.atomic.AtomicInteger;

public class ParallelCallbacks {

    final AtomicInteger counter = new AtomicInteger(0);

    @Callback
    public void callEventA(EventA event) {
        counter.incrementAndGet();
    }
}
