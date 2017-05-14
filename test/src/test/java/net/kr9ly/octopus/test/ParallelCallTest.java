package net.kr9ly.octopus.test;

import net.kr9ly.octopus.Octopus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.stream.IntStream;

public class ParallelCallTest {

    private Octopus octopus;

    private ParallelCallbacks callbacks;

    @Before
    public void before() {
        octopus = new Octopus();
        callbacks = new ParallelCallbacks();
        octopus.register(callbacks);
    }

    @Test
    public void testParallelPost() {
        long count = IntStream.range(0, 10000)
                .parallel()
                .peek(value -> octopus.post(new EventA()))
                .count();

        assertEquals(count, callbacks.counter.get());
    }

    @Test
    public void testParallelRegisterAndPost() {
        long count = IntStream.range(0, 10000)
                .parallel()
                .peek(value -> octopus.register(new TestCallbacks()))
                .peek(value -> octopus.post(new EventA()))
                .count();

        assertEquals(count, callbacks.counter.get());
    }
}
