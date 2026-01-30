package com.github.kzhunmax.virtualmdc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;


/**
 * Virtual threads friendly Mapped Diagnostic Context (MDC) implementation.
 * <p>
 * Behavior is identical to SLF4J MDC, but with support for propagation through wrappers.
 * Uses ThreadLocal for mutable storage — safe for virtual and platform threads.
 * </p>
 * <p>
 * Important: For propagation in virtual threads, use
 * {@link com.github.kzhunmax.virtualmdc.wrapper.VirtualThreadExecutors} or similar wrappers.
 * </p>
 */
public final class VirtualMDC {

    private static final ThreadLocal<HashMap<String, String>> CONTEXT =
            ThreadLocal.withInitial(HashMap::new);

    private VirtualMDC() {
    }

    /**
     * Adds/updates values in the context of the current thread.
     *
     * @param key   the key context attribute
     * @param value the value context attribute
     * @throws IllegalArgumentException if key is null
     */
    public static void put(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (value == null) {
            remove(key);
            return;
        }
        CONTEXT.get().put(key, value);
    }

    /**
     * Retrieves a value from the context or null if not present.
     *
     * @param key the key context attribute
     * @return the value associated with the key, or null if not found
     * @throws IllegalArgumentException if key is null
     */
    public static String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return CONTEXT.get().get(key);
    }

    /**
     * Returns a copy of the current context map.
     *
     * @return immutable copy of the current context map
     */
    public static Map<String, String> getCopyOfContextMap() {
        Map<String, String> map = CONTEXT.get();
        return map.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(map));
    }

    /**
     * Clear the context of the current thread.
     */
    public static void clear() {
        CONTEXT.get().clear();
    }

    /**
     * Removes the context value identified by the key.
     *
     * @param key the key context attribute to remove
     */
    public static void remove(String key) {
        if (key != null) {
            CONTEXT.get().remove(key);
        }
    }

    /**
     * Internal API: Sets the context from the map (clears the previous one and putAll).
     * Used by wrappers for propagation. Do not call manually unless you know what you are doing.
     *
     * @param contextMap the map to replace the current context
     */
    public static void setContextMap(Map<String, String> contextMap) {
        CONTEXT.get().clear();
        if (contextMap != null && !contextMap.isEmpty()) {
            CONTEXT.get().putAll(contextMap);
        }
    }

    /**
     * Internal API: Completely clears ThreadLocal (prevents memory leaks in virtual threads).
     * Called by wrappers after task execution.
     */
    public static void clearThreadLocal() {
        CONTEXT.remove();
    }

    /**
     * Executes a task with the given context snapshot (internal for wrappers).
     *
     * @param task     the task to execute
     * @param snapshot the context snapshot to apply during execution
     */
    public static void executeWithContext(Runnable task, Map<String, String> snapshot) {
        Map<String, String> previous = getCopyOfContextMap();
        try {
            setContextMap(snapshot);
            task.run();
        } finally {
            setContextMap(previous);
            if (Thread.currentThread().isVirtual()) {
                clearThreadLocal();
            }
        }
    }

    /**
     * Executes a supplier with the given context snapshot (internal for wrappers).
     *
     * @param task     the supplier to execute
     * @param snapshot the context snapshot to apply during execution
     * @param <T>      the type of the result
     * @return the result of the supplier
     */
    public static <T> T executeWithContext(Supplier<T> task, Map<String, String> snapshot) {
        Map<String, String> previous = getCopyOfContextMap();
        try {
            setContextMap(snapshot);
            return task.get();
        } finally {
            setContextMap(previous);
            if (Thread.currentThread().isVirtual()) {
                clearThreadLocal();
            }
        }
    }

    /**
     * Executes a callable with the given context snapshot (internal for wrappers).
     *
     * @param task     the callable to execute
     * @param snapshot the context snapshot to apply during execution
     * @param <T>      the type of the result
     * @return the result of the callable
     * @throws Exception if the callable throws an exception
     */
    public static <T> T executeWithContext(Callable<T> task, Map<String, String> snapshot) throws Exception {
        Map<String, String> previous = getCopyOfContextMap();
        try {
            setContextMap(snapshot);
            return task.call();
        } finally {
            setContextMap(previous);
            if (Thread.currentThread().isVirtual()) {
                clearThreadLocal();
            }
        }
    }

}
