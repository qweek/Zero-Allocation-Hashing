package sun.misc;

/**
 * Stub for JDK internal ckass sun.misc.Unsafe.
 *
 * - When crossing compiling for Java SE 7 and 8, this stub class can bypass compiler sun-api
 *   warnings.
 * - Only used methods are exported.
 * - In test and production runtime, the real class is loaded from boot classpath.
 */

public final class Unsafe {
    public native int     getInt(    Object o, long offset);
    public native byte    getByte(   Object o, long offset);
    public native long    getLong(   Object o, long offset);

    public native int arrayBaseOffset(Class arrayClass);
}
