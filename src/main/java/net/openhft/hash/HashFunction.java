package net.openhft.hash;

import net.openhft.access.Access;

import java.nio.ByteOrder;

public class HashFunction {
    private HashFunction() {}

    /**
     * Returns a hash function implementing <a href="https://github.com/Cyan4973/xxHash">xxHash
     * algorithm</a> without a seed value (0 is used as default seed value). This implementation
     * produces equal results for equal input on platforms with different {@link
     * ByteOrder}, but is slower on big-endian platforms than on little-endian.
     */
    public static LongHashFunction xx() {
        return XxHashFunction.INSTANCE;
    }

    /**
     * Returns a hash function implementing <a href="https://github.com/Cyan4973/xxHash">XXH3 64bit
     * algorithm</a> without a seed value (0 is used as default seed value). This implementation
     * produces equal results for equal input on platforms with different {@link
     * ByteOrder}, but is slower on big-endian platforms than on little-endian.
     */
    public static LongHashFunction xx3() {
        return XxHash3Function.INSTANCE;
    }

    private static class XxHashFunction extends LongHashFunction {
        private static final XxHashFunction INSTANCE = new XxHashFunction();

        @Override
        public <T> long hash(T input, Access<T> access, long off, long len) {
            return XxHash.hash(input, access, off, len);
        }
    }

    private static class XxHash3Function extends LongHashFunction {
        private static final XxHash3Function INSTANCE = new XxHash3Function();

        @Override
        public <T> long hash(final T input, final Access<T> access, final long off, final long len) {
            return XxHash3.hash(input, access, off, len);
        }
    }
}
