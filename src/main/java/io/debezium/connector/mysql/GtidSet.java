package io.debezium.connector.mysql;

import io.debezium.annotation.Immutable;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Immutable
public final class GtidSet {
    private final Map<String, UUIDSet> uuidSetsByServerId = new TreeMap();
    public static Pattern GTID_DELIMITER = Pattern.compile(":");

    protected GtidSet(Map<String, UUIDSet> uuidSetsByServerId) {
        this.uuidSetsByServerId.putAll(uuidSetsByServerId);
    }

    public GtidSet(String gtids) {
        gtids = gtids.replace("\n", "").replace("\r", "");
        (new com.github.shyiko.mysql.binlog.GtidSet(gtids)).getUUIDSets().forEach((uuidSet) -> {
            this.uuidSetsByServerId.put(uuidSet.getUUID(), new UUIDSet(uuidSet));
        });
        StringBuilder sb = new StringBuilder();
        this.uuidSetsByServerId.values().forEach((uuidSet) -> {
            if (sb.length() != 0) {
                sb.append(',');
            }

            sb.append(uuidSet.toString());
        });
    }

    public GtidSet retainAll(Predicate<String> sourceFilter) {
        if (sourceFilter == null) {
            return this;
        } else {
            Map<String, UUIDSet> newSets = (Map) this.uuidSetsByServerId.entrySet().stream().filter((entry) -> {
                return sourceFilter.test((String) entry.getKey());
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new GtidSet(newSets);
        }
    }

    public Collection<UUIDSet> getUUIDSets() {
        return Collections.unmodifiableCollection(this.uuidSetsByServerId.values());
    }

    public UUIDSet forServerWithId(String uuid) {
        return (UUIDSet) this.uuidSetsByServerId.get(uuid);
    }

    public boolean isContainedWithin(GtidSet other) {
        if (other == null) {
            return false;
        } else if (this.equals(other)) {
            return true;
        } else {
            Iterator var2 = this.uuidSetsByServerId.values().iterator();

            UUIDSet uuidSet;
            UUIDSet thatSet;
            do {
                if (!var2.hasNext()) {
                    return true;
                }

                uuidSet = (UUIDSet) var2.next();
                thatSet = other.forServerWithId(uuidSet.getUUID());
            } while (uuidSet.isContainedWithin(thatSet));

            return false;
        }
    }

    public GtidSet with(GtidSet other) {
        if (other != null && !other.uuidSetsByServerId.isEmpty()) {
            Map<String, UUIDSet> newSet = new HashMap();
            newSet.putAll(this.uuidSetsByServerId);
            newSet.putAll(other.uuidSetsByServerId);
            return new GtidSet(newSet);
        } else {
            return this;
        }
    }

    public GtidSet getGtidSetBeginning() {
        Map<String, UUIDSet> newSet = new HashMap();
        Iterator var2 = this.uuidSetsByServerId.values().iterator();

        while (var2.hasNext()) {
            UUIDSet uuidSet = (UUIDSet) var2.next();
            newSet.put(uuidSet.getUUID(), uuidSet.asIntervalBeginning());
        }

        return new GtidSet(newSet);
    }

    public boolean contains(String gtid) {
        String[] split = GTID_DELIMITER.split(gtid);
        String sourceId = split[0];
        UUIDSet uuidSet = this.forServerWithId(sourceId);
        if (uuidSet == null) {
            return false;
        } else {
            long transactionId = Long.parseLong(split[1]);
            return uuidSet.contains(transactionId);
        }
    }

    public GtidSet subtract(GtidSet other) {
        if (other == null) {
            return this;
        } else {
            Map<String, UUIDSet> newSets = (Map) this.uuidSetsByServerId.entrySet().stream().filter((entry) -> {
                return !((UUIDSet) entry.getValue()).isContainedWithin(other.forServerWithId((String) entry.getKey()));
            }).map((entry) -> {
                return new AbstractMap.SimpleEntry((String) entry.getKey(), ((UUIDSet) entry.getValue()).subtract(other.forServerWithId((String) entry.getKey())));
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new GtidSet(newSets);
        }
    }

    public int hashCode() {
        return this.uuidSetsByServerId.keySet().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof GtidSet) {
            GtidSet that = (GtidSet) obj;
            return this.uuidSetsByServerId.equals(that.uuidSetsByServerId);
        } else {
            return false;
        }
    }

    public String toString() {
        List<String> gtids = new ArrayList();
        Iterator var2 = this.uuidSetsByServerId.values().iterator();

        while (var2.hasNext()) {
            UUIDSet uuidSet = (UUIDSet) var2.next();
            gtids.add(uuidSet.toString());
        }

        return String.join(",", gtids);
    }

    @Immutable
    public static class UUIDSet {
        private final String uuid;
        private final LinkedList<Interval> intervals = new LinkedList();

        protected UUIDSet(com.github.shyiko.mysql.binlog.GtidSet.UUIDSet uuidSet) {
            this.uuid = uuidSet.getUUID();
            uuidSet.getIntervals().forEach((interval) -> {
                this.intervals.add(new Interval(interval.getStart(), interval.getEnd()));
            });
            Collections.sort(this.intervals);
            if (this.intervals.size() > 1) {
                for (int i = this.intervals.size() - 1; i != 0; --i) {
                    Interval before = (Interval) this.intervals.get(i - 1);
                    Interval after = (Interval) this.intervals.get(i);
                    if (before.getEnd() + 1L == after.getStart()) {
                        this.intervals.set(i - 1, new Interval(before.getStart(), after.getEnd()));
                        this.intervals.remove(i);
                    }
                }
            }

        }

        protected UUIDSet(String uuid, Interval interval) {
            this.uuid = uuid;
            this.intervals.add(interval);
        }

        protected UUIDSet(String uuid, List<Interval> intervals) {
            this.uuid = uuid;
            this.intervals.addAll(intervals);
        }

        public UUIDSet asIntervalBeginning() {
            Interval start = new Interval(((Interval) this.intervals.get(0)).getStart(), ((Interval) this.intervals.get(0)).getStart());
            return new UUIDSet(this.uuid, start);
        }

        public String getUUID() {
            return this.uuid;
        }

        public List<Interval> getIntervals() {
            return Collections.unmodifiableList(this.intervals);
        }

        public boolean isContainedWithin(UUIDSet other) {
            if (other == null) {
                return false;
            } else if (!this.getUUID().equalsIgnoreCase(other.getUUID())) {
                return false;
            } else if (this.intervals.isEmpty()) {
                return true;
            } else if (other.intervals.isEmpty()) {
                return false;
            } else {
                assert this.intervals.size() > 0;

                assert other.intervals.size() > 0;

                Iterator var2 = this.intervals.iterator();

                boolean found;
                do {
                    if (!var2.hasNext()) {
                        return true;
                    }

                    Interval thisInterval = (Interval) var2.next();
                    found = false;
                    Iterator var5 = other.intervals.iterator();

                    while (var5.hasNext()) {
                        Interval otherInterval = (Interval) var5.next();
                        if (thisInterval.isContainedWithin(otherInterval)) {
                            found = true;
                            break;
                        }
                    }
                } while (found);

                return false;
            }
        }

        public boolean contains(long transactionId) {
            Iterator var3 = this.intervals.iterator();

            Interval interval;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                interval = (Interval) var3.next();
            } while (!interval.contains(transactionId));

            return true;
        }

        public int hashCode() {
            return this.uuid.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof UUIDSet)) {
                return super.equals(obj);
            } else {
                UUIDSet that = (UUIDSet) obj;
                return this.getUUID().equalsIgnoreCase(that.getUUID()) && this.getIntervals().equals(that.getIntervals());
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.uuid).append(':');
            Iterator<Interval> iter = this.intervals.iterator();
            if (iter.hasNext()) {
                sb.append(iter.next());
            }

            while (iter.hasNext()) {
                sb.append(':');
                sb.append(iter.next());
            }

            return sb.toString();
        }

        public UUIDSet subtract(UUIDSet other) {
            if (!this.uuid.equals(other.getUUID())) {
                throw new IllegalArgumentException("UUIDSet subtraction is supported only within a single server UUID");
            } else {
                List<Interval> result = new ArrayList();
                Iterator var3 = this.intervals.iterator();

                while (var3.hasNext()) {
                    Interval interval = (Interval) var3.next();
                    result.addAll(interval.removeAll(other.getIntervals()));
                }

                return new UUIDSet(this.uuid, result);
            }
        }
    }

