package com.github.kzhunmax.virtualmdc.wrapper;

import com.github.kzhunmax.virtualmdc.VirtualMDC;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wrappers for CompletableFuture with automatic MDC propagation.
 */
public final class CompletableFutureWrapper {
    private CompletableFutureWrapper() {}

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        Map<String, String> snapshot = VirtualMDC.getCopyOfContextMap();
        return CompletableFuture.supplyAsync(() -> VirtualMDC.executeWithContext(supplier, snapshot));
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        Map<String, String> snapshot = VirtualMDC.getCopyOfContextMap();
        return CompletableFuture.runAsync(() -> VirtualMDC.executeWithContext(runnable, snapshot));
    }

    // Chainable thenApply / thenRun etc.
    public static <T, R> Function<T, R> withPropagation(Function<T, R> function) {
        Map<String, String> snapshot = VirtualMDC.getCopyOfContextMap();
        return t -> {
            Map<String, String> previous = VirtualMDC.getCopyOfContextMap();
            try {
                VirtualMDC.setContextMap(snapshot);
                return function.apply(t);
            } finally {
                VirtualMDC.setContextMap(previous);
                if (Thread.currentThread().isVirtual()) {
                    VirtualMDC.clearThreadLocal();
                }
            }
        };
    }
}
