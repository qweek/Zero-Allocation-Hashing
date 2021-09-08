package net.openhft.hash;

import java.nio.ByteOrder;

public class Hash {
    private Hash() {}

    /**
     * Returns a hash function implementing <a href="https://github.com/Cyan4973/xxHash">xxHash
     * algorithm</a> without a seed value (0 is used as default seed value). This implementation
     * produces equal results for equal input on platforms with different {@link
     * ByteOrder}, but is slower on big-endian platforms than on little-endian.
     */
    public static HashFunction xx() {
        return XxHash.INSTANCE;
    }

    /**
     * Returns a hash function implementing <a href="https://github.com/Cyan4973/xxHash">XXH3 64bit
     * algorithm</a> without a seed value (0 is used as default seed value). This implementation
     * produces equal results for equal input on platforms with different {@link
     * ByteOrder}, but is slower on big-endian platforms than on little-endian.
     */
    public static HashFunction xx3() {
        return XxHash3.INSTANCE;
    }
}
