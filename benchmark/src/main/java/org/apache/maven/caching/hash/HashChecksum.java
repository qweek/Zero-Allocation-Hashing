package org.apache.maven.caching.hash;

import java.io.IOException;
import java.nio.file.Path;

/**
 * HashChecksum
 */
public class HashChecksum
{
    private final Hash.Algorithm algorithm;
    private final Hash.Checksum checksum;

    HashChecksum( Hash.Algorithm algorithm, Hash.Checksum checksum )
    {
        this.algorithm = algorithm;
        this.checksum = checksum;
    }

    public String update( Path path ) throws IOException
    {
        return updateAndEncode( algorithm.hash( path ) );
    }

    public String update( byte[] bytes )
    {
        return updateAndEncode( algorithm.hash( bytes ) );
    }

    /**
     * @param hexHash hash value in hex format. This method doesn't accept generic text - could result in error
     */
    public String update( String hexHash )
    {
        return updateAndEncode( HexUtils.decode( hexHash ) );
    }

    private String updateAndEncode( byte[] hash )
    {
        checksum.update( hash );
        return HexUtils.encode( hash );
    }

    public String digest()
    {
        return HexUtils.encode( checksum.digest() );
    }
}
