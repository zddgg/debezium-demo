package io.debezium.util;

import java.time.Duration;
import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface DelayStrategy {
    default boolean sleepWhen(BooleanSupplier criteria) {
        return this.sleepWhen(criteria.getAsBoolean());
    }

    boolean sleepWhen(boolean var1);

    static DelayStrategy none() {
        return (criteria) -> {
            return false;
        };
    }

    static DelayStrategy constant(Duration delay) {
        long delayInMilliseconds = delay.toMillis();
        return (criteria) -> {
            if (!criteria) {
                return false;
            } else {
                try {
                    Thread.sleep(delayInMilliseconds);
                } catch (InterruptedException var4) {
                    Thread.currentThread().interrupt();
                }

                return true;
            }
        };
    }

    static DelayStrategy linear(Duration delay) {
        final long delayInMilliseconds = delay.toMillis();
        if (delayInMilliseconds <= 0L) {
            throw new IllegalArgumentException("Initial delay must be positive");
        } else {
            return new DelayStrategy() {
                private long misses = 0L;

                public boolean sleepWhen(boolean criteria) {
                    if (!criteria) {
                        this.misses = 0L;
                        return false;
                    } else {
                        ++this.misses;

                        try {
                            Thread.sleep(this.misses * delayInMilliseconds);
                        } catch (InterruptedException var3) {
                            Thread.currentThread().interrupt();
                        }

                        return true;
                    }
                }
            };
        }
    }

    static DelayStrategy exponential(Duration initialDelay, Duration maxDelay) {
        return exponential(initialDelay, maxDelay, 2.0);
    }

    static DelayStrategy exponential(Duration initialDelay, Duration maxDelay, final double backOffMultiplier) {
        final long initialDelayInMilliseconds = initialDelay.toMillis();
        final long maxDelayInMilliseconds = maxDelay.toMillis();
        if (backOffMultiplier <= 1.0) {
            throw new IllegalArgumentException("Backup multiplier must be greater than 1");
        } else if (initialDelayInMilliseconds <= 0L) {
            throw new IllegalArgumentException("Initial delay must be positive");
        } else if (initialDelayInMilliseconds >= maxDelayInMilliseconds) {
            throw new IllegalArgumentException("Maximum delay must be greater than initial delay");
        } else {
            return new DelayStrategy() {
                private long previousDelay = 0L;

                public boolean sleepWhen(boolean criteria) {
                    if (!criteria) {
                        this.previousDelay = 0L;
                        return false;
                    } else {
                        if (this.previousDelay == 0L) {
                            this.previousDelay = initialDelayInMilliseconds;
                        } else {
                            long nextDelay = (long) ((double) this.previousDelay * backOffMultiplier);
                            this.previousDelay = Math.min(nextDelay, maxDelayInMilliseconds);
                        }

                        try {
                            Thread.sleep(this.previousDelay);
                        } catch (InterruptedException var4) {
                            Thread.currentThread().interrupt();
                        }

                        return true;
                    }
                }
            };
        }
    }
}
