package org.apache.maven.caching.hash;

import net.openhft.hash.HashFunction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.primitives.Longs.toByteArray;

/**
 * XX
 */
public class XX3 implements Hash.Factory
{
    static final HashFunction INSTANCE = net.openhft.hash.Hash.xx3();

    @Override
    public String getAlgorithm()
    {
        return "XX3";
    }

    @Override
    public Hash.Algorithm algorithm()
    {
        return new XX3.Algorithm();
    }

    @Override
    public Hash.Checksum checksum( int count )
    {
        return new XX3.Checksum( ByteBuffer.allocate( capacity( count ) ) );
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
            return toByteArray( INSTANCE.hash( array ) );
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
            return toByteArray( INSTANCE.hash( buffer, 0, buffer.position() ) );
        }
    }
}
