package org.openftc.ashmem;

public class AshmemJNI
{
    public static native int ashmem_create_region(String name, int size);
    public static native long ashmem_set_prot_region(int fd, int prot);
    public static native long ashmem_pin_region(int fd, int offset, int len);
    public static native long ashmem_unpin_region(int fd, int offset, int len);
    public static native int ashmem_get_size_region(int fd);

    public static native long map_region(int fd, int len);
    public static native long unmap_region(long addr, int len);

    public static native void closeFd(int fd);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ashmem");
    }
}
