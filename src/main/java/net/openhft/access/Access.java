/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.access;

import net.openhft.hash.LongHashFunction;

import java.nio.ByteOrder;

import static net.openhft.internal.Primitives.unsignedByte;
import static net.openhft.internal.Primitives.unsignedInt;

/**
 * Strategy of reading bytes, defines the abstraction of {@code T} class instances as ordered byte
 * sequence. All {@code getXXX(input, offset)} should be consistent to each other in terms of
 * <i>ordered byte sequence</i> each {@code T} instance represents. For example, if some {@code
 * Access} implementation returns {@link ByteOrder#LITTLE_ENDIAN}
 * , the following expressions should always have the same value:
 * <ul>
 *     <li>{@code getLong(input, 0)}</li>
 *     <li>{@code getUnsignedInt(input, 0) | (getUnsignedInt(input, 4) << 32)}</li>
 *     <li><pre>{@code getUnsignedInt(input, 0) |
 *    ((long) getUnsignedShort(input, 4) << 32) |
 *    ((long) getUnsignedByte(input, 6) << 48) |
 *    ((long) getUnsignedByte(input, 7) << 56)}</pre></li>
 *   <li>And so on</li>
 * </ul>
 *
 * <p>{@code getXXX(input, offset)} methods could throw unchecked exceptions when requested bytes
 * range is outside of the bounds of the byte sequence, represented by the given {@code input}.
 * However, they could omit checks for better performance.
 *
 * <p>Only {@link #getByte(Object, long)}, {@link #getInt(Object, long)} and {@link #getLong(Object, long)}
 * methods are abstract in this class, so implementing them is sufficient for valid {@code Access} instance,
 * but for efficiency your should override methods used by target {@link LongHashFunction} implementation.
 *
 * <p>{@code Access} API is designed for inputs, that actually represent byte sequences that lay
 * continuously in memory. Theoretically {@code Access} strategy could be implemented for
 * non-continuous byte sequences, or abstractions which aren't actually present in memory as they
 * are accessed, but this should be awkward, and hashing using such {@code Access} is expected to
 * be slow.
 *
 * @param <T> the type of the object to access
 * @see LongHashFunction#hash(Object, Access, long, long)
 */
public abstract class Access<T> {
    /**
     * Get the {@code Access} object with a different byte order. This method should
     * always return a fixed reference.
     */
    public static <T> Access<T> reverse(Access<T> access) {
        return new ReverseAccess<>(access);
    }

    /**
     * Constructor for use in subclasses.
     */
    protected Access() {}

    // short names
    public long i64(final T input, final long offset) { return getLong(input, offset); }
    public long u32(final T input, final long offset) { return getUnsignedInt(input, offset); }
    public  int i32(final T input, final long offset) { return getInt(input, offset); }
    public  int  u8(final T input, final long offset) { return getUnsignedByte(input, offset); }
    public  int  i8(final T input, final long offset) { return getByte(input, offset); }

    /**
     * Reads {@code [offset, offset + 7]} bytes of the byte sequence represented by the given
     * {@code input} as a single {@code long} value.
     *
     * @param input the object to access
     * @param offset offset to the first byte to read within the byte sequence represented
     * by the given object
     * @return eight bytes as a {@code long} value, in the expected byteOrder
     */
    protected abstract long getLong(T input, long offset);

    /**
     * Shortcut for {@code getInt(input, offset) & 0xFFFFFFFFL}. Could be implemented more
     * efficiently.
     *
     * @param input the object to access
     * @param offset offset to the first byte to read within the byte sequence represented
     * by the given object
     * @return four bytes as an unsigned int value, in the expected byteOrder
     */
    protected long getUnsignedInt(T input, long offset) {
        return unsignedInt(getInt(input, offset));
    }

    /**
     * Reads {@code [offset, offset + 3]} bytes of the byte sequence represented by the given
     * {@code input} as a single {@code int} value.
     *
     * @param input the object to access
     * @param offset offset to the first byte to read within the byte sequence represented
     * by the given object
     * @return four bytes as an {@code int} value, in the expected byteOrder
     */
    protected abstract int getInt(T input, long offset);

    /**
     * Shortcut for {@code getByte(input, offset) & 0xFF}. Could be implemented more efficiently.
     *
     * @param input the object to access
     * @param offset offset to the byte to read within the byte sequence represented
     * by the given object
     * @return a byte by the given {@code offset}, interpreted as unsigned
     */
    protected int getUnsignedByte(T input, long offset)  {
        return unsignedByte(getByte(input, offset));
    }

    /**
     * Reads a single byte at the given {@code offset} in the byte sequence represented by the given
     * {@code input}, returned widened to {@code int}.
     *
     * @param input the object to access
     * @param offset offset to the byte to read within the byte sequence represented
     * by the given object
     * @return a byte by the given {@code offset}, widened to {@code int}
     */
    protected abstract int getByte(T input, long offset);

    /**
     * The default reverse byte order delegating {@code Access} class.
     */
    private static class ReverseAccess<T> extends Access<T> {
        private final Access<T> access;

        private ReverseAccess(final Access<T> access) {
            this.access = access;
        }

        @Override
        public long getLong(final T input, final long offset) {
            return Long.reverseBytes(access.getLong(input, offset));
        }

        @Override
        public long getUnsignedInt(final T input, final long offset) {
            return Long.reverseBytes(access.getUnsignedInt(input, offset)) >>> 32;
        }

        @Override
        public int getInt(final T input, final long offset) {
            return Integer.reverseBytes(access.getInt(input, offset));
        }

        @Override
        public int getByte(final T input, final long offset) {
            return access.getByte(input, offset);
        }
    }
}
