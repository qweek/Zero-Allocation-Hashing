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

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.*;
import static org.junit.Assert.*;

public class HashFunctionTest {
    public static void test(HashFunction f, byte[] data, long eh) {
        int len = data.length;
        ByteBuffer bb = ByteBuffer.wrap(data).order(nativeOrder());
        testArrays(f, data, eh, len);
        testByteBuffers(f, eh, len, bb);
        testByteBuffersOrder(f, eh, len, data);
    }

    public static void testException(HashFunction f, int off, int len, int capacity) {
        boolean ok = false;
        try {
            f.hash(new byte[capacity], off, len);
        } catch (IndexOutOfBoundsException expected) {
            ok = true;
        } catch (Throwable e) {
            fail("unexpected exception: " + e.toString());
        }
        assertTrue("should throw IndexOutOfBoundsException", ok);

        ok = false;
        try {
            f.hash(ByteBuffer.allocate(capacity), off, len);
        } catch (IndexOutOfBoundsException expected) {
            ok = true;
        } catch (Throwable e) {
            fail("unexpected exception: " + e.toString());
        }
        assertTrue("should throw IndexOutOfBoundsException", ok);
    }

    private static void testArrays(HashFunction f, byte[] data, long eh, int len) {
        assertEquals("byte array", eh, f.hash(data));

        byte[] data2 = new byte[len + 2];
        System.arraycopy(data, 0, data2, 1, len);
        assertEquals("byte array off len", eh, f.hash(data2, 1, len));
    }

    private static void testByteBuffersOrder(HashFunction f, long eh, int len, byte[] data) {
        ByteBuffer defaultOrder = ByteBuffer.wrap(data);
        ByteBuffer nativeOrder = ByteBuffer.wrap(data).order(nativeOrder());
        ByteBuffer littleEndian = ByteBuffer.wrap(data).order(LITTLE_ENDIAN);
        ByteBuffer bigEndian = ByteBuffer.wrap(data).order(BIG_ENDIAN);

        assertEquals("byte buffer default order", eh, f.hash(defaultOrder));
        assertEquals("byte buffer native order", eh, f.hash(nativeOrder));
        assertEquals("byte buffer little endian", eh, f.hash(littleEndian));
        assertEquals("byte buffer big endian", eh, f.hash(bigEndian));
    }

    private static void testByteBuffers(HashFunction f, long eh, int len, ByteBuffer bb) {
        // To Support IBM JDK7, methods of Buffer#position(int) and Buffer#clear() for a ByteBuffer
        // object need to be invoked from a parent Buffer object explicitly.

        bb.order(LITTLE_ENDIAN);
        assertEquals("byte buffer little endian", eh, f.hash(bb));
        ByteBuffer bb2 = ByteBuffer.allocate(len + 2).order(LITTLE_ENDIAN);
        ((Buffer)bb2).position(1);
        bb2.put(bb);
        assertEquals("byte buffer little endian off len", eh, f.hash(bb2, 1, len));

        ((Buffer)bb.order(BIG_ENDIAN)).clear();

        assertEquals("byte buffer big endian", eh, f.hash(bb));
        bb2.order(BIG_ENDIAN);
        assertEquals("byte buffer big endian off len", eh, f.hash(bb2, 1, len));

        ((Buffer)bb.order(nativeOrder())).clear();
    }
}
