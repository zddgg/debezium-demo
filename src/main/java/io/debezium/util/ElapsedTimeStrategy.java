package io.debezium.util;

import java.time.Duration;
import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface ElapsedTimeStrategy {
    boolean hasElapsed();

    static ElapsedTimeStrategy none() {
        return () -> {
            return true;
        };
    }

    static ElapsedTimeStrategy constant(final Clock clock, final long delayInMilliseconds) {
        if (delayInMilliseconds <= 0L) {
            throw new IllegalArgumentException("Initial delay must be positive");
        } else {
            return new ElapsedTimeStrategy() {
                private long nextTimestamp = 0L;

                public boolean hasElapsed() {
                    if (this.nextTimestamp == 0L) {
                        this.nextTimestamp = clock.currentTimeInMillis() + delayInMilliseconds;
                        return true;
                    } else {
                        long current = clock.currentTimeInMillis();
                        if (current < this.nextTimestamp) {
                            return false;
                        } else {
                            do {
                                long multiple = 1L + (current - this.nextTimestamp) / delayInMilliseconds;
                                this.nextTimestamp += multiple * delayInMilliseconds;
                            } while (current > this.nextTimestamp);

                            return true;
                        }
                    }
                }
            };
        }
    }

    static ElapsedTimeStrategy constant(Clock clock, Duration delay) {
        return constant(clock, delay.toMillis());
    }

    static ElapsedTimeStrategy step(final Clock clock, Duration preStepDelay, final BooleanSupplier stepFunction, Duration postStepDelay) {
        final long preStepDelayinMillis = preStepDelay.toMillis();
        final long postStepDelayinMillis = postStepDelay.toMillis();
        if (preStepDelayinMillis <= 0L) {
            throw new IllegalArgumentException("Pre-step delay must be positive");
        } else if (postStepDelayinMillis <= 0L) {
            throw new IllegalArgumentException("Post-step delay must be positive");
        } else {
            return new ElapsedTimeStrategy() {
                private long nextTimestamp = 0L;
                private boolean elapsed = false;
                private long delta = 0L;

                public boolean hasElapsed() {
                    if (this.nextTimestamp == 0L) {
                        this.elapsed = stepFunction.getAsBoolean();
                        this.delta = this.elapsed ? postStepDelayinMillis : preStepDelayinMillis;
                        this.nextTimestamp = clock.currentTimeInMillis() + this.delta;
                        return true;
                    } else {
                        if (!this.elapsed) {
                            this.elapsed = stepFunction.getAsBoolean();
                            if (this.elapsed) {
                                this.delta = postStepDelayinMillis;
                            }
                        }

                        long current = clock.currentTimeInMillis();
                        if (current < this.nextTimestamp) {
                            return false;
                        } else {
                            do {
                                assert this.delta > 0L;

                                long multiple = 1L + (current - this.nextTimestamp) / this.delta;
                                this.nextTimestamp += multiple * this.delta;
                            } while (this.nextTimestamp <= current);

                            return true;
                        }
                    }
                }
            };
        }
    }

    static ElapsedTimeStrategy linear(final Clock clock, Duration delay) {
        final long delayInMilliseconds = delay.toMillis();
        if (delayInMilliseconds <= 0L) {
            throw new IllegalArgumentException("Initial delay must be positive");
        } else {
            return new ElapsedTimeStrategy() {
                private long nextTimestamp = 0L;
                private long counter = 1L;

                public boolean hasElapsed() {
                    if (this.nextTimestamp == 0L) {
                        this.nextTimestamp = clock.currentTimeInMillis() + delayInMilliseconds;
                        this.counter = 1L;
                        return true;
                    } else {
                        long current = clock.currentTimeInMillis();
                        if (current < this.nextTimestamp) {
                            return false;
                        } else {
                            do {
                                if (this.counter < Long.MAX_VALUE) {
                                    ++this.counter;
                                }

                                this.nextTimestamp += delayInMilliseconds * this.counter;
                            } while (this.nextTimestamp <= current);

                            return true;
                        }
                    }
                }
            };
        }
    }

    static ElapsedTimeStrategy exponential(Clock clock, Duration initialDelay, Duration maxDelay) {
        return exponential(clock, initialDelay.toMillis(), maxDelay.toMillis(), 2.0);
    }

    static ElapsedTimeStrategy exponential(final Clock clock, final long initialDelayInMilliseconds, final long maxDelayInMilliseconds, final double multiplier) {
        if (multiplier <= 1.0) {
            throw new IllegalArgumentException("Multiplier must be greater than 1");
        } else if (initialDelayInMilliseconds <= 0L) {
            throw new IllegalArgumentException("Initial delay must be positive");
        } else if (initialDelayInMilliseconds >= maxDelayInMilliseconds) {
            throw new IllegalArgumentException("Maximum delay must be greater than initial delay");
        } else {
            return new ElapsedTimeStrategy() {
                private long nextTimestamp = 0L;
                private long previousDelay = 0L;

                public boolean hasElapsed() {
                    if (this.nextTimestamp == 0L) {
                        this.nextTimestamp = clock.currentTimeInMillis() + initialDelayInMilliseconds;
                        this.previousDelay = initialDelayInMilliseconds;
                        return true;
                    } else {
                        long current = clock.currentTimeInMillis();
                        if (current >= this.nextTimestamp) {
                            do {
                                long nextDelay = (long) ((double) this.previousDelay * multiplier);
                                if (nextDelay >= maxDelayInMilliseconds) {
                                    this.previousDelay = maxDelayInMilliseconds;
                                    if (this.nextTimestamp < current) {
                                        long multiple = 1L + (current - this.nextTimestamp) / maxDelayInMilliseconds;
                                        this.nextTimestamp += multiple * maxDelayInMilliseconds;
                                    }
                                } else {
                                    this.previousDelay = nextDelay;
                                }

                                this.nextTimestamp += this.previousDelay;
                            } while (this.nextTimestamp <= current);

                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            };
        }
    }
}
