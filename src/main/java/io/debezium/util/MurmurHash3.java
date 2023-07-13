package io.debezium.util;

public class MurmurHash3 {
    private static final MurmurHash3 instance = new MurmurHash3();
    public static final byte INVALID_CHAR = 63;

    public static MurmurHash3 getInstance() {
        return instance;
    }

    private MurmurHash3() {
    }

    static long getblock(byte[] key, int i) {
        return (long) key[i + 0] & 255L | ((long) key[i + 1] & 255L) << 8 | ((long) key[i + 2] & 255L) << 16 | ((long) key[i + 3] & 255L) << 24 | ((long) key[i + 4] & 255L) << 32 | ((long) key[i + 5] & 255L) << 40 | ((long) key[i + 6] & 255L) << 48 | ((long) key[i + 7] & 255L) << 56;
    }

    static void bmix(State state) {
        state.k1 *= state.c1;
        state.k1 = state.k1 << 23 | state.k1 >>> 41;
        state.k1 *= state.c2;
        state.h1 ^= state.k1;
        state.h1 += state.h2;
        state.h2 = state.h2 << 41 | state.h2 >>> 23;
        state.k2 *= state.c2;
        state.k2 = state.k2 << 23 | state.k2 >>> 41;
        state.k2 *= state.c1;
        state.h2 ^= state.k2;
        state.h2 += state.h1;
        state.h1 = state.h1 * 3L + 1390208809L;
        state.h2 = state.h2 * 3L + 944331445L;
        state.c1 = state.c1 * 5L + 2071795100L;
        state.c2 = state.c2 * 5L + 1808688022L;
    }

    static long fmix(long k) {
        k ^= k >>> 33;
        k *= -49064778989728563L;
        k ^= k >>> 33;
        k *= -4265267296055464877L;
        k ^= k >>> 33;
        return k;
    }

    public static long MurmurHash3_x64_64(byte[] key, int seed) {
        State state = new State();
        state.h1 = -7824752305899900300L ^ (long) seed;
        state.h2 = 6371974587529090045L ^ (long) seed;
        state.c1 = -8663945395140668459L;
        state.c2 = 5545529020109919103L;

        int tail;
        for (tail = 0; tail < key.length / 16; ++tail) {
            state.k1 = getblock(key, tail * 2 * 8);
            state.k2 = getblock(key, (tail * 2 + 1) * 8);
            bmix(state);
        }

        state.k1 = 0L;
        state.k2 = 0L;
        tail = key.length >>> 4 << 4;
        switch (key.length & 15) {
            case 15:
                state.k2 ^= (long) key[tail + 14] << 48;
            case 14:
                state.k2 ^= (long) key[tail + 13] << 40;
            case 13:
                state.k2 ^= (long) key[tail + 12] << 32;
            case 12:
                state.k2 ^= (long) key[tail + 11] << 24;
            case 11:
                state.k2 ^= (long) key[tail + 10] << 16;
            case 10:
                state.k2 ^= (long) key[tail + 9] << 8;
            case 9:
                state.k2 ^= (long) key[tail + 8];
            case 8:
                state.k1 ^= (long) key[tail + 7] << 56;
            case 7:
                state.k1 ^= (long) key[tail + 6] << 48;
            case 6:
                state.k1 ^= (long) key[tail + 5] << 40;
            case 5:
                state.k1 ^= (long) key[tail + 4] << 32;
            case 4:
                state.k1 ^= (long) key[tail + 3] << 24;
            case 3:
                state.k1 ^= (long) key[tail + 2] << 16;
            case 2:
                state.k1 ^= (long) key[tail + 1] << 8;
            case 1:
                state.k1 ^= (long) key[tail + 0];
                bmix(state);
            default:
                state.h2 ^= (long) key.length;
                state.h1 += state.h2;
                state.h2 += state.h1;
                state.h1 = fmix(state.h1);
                state.h2 = fmix(state.h2);
                state.h1 += state.h2;
                state.h2 += state.h1;
                return state.h1;
        }
    }

