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

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the ForkJoinPool.commonPool()
     * with the current MDC context captured.
     *
     * @param supplier a function returning the value to be used to complete the returned CompletableFuture
     * @param <U>      the function's return type
     * @return the new CompletableFuture
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        Map<String, String> snapshot = VirtualMDC.getCopyOfContextMap();
        return CompletableFuture.supplyAsync(() -> VirtualMDC.executeWithContext(supplier, snapshot));
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the ForkJoinPool.commonPool()
     * after it runs the given action with the current MDC context captured.
     *
     * @param runnable the action to run before completing the returned CompletableFuture
     * @return the new CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        Map<String, String> snapshot = VirtualMDC.getCopyOfContextMap();
        return CompletableFuture.runAsync(() -> VirtualMDC.executeWithContext(runnable, snapshot));
    }

    /**
     * Wraps a function to enable context propagation in chained calls (e.g. thenApply).
     *
     * @param function the function to wrap
     * @param <T>      the input type
     * @param <R>      the output type
     * @return a wrapped function that restores the context before execution
     */
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
