package net.kr9ly.octopus.test;

import net.kr9ly.octopus.Callback;

public class TestCallbacks {

    EventA eventA;

    EventB eventB;

    EventC eventC;

    @Callback
    public boolean aCallback(EventA eventA) {
        this.eventA = eventA;
        return false;
    }

    @Callback
    public boolean bCallback(EventB eventB) {
        this.eventB = eventB;
        return true;
    }

    @Callback
    public void cCallback(EventC eventC) {
        this.eventC = eventC;
    }
}
