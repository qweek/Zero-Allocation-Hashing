package net.openhft.internal;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("Since15")
public class Maths {
    @NotNull
    private static final MathsJDK8 INSTANCE = hasMultiplyHigh() ? new MathsJDK9() : new MathsJDK8();

    static boolean hasMultiplyHigh() {
        try {
            return Math.multiplyHigh(0L, 0L) == 0L;
        } catch (final Throwable ignore) {
            return false;
        }
    }

    public static long unsignedLongMulXorFold(final long lhs, final long rhs) {
        return INSTANCE.unsignedLongMulXorFoldImp(lhs, rhs);
    }

    private static class MathsJDK8 {
        long unsignedLongMulXorFoldImp(final long lhs, final long rhs) {
            // The Grade School method of multiplication is a hair faster in Java,
            // primarily used here because the implementation is simpler.
            final long lhs_l = lhs & 0xFFFFFFFFL;
            final long lhs_h = lhs >>> 32;
            final long rhs_l = rhs & 0xFFFFFFFFL;
            final long rhs_h = rhs >>> 32;
            final long lo_lo = lhs_l * rhs_l;
            final long hi_lo = lhs_h * rhs_l;
            final long lo_hi = lhs_l * rhs_h;
            final long hi_hi = lhs_h * rhs_h;

            // Add the products together. This will never overflow.
            final long cross = (lo_lo >>> 32) + (hi_lo & 0xFFFFFFFFL) + lo_hi;
            final long upper = (hi_lo >>> 32) + (cross >>> 32) + hi_hi;
            final long lower = (cross << 32) | (lo_lo & 0xFFFFFFFFL);
            return lower ^ upper;
        }
    }

    private static class MathsJDK9 extends MathsJDK8 {
        // Math.multiplyHigh() is intrinsified from JDK 10. But JDK 9 is out of life, we always prefer
        // this version to the scalar one.
        @Override
        long unsignedLongMulXorFoldImp(final long lhs, final long rhs) {
            final long upper = Math.multiplyHigh(lhs, rhs) + ((lhs >> 63) & rhs) + ((rhs >> 63) & lhs);
            final long lower = lhs * rhs;
            return lower ^ upper;
        }
    }
}
