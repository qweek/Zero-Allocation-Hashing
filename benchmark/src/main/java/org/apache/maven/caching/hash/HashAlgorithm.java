package org.apache.maven.caching.hash;

import java.io.IOException;
import java.nio.file.Path;

/**
 * HashAlgorithm
 */
public class HashAlgorithm
{
    private final Hash.Algorithm algorithm;

    HashAlgorithm( Hash.Algorithm algorithm )
    {
        this.algorithm = algorithm;
    }

    public String hash( Path path ) throws IOException
    {
        return HexUtils.encode( algorithm.hash( path ) );
    }

    public String hash( byte[] bytes )
    {
        return HexUtils.encode( algorithm.hash( bytes ) );
    }
}
