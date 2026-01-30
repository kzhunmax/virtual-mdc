package com.github.kzhunmax.virtualmdc;

import org.slf4j.spi.MDCAdapter;

import java.util.Deque;
import java.util.Map;

/**
 * SLF4J MDCAdapter, which delegates to VirtualMDC
 * <p>
 * Drop-in replacement: add JAR to the classpath - SLF4J will pick it up automatically.
 * </p>
 */
public class VirtualMDCAdapter implements MDCAdapter {

    /**
     * Default constructor required by SLF4J SPI.
     */
    public VirtualMDCAdapter() {
    }

    @Override
    public void put(String key, String val) {
        VirtualMDC.put(key, val);
    }

    @Override
    public String get(String key) {
        return VirtualMDC.get(key);
    }

    @Override
    public void remove(String key) {
        VirtualMDC.remove(key);
    }

    @Override
    public void clear() {
        VirtualMDC.clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return VirtualMDC.getCopyOfContextMap();
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        VirtualMDC.clear();
        if (contextMap != null) {
            VirtualMDC.setContextMap(contextMap);
        }
    }

    // New methods SLF4J 2.0+ for nested contexts (not supported)
    @Override
    public void pushByKey(String key, String value) {
        // Nested not supported — no-op
    }

    @Override
    public String popByKey(String key) {
        // Nested not supported
        return null;
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        // Nested not supported — return null
        return null;
    }

    @Override
    public void clearDequeByKey(String key) {
        // Nested not supported — no-op
    }
}
