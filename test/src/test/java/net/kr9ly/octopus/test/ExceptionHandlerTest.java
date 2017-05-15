package net.kr9ly.octopus.test;

import net.kr9ly.octopus.Octopus;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExceptionHandlerTest {

    private boolean childCalled;

    private boolean parentCalled;

    private Octopus parent;

    private Octopus child;

    private Octopus child2;

    @Before
    public void before() {
        childCalled = false;
        parentCalled = false;
        parent = new Octopus(throwable -> {
            if (throwable instanceof ParentException) {
                parentCalled = true;
                return true;
            }
            return false;
        });
        child = new Octopus(parent, throwable -> {
            if (throwable instanceof ChildException) {
                childCalled = true;
                return true;
            }
            return false;
        });
        child2 = new Octopus(child);
        child.register(new ExceptionCallbacks());
    }

    @Test
    public void testChildHandler() {
        child2.post(new EventA());
        assertTrue(childCalled);
        assertFalse(parentCalled);
    }

    @Test
    public void testParentHandler() {
        child2.post(new EventB());
        assertFalse(childCalled);
        assertTrue(parentCalled);
    }

    @Test
    public void testChildBroadcast() {
        parent.broadcast(new EventA());
        assertTrue(childCalled);
        assertFalse(parentCalled);
    }

    @Test
    public void testParentBroadcast() {
        parent.broadcast(new EventB());
        assertFalse(childCalled);
        assertTrue(parentCalled);
    }
}
