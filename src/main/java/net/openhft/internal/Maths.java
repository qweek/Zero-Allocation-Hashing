package net.openhft.internal;

@SuppressWarnings("Since15")
public class Maths {
    public static long unsignedLongMulXorFold(final long lhs, final long rhs) {
        final long upper = Math.multiplyHigh(lhs, rhs) + ((lhs >> 63) & rhs) + ((rhs >> 63) & lhs);
        final long lower = lhs * rhs;
        return lower ^ upper;
    }
}
