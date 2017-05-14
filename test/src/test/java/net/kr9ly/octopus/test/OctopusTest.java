package net.kr9ly.octopus.test;

import net.kr9ly.octopus.Octopus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class OctopusTest {

    private Octopus parent;

    private Octopus child;

    private TestCallbacks parentCallbacks;

    private TestCallbacks childCallbacks;

    @Before
    public void before() {
        parent = new Octopus();
        child = new Octopus(parent);
        parentCallbacks = new TestCallbacks();
        childCallbacks = new TestCallbacks();
        child.register(childCallbacks);
        parent.register(parentCallbacks);
    }

    @Test
    public void testVoidMethod() {
        parent.post(new EventC());
        assertNotNull(parentCallbacks.eventC);
    }

    @Test
    public void testRegister() {
        parent.post(new EventA());
        assertNull(childCallbacks.eventA);
        assertNotNull(parentCallbacks.eventA);
    }

    @Test
    public void testUnregister() {
        parent.unregister(parentCallbacks);
        parent.post(new EventA());
        assertNull(childCallbacks.eventA);
        assertNull(parentCallbacks.eventA);
    }

    @Test
    public void testParentRegister() {
        child.post(new EventA());
        assertNotNull(childCallbacks.eventA);
        assertNotNull(parentCallbacks.eventA);
    }

    @Test
    public void testPreventPropagation() {
        child.post(new EventB());
        assertNotNull(childCallbacks.eventB);
        assertNull(parentCallbacks.eventB);
    }
}