    @Immutable
    public static class Interval implements Comparable<Interval> {
        private final long start;
        private final long end;

        public Interval(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return this.start;
        }

        public long getEnd() {
            return this.end;
        }

        public boolean isContainedWithin(Interval other) {
            if (other == this) {
                return true;
            } else if (other == null) {
                return false;
            } else {
                return this.getStart() >= other.getStart() && this.getEnd() <= other.getEnd();
            }
        }

        public boolean contains(long transactionId) {
            return this.getStart() <= transactionId && transactionId <= this.getEnd();
        }

        public boolean contains(Interval other) {
            return this.getStart() <= other.getStart() && this.getEnd() >= other.getEnd();
        }

        public boolean nonintersecting(Interval other) {
            return other.getEnd() < this.getStart() || other.getStart() > this.getEnd();
        }

        public List<Interval> remove(Interval other) {
            if (this.nonintersecting(other)) {
                return Collections.singletonList(this);
            } else if (other.contains(this)) {
                return Collections.emptyList();
            } else {
                List<Interval> result = new LinkedList();
                Interval part;
                if (this.getStart() < other.getStart()) {
                    part = new Interval(this.getStart(), other.getStart() - 1L);
                    result.add(part);
                }

                if (other.getEnd() < this.getEnd()) {
                    part = new Interval(other.getEnd() + 1L, this.getEnd());
                    result.add(part);
                }

                return result;
            }
        }

        public List<Interval> removeAll(List<Interval> otherIntervals) {
            List<Interval> thisIntervals = new LinkedList();
            thisIntervals.add(this);
            List<Interval> result = new LinkedList();
            result.add(this);

            for (Iterator var4 = otherIntervals.iterator(); var4.hasNext(); thisIntervals = result) {
                Interval other = (Interval) var4.next();
                result = new LinkedList();
                Iterator var6 = thisIntervals.iterator();

                while (var6.hasNext()) {
                    Interval thisInterval = (Interval) var6.next();
                    result.addAll(thisInterval.remove(other));
                }
            }

            return result;
        }

        public int compareTo(Interval that) {
            if (that == this) {
                return 0;
            } else {
                long diff = this.start - that.start;
                if (diff > 2147483647L) {
                    return Integer.MAX_VALUE;
                } else {
                    return diff < -2147483648L ? Integer.MIN_VALUE : (int) diff;
                }
            }
        }

        public int hashCode() {
            return (int) this.getStart();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof Interval)) {
                return false;
            } else {
                Interval that = (Interval) obj;
                return this.getStart() == that.getStart() && this.getEnd() == that.getEnd();
            }
        }

        public String toString() {
            long var10000 = this.getStart();
            return "" + var10000 + "-" + this.getEnd();
        }
    }
}
