package com.github.kzhunmax.virtualmdc;

import com.github.kzhunmax.virtualmdc.wrapper.VirtualThreadExecutors;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class VirtualMDCTest {

    private static final Logger LOG = LoggerFactory.getLogger(VirtualMDCTest.class);

    @Test
    void testDropInReplacement() {
        // Test that SLF4J MDC works through our adapter
        MDC.put("testKey", "testValue");
        assertEquals("testValue", MDC.get("testKey"));
        LOG.info("Drop-in test log");

        MDC.clear();
        assertNull(MDC.get("testKey"));
    }

    @Test
    void testPropagationInVirtualThreads() throws Exception {
        // Set MDC in parent
        VirtualMDC.put("requestId", "parent-123");

        ExecutorService executor = VirtualThreadExecutors.propagatingVirtualExecutor();

        executor.submit(() -> {
            // Inside virtual thread: MDC should be propagated
            LOG.info("Inside virtual thread");
            assertEquals("parent-123", VirtualMDC.get("requestId"));
        }).get();  // Wait for completion

        // After task: parent MDC unchanged
        assertEquals("parent-123", VirtualMDC.get("requestId"));

        executor.shutdown();
    }

    @Test
    void testClearAfterTask() throws Exception {
        VirtualMDC.put("requestId", "to-clear");

        ExecutorService executor = VirtualThreadExecutors.propagatingVirtualExecutor();

        executor.submit(() -> {
            // Should have propagated value
            assertEquals("to-clear", VirtualMDC.get("requestId"));
            LOG.info("Task with MDC");
        }).get();

        // Parent still has value (propagation doesn't clear parent)
        assertEquals("to-clear", VirtualMDC.get("requestId"));

        VirtualMDC.clear();
        executor.shutdown();
    }
}
