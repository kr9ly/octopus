package net.kr9ly.octopus.test;

import net.kr9ly.octopus.Callback;

public class ExceptionCallbacks {

    @Callback
    public void throwException(EventA eventA) throws ChildException {
        throw new ChildException();
    }

    @Callback
    public void throwParentException(EventB eventB) throws ParentException {
        throw new ParentException();
    }
}
