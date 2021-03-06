package net.kr9ly.octopus;

import net.kr9ly.octopus.internal.Callbacks;
import net.kr9ly.octopus.internal.CallbacksFinder;
import net.kr9ly.octopus.internal.Caller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Copyright 2017 kr9ly
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressWarnings("unchecked")
public class Octopus {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock readLock = lock.readLock();

    private final Lock writeLock = lock.writeLock();

    private final Map<Object, Map<Object, Caller<?>>> callersMap = new HashMap<Object, Map<Object, Caller<?>>>();

    private final Octopus parent;

    private final ExceptionHandler exceptionHandler;

    private final WeakHashMap<Octopus, Void> children = new WeakHashMap<Octopus, Void>();

    public Octopus() {
        this(null, null);
    }

    public Octopus(ExceptionHandler exceptionHandler) {
        this(null, exceptionHandler);
    }

    public Octopus(Octopus parent) {
        this(parent, null);
    }

    public Octopus(Octopus parent, ExceptionHandler exceptionHandler) {
        this.parent = parent;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Register all callback methods.
     *
     * @param target Callbacks
     * @param <T>    Callbacks Class
     */
    public <T> void register(T target) {
        try {
            CallbacksFinder finder = (CallbacksFinder) Class.forName(target.getClass().getPackage().getName() + ".CallbacksFinderImpl").newInstance();
            Callbacks<T> callbacks = finder.findCallbacks(target);
            if (callbacks == null) {
                return;
            }
            List<Caller<?>> callers = callbacks.callers(target);
            for (Caller<?> caller : callers) {
                try {
                    writeLock.lock();
                    Map<Object, Caller<?>> eventCallers = callersMap.get(caller.eventClass());
                    if (eventCallers == null) {
                        eventCallers = new HashMap<Object, Caller<?>>();
                        callersMap.put(caller.eventClass(), eventCallers);
                    }
                    eventCallers.put(target, caller);
                    if (parent != null) {
                        parent.children.put(this, null);
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        } catch (Throwable e) {
            // do nothing.
        }
    }

    /**
     * Unregister all callback methods.
     *
     * @param target Callbacks
     * @param <T>    Callbacks Class
     */
    public <T> void unregister(T target) {
        try {
            CallbacksFinder finder = (CallbacksFinder) Class.forName(target.getClass().getPackage().getName() + ".CallbacksFinderImpl").newInstance();
            Callbacks<T> callbacks = finder.findCallbacks(target);
            if (callbacks == null) {
                return;
            }
            List<Caller<?>> callers = callbacks.callers(target);
            for (Caller<?> caller : callers) {
                try {
                    writeLock.lock();
                    Map<Object, Caller<?>> eventCallers = callersMap.get(caller.eventClass());
                    if (eventCallers == null) {
                        return;
                    }
                    eventCallers.remove(target);
                    if (eventCallers.isEmpty()) {
                        callersMap.remove(caller.eventClass());
                    }
                    if (parent != null && callersMap.isEmpty()) {
                        parent.children.remove(this);
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        } catch (Throwable e) {
            // do nothing.
        }
    }

    /**
     * Emit event to upper level.
     *
     * @param event Event Object
     * @param <T>   Event Class
     */
    public <T> void post(T event) {
        try {
            readLock.lock();
            boolean preventPropagation = call(event);
            if (parent != null && !preventPropagation) {
                parent.post(event);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Emit event to lower level.
     *
     * @param event Event Object
     * @param <T>   Event Class
     */
    public <T> void broadcast(T event) {
        try {
            readLock.lock();
            boolean preventPropagation = call(event);
            if (!preventPropagation) {
                for (Octopus octopus : children.keySet()) {
                    octopus.broadcast(event);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    private <T> boolean call(T event) {
        Map<Object, Caller<?>> callers = callersMap.get(event.getClass());
        boolean preventPropagation = false;
        if (callers != null) {
            for (Caller<?> caller : callers.values()) {
                try {
                    preventPropagation |= ((Caller<T>) caller).call(event);
                } catch (Throwable e) {
                    handleException(e);
                }
            }
        }
        return preventPropagation;
    }

    private void handleException(Throwable e) {
        if (exceptionHandler != null && exceptionHandler.handle(e)) {
            return;
        }

        if (parent != null) {
            parent.handleException(e);
        }
    }
}
