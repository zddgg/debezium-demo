package io.debezium.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@FunctionalInterface
public interface Metronome {
    void pause() throws InterruptedException;

    static Metronome sleeper(Duration period, final Clock timeSystem) {
        final long periodInMillis = period.toMillis();
        return new Metronome() {
            private long next = timeSystem.currentTimeInMillis() + periodInMillis;

            public void pause() throws InterruptedException {
                while (true) {
                    long now = timeSystem.currentTimeInMillis();
                    if (this.next <= now) {
                        this.next += periodInMillis;
                        return;
                    }

                    Thread.sleep(this.next - now);
                }
            }

            public String toString() {
                return "Metronome (sleep for " + periodInMillis + " ms)";
            }
        };
    }

    static Metronome parker(Duration period, final Clock timeSystem) {
        final long periodInNanos = period.toNanos();
        return new Metronome() {
            private long next = timeSystem.currentTimeInNanos() + periodInNanos;

            public void pause() throws InterruptedException {
                while (true) {
                    if (this.next > timeSystem.currentTimeInNanos()) {
                        LockSupport.parkNanos(this.next - timeSystem.currentTimeInNanos());
                        if (!Thread.currentThread().isInterrupted()) {
                            continue;
                        }

                        throw new InterruptedException();
                    }

                    this.next += periodInNanos;
                    return;
                }
            }

            public String toString() {
                return "Metronome (park for " + TimeUnit.NANOSECONDS.toMillis(periodInNanos) + " ms)";
            }
        };
    }
}