    public static int MurmurHash3_x64_32(byte[] key, int seed) {
        return (int) (MurmurHash3_x64_64(key, seed) >>> 32);
    }

    public static long MurmurHash3_x64_64(long[] key, int seed) {
        State state = new State();
        state.h1 = -7824752305899900300L ^ (long) seed;
        state.h2 = 6371974587529090045L ^ (long) seed;
        state.c1 = -8663945395140668459L;
        state.c2 = 5545529020109919103L;

        for (int i = 0; i < key.length / 2; ++i) {
            state.k1 = key[i * 2];
            state.k2 = key[i * 2 + 1];
            bmix(state);
        }

        long tail = key[key.length - 1];
        if (key.length % 2 != 0) {
            state.k1 ^= tail;
            bmix(state);
        }

        state.h2 ^= (long) (key.length * 8);
        state.h1 += state.h2;
        state.h2 += state.h1;
        state.h1 = fmix(state.h1);
        state.h2 = fmix(state.h2);
        state.h1 += state.h2;
        state.h2 += state.h1;
        return state.h1;
    }

    public static int MurmurHash3_x64_32(long[] key, int seed) {
        return (int) (MurmurHash3_x64_64(key, seed) >>> 32);
    }

    public int hash(byte[] payload) {
        return MurmurHash3_x64_32((byte[]) payload, 9001);
    }

    public static int hash(long[] payload) {
        return MurmurHash3_x64_32((long[]) payload, 9001);
    }

    public int hash(int hashcode) {
        byte b0 = (byte) hashcode;
        byte b1 = (byte) (hashcode >>> 8);
        byte b2 = (byte) (hashcode >>> 16);
        byte b3 = (byte) (hashcode >>> 24);
        State state = new State();
        state.h1 = -7824752305899908771L;
        state.h2 = 6371974587529097428L;
        state.c1 = -8663945395140668459L;
        state.c2 = 5545529020109919103L;
        state.k1 = 0L;
        state.k2 = 0L;
        state.k1 ^= (long) b3 << 24;
        state.k1 ^= (long) b2 << 16;
        state.k1 ^= (long) b1 << 8;
        state.k1 ^= (long) b0;
        bmix(state);
        state.h2 ^= 4L;
        state.h1 += state.h2;
        state.h2 += state.h1;
        state.h1 = fmix(state.h1);
        state.h2 = fmix(state.h2);
        state.h1 += state.h2;
        state.h2 += state.h1;
        return (int) (state.h1 >>> 32);
    }

    public int hash(Object o) {
        if (o instanceof byte[]) {
            return this.hash((byte[]) o);
        } else if (o instanceof long[]) {
            return hash((long[]) o);
        } else {
            return o instanceof String ? this.hashString((String) o) : this.hash(o.hashCode());
        }
    }

    private int hashString(String s) {
        return (int) (this.MurmurHash3_x64_64_String(s, 9001L) >> 32);
    }

