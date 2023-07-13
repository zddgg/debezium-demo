package io.debezium.connector.mysql;

import io.debezium.document.Document;
import io.debezium.relational.history.HistoryRecordComparator;

import java.util.function.Predicate;

final class MySqlHistoryRecordComparator extends HistoryRecordComparator {
    private final Predicate<String> gtidSourceFilter;

    MySqlHistoryRecordComparator(Predicate<String> gtidSourceFilter) {
        this.gtidSourceFilter = gtidSourceFilter;
    }

    protected boolean isPositionAtOrBefore(Document recorded, Document desired) {
        String recordedGtidSetStr = recorded.getString("gtids");
        String desiredGtidSetStr = desired.getString("gtids");
        int diff;
        if (desiredGtidSetStr != null) {
            if (recordedGtidSetStr != null) {
                GtidSet recordedGtidSet = new GtidSet(recordedGtidSetStr);
                GtidSet desiredGtidSet = new GtidSet(desiredGtidSetStr);
                if (this.gtidSourceFilter != null) {
                    recordedGtidSet = recordedGtidSet.retainAll(this.gtidSourceFilter);
                    desiredGtidSet = desiredGtidSet.retainAll(this.gtidSourceFilter);
                }

                if (recordedGtidSet.equals(desiredGtidSet)) {
                    if (!recorded.has("snapshot") && desired.has("snapshot")) {
                        return false;
                    } else {
                        int recordedEventCount = recorded.getInteger("event", 0);
                        int desiredEventCount = desired.getInteger("event", 0);
                        diff = recordedEventCount - desiredEventCount;
                        return diff <= 0;
                    }
                } else {
                    return recordedGtidSet.isContainedWithin(desiredGtidSet);
                }
            } else {
                return true;
            }
        } else if (recordedGtidSetStr != null) {
            return false;
        } else {
            int recordedServerId = recorded.getInteger("server_id", 0);
            int desiredServerId = recorded.getInteger("server_id", 0);
            if (recordedServerId != desiredServerId) {
                long recordedTimestamp = recorded.getLong("ts_ms", 0L);
                long desiredTimestamp = recorded.getLong("ts_ms", 0L);
                return recordedTimestamp <= desiredTimestamp;
            } else {
                BinlogFilename recordedFilename = BinlogFilename.of(recorded.getString("file"));
                BinlogFilename desiredFilename = BinlogFilename.of(desired.getString("file"));
                diff = recordedFilename.compareTo(desiredFilename);
                if (diff > 0) {
                    return false;
                } else if (diff < 0) {
                    return true;
                } else {
                    int recordedPosition = recorded.getInteger("pos", -1);
                    int desiredPosition = desired.getInteger("pos", -1);
                    diff = recordedPosition - desiredPosition;
                    if (diff > 0) {
                        return false;
                    } else if (diff < 0) {
                        return true;
                    } else {
                        int recordedEventCount = recorded.getInteger("event", 0);
                        int desiredEventCount = desired.getInteger("event", 0);
                        diff = recordedEventCount - desiredEventCount;
                        if (diff > 0) {
                            return false;
                        } else if (diff < 0) {
                            return true;
                        } else {
                            int recordedRow = recorded.getInteger("row", -1);
                            int desiredRow = desired.getInteger("row", -1);
                            diff = recordedRow - desiredRow;
                            return diff <= 0;
                        }
                    }
                }
            }
        }
    }

    private static class BinlogFilename implements Comparable<BinlogFilename> {
        private final String baseName;
        private final long extension;

        private BinlogFilename(String baseName, long extension) {
            this.baseName = baseName;
            this.extension = extension;
        }

        public static BinlogFilename of(String filename) {
            int index = filename.lastIndexOf(".");
            if (index == -1) {
                throw new IllegalArgumentException("Filename does not have an extension: " + filename);
            } else {
                String baseFilename = filename.substring(0, index);
                String stringExtension = filename.substring(index + 1);

                long extension;
                try {
                    extension = Long.parseLong(stringExtension);
                } catch (NumberFormatException var7) {
                    throw new IllegalArgumentException("Can't parse binlog filename extension: " + filename, var7);
                }

                return new BinlogFilename(baseFilename, extension);
            }
        }

        public String toString() {
            return "BinlogFilename [baseName=" + this.baseName + ", extension=" + this.extension + "]";
        }

        public int compareTo(BinlogFilename other) {
            if (!this.baseName.equals(other.baseName)) {
                throw new IllegalArgumentException("Can't compare binlog filenames with different base names");
            } else {
                return Long.compare(this.extension, other.extension);
            }
        }
    }
}
