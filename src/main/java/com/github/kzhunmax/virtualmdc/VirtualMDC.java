package com.github.kzhunmax.virtualmdc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


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
     */
    public static String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return CONTEXT.get().get(key);
    }

    /**
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
     * Removes the key
     */
    public static void remove(String key) {
        if (key != null) {
            CONTEXT.get().remove(key);
        }
    }

    // Additional helper methods for internal use (in wrappers)
    static void setContextMap(Map<String, String> contextMap) {
        CONTEXT.get().clear();
        if (contextMap != null && !contextMap.isEmpty()) {
            CONTEXT.get().putAll(contextMap);
        }
    }

    static void clearThreadLocal() {
        CONTEXT.remove(); // Prevent potential memory leaks
    }

}
