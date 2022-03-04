package org.openftc.ashmem;

import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class AshmemRegion
{
    private int fd;
    private long ptr;

    public AshmemRegion(String name, int size)
    {
        fd = AshmemJNI.ashmem_create_region(name, size);

        if(fd < 0)
        {
            throw new RuntimeException("Unable to allocate shared memory!");
        }

        ptr = AshmemJNI.map_region(fd, AshmemJNI.ashmem_get_size_region(fd));
    }

    public AshmemRegion(ParcelFileDescriptor parcel)
    {
        fd = parcel.getFd();
        ptr = AshmemJNI.map_region(fd, AshmemJNI.ashmem_get_size_region(fd));

        System.out.println("Region size: " + AshmemJNI.ashmem_get_size_region(fd));
    }

    public long getPtr()
    {
        return ptr;
    }

    public ParcelFileDescriptor getParcelFileDescriptor()
    {
        try
        {
            return ParcelFileDescriptor.fromFd(fd);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    protected void finalize()
    {
        AshmemJNI.unmap_region(ptr, AshmemJNI.ashmem_get_size_region(fd));
        AshmemJNI.closeFd(fd);
    }
}
