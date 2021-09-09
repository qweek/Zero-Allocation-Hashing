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

package net.openhft.hash;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Hash function producing {@code long}-valued result from byte sequences of any length and
 * a plenty of different sources which "feels like byte sequences". Except {@link
 * #hash(byte[])} and {@link #hash(ByteBuffer)} (with their "sliced" versions)
 * methods, which actually accept byte sequences, notion of byte
 * sequence is defined as follows:
 * <ul>
 *     <li>For methods accepting arrays of Java primitives, {@code String}s and
 *     {@code StringBuilder}s, byte sequence is how the input's bytes are actually lay in memory.
 *     </li>
 *     <li>For methods accepting single primitive values, byte sequence is how this primitive
 *     would be put into memory with {@link ByteOrder#nativeOrder() native} byte order, or
 *     equivalently, {@code hashXxx(primitive)} has always the same result as {@code
 *     hashXxxs(new xxx[] {primitive})}, where "xxx" is any Java primitive type name.</li>
 *     <li>For {@link #hashByteBuffer(ByteBuffer, long, long)} method byte sequence abstraction
 *     is defined by the given {@link ByteOrder#LITTLE_ENDIAN} strategy to the given object.</li>
 * </ul>
 *
 * <p>Hash function implementation could either produce equal results for equal input on platforms
 * with different {@link ByteOrder}, favoring one byte order in terms of performance, or different
 * results, but performing equally good. This choice should be explicitly documented for all
 * {@code LongHashFunction} implementations.
 *
 * <h2>Subclassing</h2>
 * To implement a specific hash function algorithm, this class should be subclassed. Only method
 * {@link #hashByteBuffer(ByteBuffer, long, long)}
 * should be implemented; other have default implementations which in the end delegate to
 * {@link #hashByteBuffer(ByteBuffer, long, long)} abstract method.
 *
 * <p>Notes about how exactly methods with default implementations are implemented in doc comments
 * are given for information and could be changed at any moment. However, it could hardly cause
 * any issues with subclassing, except probably little performance degradation. Methods documented
 * as "shortcuts" could either delegate to the referenced method or delegate directly to the method
 * to which the referenced method delegates.
 *
 *<p>{@code LongHashFunction} implementations shouldn't assume that {@code Access} strategies
 * do defensive checks, and access only bytes within the requested range.
 */
public abstract class HashFunction {
    /**
     * Shortcut for {@link #hash(byte[], int, int) hashBytes(input, 0, input.length)}.
     */
    public long hash(@NotNull byte[] input) {
        return hash(ByteBuffer.wrap(input), 0, input.length);
    }

    /**
     * Returns the hash code for the specified subsequence of the given {@code byte} array.
     *
     * <p>Default implementation delegates to {@link #hashByteBuffer(ByteBuffer, long, long)} method
     * using {@linkplain } unsafe {@code Access}.
     *
     * @param input the array to read bytes from
     * @param off index of the first {@code byte} in the subsequence to hash
     * @param len length of the subsequence to hash
     * @return hash code for the specified subsequence
     * @throws IndexOutOfBoundsException if {@code off < 0} or {@code off + len > input.length}
     * or {@code len < 0}
     */
    public long hash(@NotNull byte[] input, int off, int len) {
        return hash(ByteBuffer.wrap(input), off, len);
    }

    /**
     * Shortcut for {@link #hash(ByteBuffer, int, int)
     * hashBytes(input, input.position(), input.remaining())}.
     */
    public long hash(@NotNull ByteBuffer input) {
        return hash(input, input.position(), input.remaining());
    }

    /**
     * Returns the hash code for the specified subsequence of the given {@code ByteBuffer}.
     *
     * <p>This method doesn't alter the state (mark, position, limit or order) of the given
     * {@code ByteBuffer}.
     *
     * <p>Default implementation delegates to {@link #hashByteBuffer(ByteBuffer, long, long)} method
     * using {@link }.
     *
     * @param input the buffer to read bytes from
     * @param off index of the first {@code byte} in the subsequence to hash
     * @param len length of the subsequence to hash
     * @return hash code for the specified subsequence
     * @throws IndexOutOfBoundsException if {@code off < 0} or {@code len < 0}
     * or {@code off + len > input.capacity()}
     */
    public long hash(@NotNull ByteBuffer input, int off, int len) {
        if ((off | len | (off + len) | (input.capacity() - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
        return hashByteBuffer(littleEndian(input), off, len);
    }

    /**
     * Returns the hash code for {@code len} continuous bytes of the given {@code input} object,
     * starting from the given offset. The abstraction of input as ordered byte sequence and
     * "offset within the input" is defined by the given {@code access} strategy.
     *
     * <p>This method doesn't promise to throw a {@code RuntimeException} if {@code
     * [off, off + len - 1]} subsequence exceeds the bounds of the bytes sequence, defined by {@code
     * access} strategy for the given {@code input}, so use this method with caution.
     *
     * @param buffer the object to read bytes from
     * @param off offset to the first byte of the subsequence to hash
     * @param len length of the subsequence to hash
     * @return hash code for the specified bytes subsequence
     */
    protected abstract long hashByteBuffer(ByteBuffer buffer, long off, long len);

    protected static ByteBuffer littleEndian(final ByteBuffer input) {
        return input.order() == LITTLE_ENDIAN ? input : input.duplicate().order(LITTLE_ENDIAN);
    }

    protected static long i64(final ByteBuffer buffer, final long offset) { return buffer.getLong((int) offset); }
    protected static long u32(final ByteBuffer buffer, final long offset) { return unsignedInt(i32(buffer, offset)); }
    protected static  int i32(final ByteBuffer buffer, final long offset) { return buffer.getInt((int) offset); }
    protected static  int  u8(final ByteBuffer buffer, final long offset) { return unsignedByte(i8(buffer, offset)); }
    protected static  int  i8(final ByteBuffer buffer, final long offset) { return buffer.get((int) offset); }

    protected static long unsignedInt(int i) {
        return i & 0xFFFFFFFFL;
    }

    protected static int unsignedByte(int b) {
        return b & 0xFF;
    }
}
