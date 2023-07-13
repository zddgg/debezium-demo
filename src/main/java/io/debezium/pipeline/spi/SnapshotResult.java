package io.debezium.pipeline.spi;

public class SnapshotResult<O extends OffsetContext> {
    private final SnapshotResultStatus status;
    private final O offset;

    private SnapshotResult(SnapshotResultStatus status, O offset) {
        this.status = status;
        this.offset = offset;
    }

    public static <O extends OffsetContext> SnapshotResult<O> completed(O offset) {
        return new SnapshotResult(SnapshotResultStatus.COMPLETED, offset);
    }

    public static <O extends OffsetContext> SnapshotResult<O> aborted() {
        return new SnapshotResult(SnapshotResultStatus.ABORTED, (OffsetContext) null);
    }

    public static <O extends OffsetContext> SnapshotResult<O> skipped(O offset) {
        return new SnapshotResult(SnapshotResultStatus.SKIPPED, offset);
    }

    public boolean isCompletedOrSkipped() {
        return this.status == SnapshotResultStatus.SKIPPED || this.status == SnapshotResultStatus.COMPLETED;
    }

    public SnapshotResultStatus getStatus() {
        return this.status;
    }

    public O getOffset() {
        return this.offset;
    }

    public String toString() {
        return "SnapshotResult [status=" + this.status + ", offset=" + this.offset + "]";
    }

    public static enum SnapshotResultStatus {
        COMPLETED,
        ABORTED,
        SKIPPED;

        // $FF: synthetic method
        private static SnapshotResultStatus[] $values() {
            return new SnapshotResultStatus[]{COMPLETED, ABORTED, SKIPPED};
        }
    }
}
