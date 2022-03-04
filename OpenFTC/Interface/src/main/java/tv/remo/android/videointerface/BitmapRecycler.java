package tv.remo.android.videointerface;/*
 * Copyright (c) 2019 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.graphics.Bitmap;

import java.util.concurrent.ArrayBlockingQueue;

/*
 * A utility class for managing the re-use of Mats
 * so as to re-use already allocated memory instead
 * of constantly allocating new Mats and then freeing
 * them after use.
 */
class BitmapRecycler
{
    private RecyclableBitmap[] bmps;
    private ArrayBlockingQueue<RecyclableBitmap> availableBmps;

    BitmapRecycler(int num, int width, int height, Bitmap.Config config)
    {
        bmps = new RecyclableBitmap[num];
        availableBmps = new ArrayBlockingQueue<>(num);

        for(int i = 0; i < bmps.length; i++)
        {
            bmps[i] = new RecyclableBitmap(i, width, height, config);
            availableBmps.add(bmps[i]);
        }
    }

    synchronized RecyclableBitmap takeBmp() throws InterruptedException
    {
        if(availableBmps.size() == 0)
        {
            throw new RuntimeException("All bmp have been checked out!");
        }

        RecyclableBitmap bmp = availableBmps.take();
        bmp.checkedOut = true;
        return bmp;
    }

    synchronized void returnBmp(RecyclableBitmap bmp)
    {
        if(bmp != bmps[bmp.idx])
        {
            throw new IllegalArgumentException("This bmp does not belong to this recycler!");
        }

        if(bmp.checkedOut)
        {
            bmp.checkedOut = false;
            availableBmps.add(bmp);
        }
        else
        {
            throw new IllegalArgumentException("This bmp has already been returned!");
        }
    }

    class RecyclableBitmap
    {
        private int idx = -1;
        private volatile boolean checkedOut = false;
        private Bitmap bitmap;

        private RecyclableBitmap(int idx, int width, int height, Bitmap.Config config)
        {
            this.bitmap = Bitmap.createBitmap(width, height, config);
            this.idx = idx;
        }

        public Bitmap getBitmap()
        {
            return bitmap;
        }
    }
}