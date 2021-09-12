package org.apache.maven.caching.hash;

import net.openhft.hashing.LongHashFunction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.primitives.Longs.toByteArray;

/**
 * XX
 */
public class XXH3 implements Hash.Factory
{
    static final LongHashFunction INSTANCE = LongHashFunction.xx3();

    @Override
    public String getAlgorithm()
    {
        return "XXH3";
    }

    @Override
    public Hash.Algorithm algorithm()
    {
        return new XXH3.Algorithm();
    }

    @Override
    public Hash.Checksum checksum( int count )
    {
        return new XXH3.Checksum( ByteBuffer.allocate( capacity( count ) ) );
    }

    static int capacity( int count )
    {
        // Java 8: Long.BYTES
        return count * Long.SIZE / Byte.SIZE;
    }

    static class Algorithm implements Hash.Algorithm
    {
        @Override
        public byte[] hash( byte[] array )
        {
            return toByteArray( INSTANCE.hashBytes( array ) );
        }

        @Override
        public byte[] hash( Path path ) throws IOException
        {
            return hash( Files.readAllBytes( path ) );
        }
    }

    static class Checksum implements Hash.Checksum
    {
        private final ByteBuffer buffer;

        Checksum( ByteBuffer buffer )
        {
            this.buffer = buffer;
        }

        @Override
        public void update( byte[] hash )
        {
            buffer.put( hash );
        }

        @Override
        public byte[] digest()
        {
            return toByteArray( INSTANCE.hashBytes( buffer, 0, buffer.position() ) );
        }
    }
}
