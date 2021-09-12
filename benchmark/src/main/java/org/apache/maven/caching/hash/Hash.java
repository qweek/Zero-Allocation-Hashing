package org.apache.maven.caching.hash;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Hash
 */
public class Hash
{
    /**
     * Algorithm
     */
    public interface Algorithm
    {
        byte[] hash( byte[] array );

        byte[] hash( Path path ) throws IOException;
    }

    /**
     * accumulates states and should be completed by {@link #digest()}
     */
    public interface Checksum
    {
        void update( byte[] hash );

        byte[] digest();
    }

    /**
     * Factory
     */
    public interface Factory
    {
        String getAlgorithm();

        Algorithm algorithm();

        Checksum checksum( int count );
    }
}
