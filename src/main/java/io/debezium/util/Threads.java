package io.debezium.util;

import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public class Threads {
    private static final String DEBEZIUM_THREAD_NAME_PREFIX = "debezium-";
    private static final Logger LOGGER = LoggerFactory.getLogger(Threads.class);

    public static TimeSince timeSince(final Clock clock) {
        return new TimeSince() {
            private long lastTimeInMillis;

            public void reset() {
                this.lastTimeInMillis = clock.currentTimeInMillis();
            }

            public long elapsedTime() {
                long elapsed = clock.currentTimeInMillis() - this.lastTimeInMillis;
                return elapsed <= 0L ? 0L : elapsed;
            }
        };
    }

    public static Timer timer(Clock clock, final Duration time) {
        final TimeSince start = timeSince(clock);
        start.reset();
        return new Timer() {
            public boolean expired() {
                return start.elapsedTime() > time.toMillis();
            }

            public Duration remaining() {
                return time.minus(start.elapsedTime(), ChronoUnit.MILLIS);
            }
        };
    }

    public static Thread interruptAfterTimeout(String threadName, long timeout, TimeUnit timeoutUnit, TimeSince elapsedTimer) {
        Thread threadToInterrupt = Thread.currentThread();
        return interruptAfterTimeout(threadName, timeout, timeoutUnit, elapsedTimer, threadToInterrupt);
    }

    public static Thread interruptAfterTimeout(String threadName, long timeout, TimeUnit timeoutUnit, TimeSince elapsedTimer, Thread threadToInterrupt) {
        TimeUnit var10004 = TimeUnit.MILLISECONDS;
        Objects.requireNonNull(elapsedTimer);
        LongSupplier var10005 = elapsedTimer::elapsedTime;
        Objects.requireNonNull(elapsedTimer);
        return timeout(threadName, timeout, timeoutUnit, 100L, var10004, var10005, elapsedTimer::reset, () -> {
            threadToInterrupt.interrupt();
        });
    }

    public static Thread timeout(String threadName, long timeout, TimeUnit timeoutUnit, TimeSince elapsedTimer, Runnable uponTimeout) {
        TimeUnit var10004 = TimeUnit.MILLISECONDS;
        Objects.requireNonNull(elapsedTimer);
        LongSupplier var10005 = elapsedTimer::elapsedTime;
        Objects.requireNonNull(elapsedTimer);
        return timeout(threadName, timeout, timeoutUnit, 100L, var10004, var10005, elapsedTimer::reset, uponTimeout);
    }

    public static Thread timeout(String threadName, long timeout, TimeUnit timeoutUnit, long sleepInterval, TimeUnit sleepUnit, TimeSince elapsedTimer, Runnable uponTimeout) {
        Objects.requireNonNull(elapsedTimer);
        LongSupplier var10005 = elapsedTimer::elapsedTime;
        Objects.requireNonNull(elapsedTimer);
        return timeout(threadName, timeout, timeoutUnit, sleepInterval, sleepUnit, var10005, elapsedTimer::reset, uponTimeout);
    }

    public static Thread timeout(String threadName, long timeout, TimeUnit timeoutUnit, long sleepInterval, TimeUnit sleepUnit, LongSupplier elapsedTime, Runnable uponStart, Runnable uponTimeout) {
        long timeoutInMillis = timeoutUnit.toMillis(timeout);
        long sleepTimeInMillis = sleepUnit.toMillis(sleepInterval);
        Runnable r = () -> {
            if (uponStart != null) {
                uponStart.run();
            }

            while (elapsedTime.getAsLong() < timeoutInMillis) {
                try {
                    Thread.sleep(sleepTimeInMillis);
                } catch (InterruptedException var8) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            uponTimeout.run();
        };
        return new Thread(r, "debezium--timeout-" + threadName);
    }

    private Threads() {
    }

    public static ThreadFactory threadFactory(Class<? extends SourceConnector> connector, String connectorId, String name, boolean indexed, boolean daemon) {
        return threadFactory(connector, connectorId, name, indexed, daemon, (Consumer) null);
    }

    public static ThreadFactory threadFactory(final Class<? extends SourceConnector> connector, final String connectorId, final String name, final boolean indexed, final boolean daemon, final Consumer<Thread> callback) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Requested thread factory for connector {}, id = {} named = {}", new Object[]{connector.getSimpleName(), connectorId, name});
        }

        return new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                StringBuilder threadName = (new StringBuilder("debezium-")).append(connector.getSimpleName().toLowerCase()).append('-').append(connectorId).append('-').append(name);
                if (indexed) {
                    threadName.append('-').append(this.index.getAndIncrement());
                }

                Threads.LOGGER.info("Creating thread {}", threadName);
                Thread t = new Thread(r, threadName.toString());
                t.setDaemon(daemon);
                if (callback != null) {
                    callback.accept(t);
                }

                return t;
            }
        };
    }

    public static ExecutorService newSingleThreadExecutor(Class<? extends SourceConnector> connector, String connectorId, String name, boolean daemon) {
        return Executors.newSingleThreadExecutor(threadFactory(connector, connectorId, name, false, daemon));
    }

    public static ExecutorService newFixedThreadPool(Class<? extends SourceConnector> connector, String connectorId, String name, int threadCount) {
        return Executors.newFixedThreadPool(threadCount, threadFactory(connector, connectorId, name, true, false));
    }

    public static ExecutorService newSingleThreadExecutor(Class<? extends SourceConnector> connector, String connectorId, String name) {
        return newSingleThreadExecutor(connector, connectorId, name, false);
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(Class<? extends SourceConnector> connector, String connectorId, String name, boolean daemon) {
        return Executors.newSingleThreadScheduledExecutor(threadFactory(connector, connectorId, name, false, daemon));
    }

    public interface TimeSince {
        void reset();

        long elapsedTime();
    }

    public interface Timer {
        boolean expired();

        Duration remaining();
    }
}