    private long MurmurHash3_x64_64_String(String s, long seed) {
        State state = new State();
        state.h1 = -7824752305899900300L ^ seed;
        state.h2 = 6371974587529090045L ^ seed;
        state.c1 = -8663945395140668459L;
        state.c2 = 5545529020109919103L;
        int byteLen = 0;
        int stringLen = s.length();

        for (int i = 0; i < stringLen; ++i) {
            char c1 = s.charAt(i);
            int cp;
            if (!Character.isSurrogate(c1)) {
                cp = c1;
            } else if (Character.isHighSurrogate(c1)) {
                if (i + 1 < stringLen) {
                    char c2 = s.charAt(i + 1);
                    if (Character.isLowSurrogate(c2)) {
                        ++i;
                        cp = Character.toCodePoint(c1, c2);
                    } else {
                        cp = 63;
                    }
                } else {
                    cp = 63;
                }
            } else {
                cp = 63;
            }

            if (cp <= 127) {
                this.addByte(state, (byte) cp, byteLen++);
            } else {
                byte b2;
                byte b1;
                if (cp <= 2047) {
                    b1 = (byte) (192 | 31 & cp >> 6);
                    b2 = (byte) (128 | 63 & cp);
                    this.addByte(state, b1, byteLen++);
                    this.addByte(state, b2, byteLen++);
                } else {
                    byte b3;
                    if (cp <= 65535) {
                        b1 = (byte) (224 | 15 & cp >> 12);
                        b2 = (byte) (128 | 63 & cp >> 6);
                        b3 = (byte) (128 | 63 & cp);
                        this.addByte(state, b1, byteLen++);
                        this.addByte(state, b2, byteLen++);
                        this.addByte(state, b3, byteLen++);
                    } else {
                        b1 = (byte) (240 | 7 & cp >> 18);
                        b2 = (byte) (128 | 63 & cp >> 12);
                        b3 = (byte) (128 | 63 & cp >> 6);
                        byte b4 = (byte) (128 | 63 & cp);
                        this.addByte(state, b1, byteLen++);
                        this.addByte(state, b2, byteLen++);
                        this.addByte(state, b3, byteLen++);
                        this.addByte(state, b4, byteLen++);
                    }
                }
            }
        }

        long savedK1 = state.k1;
        long savedK2 = state.k2;
        state.k1 = 0L;
        state.k2 = 0L;
        switch (byteLen & 15) {
            case 15:
                state.k2 ^= (long) ((byte) ((int) (savedK2 >> 48))) << 48;
            case 14:
                state.k2 ^= (long) ((byte) ((int) (savedK2 >> 40))) << 40;
            case 13:
                state.k2 ^= (long) ((byte) ((int) (savedK2 >> 32))) << 32;
            case 12:
                state.k2 ^= (long) ((byte) ((int) (savedK2 >> 24))) << 24;
            case 11:
                state.k2 ^= (long) ((byte) ((int) (savedK2 >> 16))) << 16;
            case 10:
                state.k2 ^= (long) ((byte) ((int) (savedK2 >> 8))) << 8;
            case 9:
                state.k2 ^= (long) ((byte) ((int) savedK2));
            case 8:
                state.k1 ^= (long) ((byte) ((int) (savedK1 >> 56))) << 56;
            case 7:
                state.k1 ^= (long) ((byte) ((int) (savedK1 >> 48))) << 48;
            case 6:
                state.k1 ^= (long) ((byte) ((int) (savedK1 >> 40))) << 40;
            case 5:
                state.k1 ^= (long) ((byte) ((int) (savedK1 >> 32))) << 32;
            case 4:
                state.k1 ^= (long) ((byte) ((int) (savedK1 >> 24))) << 24;
            case 3:
                state.k1 ^= (long) ((byte) ((int) (savedK1 >> 16))) << 16;
            case 2:
                state.k1 ^= (long) ((byte) ((int) (savedK1 >> 8))) << 8;
            case 1:
                state.k1 ^= (long) ((byte) ((int) savedK1));
                bmix(state);
            default:
                state.h2 ^= (long) byteLen;
                state.h1 += state.h2;
                state.h2 += state.h1;
                state.h1 = fmix(state.h1);
                state.h2 = fmix(state.h2);
                state.h1 += state.h2;
                state.h2 += state.h1;
                return state.h1;
        }
    }

    private void addByte(State state, byte b, int len) {
        int shift = (len & 7) * 8;
        long bb = ((long) b & 255L) << shift;
        if ((len & 8) == 0) {
            state.k1 |= bb;
        } else {
            state.k2 |= bb;
            if ((len & 15) == 15) {
                bmix(state);
                state.k1 = 0L;
                state.k2 = 0L;
            }
        }

    }

    public boolean equals(Object other) {
        return other != null && other.getClass() == this.getClass();
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return "MurmurHash3";
    }

    static class State {
        long h1;
        long h2;
        long k1;
        long k2;
        long c1;
        long c2;
    }
}
