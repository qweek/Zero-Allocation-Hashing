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
import static org.junit.Assert.assertEquals;

public class LongHashFunctionTest {
    public static void test(LongHashFunction f, byte[] data, long eh) {
        int len = data.length;
        ByteBuffer bb = ByteBuffer.wrap(data).order(nativeOrder());
        testArrays(f, data, eh, len);
        testByteBuffers(f, eh, len, bb);
    }

    private static void testArrays(LongHashFunction f, byte[] data, long eh, int len) {
        assertEquals("byte array", eh, f.hashBytes(data));

        byte[] data2 = new byte[len + 2];
        System.arraycopy(data, 0, data2, 1, len);
        assertEquals("byte array off len", eh, f.hashBytes(data2, 1, len));
    }

    private static void testByteBuffers(LongHashFunction f, long eh, int len, ByteBuffer bb) {
        // To Support IBM JDK7, methods of Buffer#position(int) and Buffer#clear() for a ByteBuffer
        // object need to be invoked from a parent Buffer object explicitly.

        bb.order(LITTLE_ENDIAN);
        assertEquals("byte buffer little endian", eh, f.hashBytes(bb));
        ByteBuffer bb2 = ByteBuffer.allocate(len + 2).order(LITTLE_ENDIAN);
        ((Buffer)bb2).position(1);
        bb2.put(bb);
        assertEquals("byte buffer little endian off len", eh, f.hashBytes(bb2, 1, len));

        ((Buffer)bb.order(BIG_ENDIAN)).clear();

        assertEquals("byte buffer big endian", eh, f.hashBytes(bb));
        bb2.order(BIG_ENDIAN);
        assertEquals("byte buffer big endian off len", eh, f.hashBytes(bb2, 1, len));

        ((Buffer)bb.order(nativeOrder())).clear();
    }
}
