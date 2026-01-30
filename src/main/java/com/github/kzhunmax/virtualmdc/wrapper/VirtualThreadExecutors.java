package com.github.kzhunmax.virtualmdc.wrapper;

import com.github.kzhunmax.virtualmdc.VirtualMDC;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Utilities for creating ExecutorService with automatic MDC context propagation.
 * <p>
 * Use {@link #propagatingVirtualExecutor()} instead of Executor.newVirtualThreadPerTaskExecutor() for virtual threads.
 * The context (MDC) is captured at the moment of submit and restored in the child thread.
 * Safe: clears the context after execution (prevents leaks).
 * </p>
 */
public final class VirtualThreadExecutors {

    private VirtualThreadExecutors() {
    }

    /**
     * Returns an ExecutorService that creates a new virtual thread for each task,
     * with automatic MDC propagation.
     *
     * @return a new propagating ExecutorService
     */
    public static ExecutorService propagatingVirtualExecutor() {
        return new PropagatingExecutorService(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Decorates an existing ExecutorService with MDC propagation logic.
     *
     * @param delegate the underlying ExecutorService to wrap
     * @return a wrapped ExecutorService that propagates MDC
     */
    public static ExecutorService decorate(ExecutorService delegate) {
        return new PropagatingExecutorService(delegate);
    }

    private static class PropagatingExecutorService extends AbstractExecutorService {

        private final ExecutorService delegate;

        PropagatingExecutorService(ExecutorService delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(Runnable command) {
            delegate.execute(wrap(command));
        }

        @Override
        public Future<?> submit(Runnable task) {
            return delegate.submit(wrap(task));
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return delegate.submit(wrap(task), result);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return delegate.submit(wrap(task));
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return delegate.invokeAll(tasks.stream().map(this::wrap).collect(Collectors.toList()));
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.invokeAll(tasks.stream().map(this::wrap).collect(Collectors.toList()), timeout, unit);
        }

        // Delegate method lifecycle
        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        // Wrap logic
        private Runnable wrap(Runnable task) {
            Map<String, String> snapshot = VirtualMDC.getCopyOfContextMap();
            return () -> VirtualMDC.executeWithContext(task, snapshot);
        }

        private <T> Callable<T> wrap(Callable<T> task) {
            Map<String, String> snapshot = VirtualMDC.getCopyOfContextMap();
            return () -> VirtualMDC.executeWithContext(task, snapshot);
        }
    }
}
