package com.cop4331.image_manipulation;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.cop4331.networking.AccountManager;
import com.cop4331.networking.Game;
import com.cop4331.oneshot.InGameActivity;
import com.cop4331.oneshot.R;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageManipulateTest extends AppCompatActivity {

    private AmendedBitmap mAmendedBitmap = null;
    private Point lastDraw = new Point(0, 0);

    private int mDrawColor = 0x46AF4AFF;

    private final ColorPickerDialog mColorPickerDialog = new ColorPickerDialog();
    private Button mColorPaletteButton = null;
    private Button mSaveButton = null;

    private String mSubmitTo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_manipulate_test);

        mColorPaletteButton = (Button)findViewById(R.id.colorPaletteButton);
        mColorPaletteButton.setOnClickListener(mColorPaletteButtonListener);

        mSaveButton = (Button)findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(mSaveButtonListener);

        mSubmitTo = getIntent().getStringExtra("gameId");

        mColorPickerDialog.initialize(R.string.picker_title, mPickerColors, mDrawColor, 4, mPickerColors.length);
        mColorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                mDrawColor = color;
            }
        });


        final ImageView imageView = (ImageView)findViewById(R.id.birdImageView);
        final BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

        File loadBitmap = (File)getIntent().getExtras().get("Bitmap");
        mAmendedBitmap = new AmendedBitmap(BitmapFactory.decodeFile(loadBitmap.getAbsolutePath()));
        loadBitmap.delete();
        imageView.setImageBitmap(mAmendedBitmap.getBitmap());

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

                            mAmendedBitmap.drawPoint(paint, mDrawColor);
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

    private final Button.OnClickListener mColorPaletteButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mColorPickerDialog.show(getFragmentManager(), "Color");
        }
    };

    private final Button.OnClickListener mSaveButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("bitmap", Context.MODE_PRIVATE);
            if (!directory.exists()) {
                directory.mkdir();
            }

            FileOutputStream fStream = null;
            File filePath = null;
            try {
                filePath = File.createTempFile("bitmap", ".png", directory);
                mAmendedBitmap.saveToFile(filePath);
                AccountManager.getInstance().getCurrentAccount().submitShot(mSubmitTo, filePath);
                Log.d("SAVE", "Image saved to " + filePath.toString());
            } catch (IOException e) {

            }
        }
    };

    private final static int[] mPickerColors = new int[] {
        Color.argb(255, 246, 64, 44),
            Color.argb(255, 235, 20, 96),
            Color.argb(255, 156, 26, 177),
            Color.argb(255, 102, 51, 185),
            Color.argb(255, 61, 77, 183),
            Color.argb(255, 16, 147, 245),
            Color.argb(255, 0, 16, 246),
            Color.argb(255, 0, 187, 213),
            Color.argb(255, 0, 150, 135),
            Color.argb(255, 70, 175, 74),
            Color.argb(255, 136, 196, 64),
            Color.argb(255, 204, 221, 30),
            Color.argb(255, 255, 236, 22),
            Color.argb(255, 255, 193, 0),
            Color.argb(255, 255, 152, 96),
            Color.argb(255, 255, 85, 5),
            Color.argb(255, 122, 85, 71),
            Color.argb(255, 157, 157, 157),
            Color.argb(255, 94, 124, 139)
    };
}
