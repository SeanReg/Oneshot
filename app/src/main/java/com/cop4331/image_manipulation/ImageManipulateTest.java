package com.cop4331.image_manipulation;

import com.cop4331.oneshot.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageManipulateTest extends AppCompatActivity {

    private AmendedBitmap mAmendedBitmap = null;
    private Point lastDraw = new Point(0, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_manipulate_test);

        final ImageView imageView = (ImageView)findViewById(R.id.birdImageView);
        final BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap imgBitmap = drawable.getBitmap();

        mAmendedBitmap = new AmendedBitmap(imgBitmap);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Matrix imgMatrixInvert = new Matrix();
                imageView.getImageMatrix().invert(imgMatrixInvert);
                float[] cords = {event.getX(), event.getY()};
                imgMatrixInvert.mapPoints(cords);

                Point touchPoint = new Point((int)cords[0], (int)cords[1]);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastDraw = touchPoint;
                        //Fall through on purpose

                    case MotionEvent.ACTION_MOVE:
                        Log.d("Point", "X: " + event.getX() + " Y: " + event.getY());

                        Point posDiff = new Point(lastDraw.x - touchPoint.x, lastDraw.y - touchPoint.y);
                        double distance = Math.hypot((double)posDiff.x, (double)posDiff.y);
                        Point paint = new Point();
                        int i = 0;
                        do {
                            double delta = 0.0f;
                            if (distance > 0.0 ) delta = i / distance;

                            paint.x = (int)(touchPoint.x + (posDiff.x * delta));
                            paint.y = (int)(touchPoint.y + (posDiff.y * delta));

                            mAmendedBitmap.drawPoint(paint, Color.RED);
                            ++i;
                        }while(i < distance);

                        lastDraw = touchPoint;
                        imageView.setImageBitmap(mAmendedBitmap.getBitmap());
                        break;
                }
                return true;
            }
        });
    }

    private void fixViewAspect(View fixView, Size resolution) {
        //Get the aspect ratio of the camera resolution
        double aspectRatio = resolution.getHeight() / (double)resolution.getWidth();

        int scaledWidth = 0;
        int scaledHeight = 0;
        //Find the width or height constraint
        if (fixView.getMeasuredWidth() > fixView.getMeasuredHeight()) {
            scaledWidth  = fixView.getMeasuredWidth();
            //Scale the height proportionately with the width
            scaledHeight = (int)(fixView.getMeasuredWidth() * aspectRatio);
        } else {
            //Scale the width proportionately with the height
            scaledWidth  = (int)(fixView.getMeasuredHeight() * aspectRatio);
            scaledHeight = fixView.getMeasuredHeight();
        }

        //Set the new scales of the TextureView
        fixView.setScaleX((float)scaledWidth / fixView.getMeasuredWidth());
        fixView.setScaleY((float)scaledHeight / fixView.getMeasuredHeight());
    }
}
