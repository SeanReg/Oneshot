package com.cop4331.image_manipulation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.Image;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

/**
 * Created by Sean on 9/12/2016.
 */
public class AmendedBitmap  {

    private Bitmap mBitmap = null;
    private Canvas mCanvas = null;

    public static AmendedBitmap createFromImage(Image image) {
        Bitmap bmp = null;
        if(image.getFormat() == ImageFormat.JPEG)
        {
            //JPEGs - Simple just need to copy buffer
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] jpegByteData = new byte[buffer.remaining()];
            buffer.get(jpegByteData);
            bmp = BitmapFactory.decodeByteArray(jpegByteData, 0, jpegByteData.length, null);
        } else {
            //Multi-planar image formats are mor complicated
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();

            //Get the distance between pixels and the total row length
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            //Calculate amount of padding that's been added
            int rowPadding = rowStride - pixelStride * image.getWidth();

            // create bitmap
            bmp = Bitmap.createBitmap(image.getWidth() + rowPadding / Math.max(1, pixelStride), image.getHeight(), Bitmap.Config.RGB_565);
            bmp.copyPixelsFromBuffer(buffer);
        }


 /*       Canvas canvas = new Canvas(bmp);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (14 * 10));

        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds("Hello World", 0, ("Hello World").length(), bounds);
        int x = (bmp.getWidth() - bounds.width())/2;
        int y = (bmp.getHeight() + bounds.height())/2;

        canvas.drawText("Hello World", x, y, paint);*/

        return new AmendedBitmap(bmp);
    }

    public AmendedBitmap(Bitmap bitmap) {
        if (!bitmap.isMutable()) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        mBitmap = bitmap;
        mCanvas = new Canvas(mBitmap);
    }

    public void drawView(View drawText) {
        drawText.setDrawingCacheEnabled(true);
        mCanvas.drawBitmap(drawText.getDrawingCache(), mBitmap.getWidth() / 2.0f - drawText.getWidth() / 2.0f, mBitmap.getHeight() / 2.0f - drawText.getHeight() / 2.0f, null);
    }

    public void drawPoint(Point cordinates, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);

        mCanvas.drawCircle(cordinates.x, cordinates.y, 20.0f, paint);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void saveToFile(File saveFile) {
        try {
            FileOutputStream out = new FileOutputStream(saveFile);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
