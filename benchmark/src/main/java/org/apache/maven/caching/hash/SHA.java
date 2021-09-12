package org.apache.maven.caching.hash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * SHA
 */
public class SHA implements Hash.Factory
{
    private static final ThreadLocal<MessageDigest> ALGORITHM = new ThreadLocal<>();
    private static final ThreadLocal<MessageDigest> CHECKSUM = new ThreadLocal<>();

    private final String algorithm;

    SHA( String algorithm )
    {
        this.algorithm = algorithm;
    }

    @Override
    public String getAlgorithm()
    {
        return algorithm;
    }

    @Override
    public Hash.Algorithm algorithm()
    {
        return new SHA.Algorithm( ThreadLocalDigest.get( ALGORITHM, algorithm ) );
    }

    @Override
    public Hash.Checksum checksum( int count )
    {
        return new SHA.Checksum( ThreadLocalDigest.get( CHECKSUM, algorithm ) );
    }

    private static class Algorithm implements Hash.Algorithm
    {
        private final MessageDigest digest;

        private Algorithm( MessageDigest digest )
        {
            this.digest = digest;
        }

        @Override
        public byte[] hash( byte[] array )
        {
            return digest.digest( array );
        }

        @Override
        public byte[] hash( Path path ) throws IOException
        {
            return hash( Files.readAllBytes( path ) );
        }
    }

    private static class Checksum implements Hash.Checksum
    {
        private final MessageDigest digest;

        private Checksum( MessageDigest digest )
        {
            this.digest = digest;
        }

        @Override
        public void update( byte[] hash )
        {
            digest.update( hash );
        }

        @Override
        public byte[] digest()
        {
            return digest.digest();
        }
    }
}
