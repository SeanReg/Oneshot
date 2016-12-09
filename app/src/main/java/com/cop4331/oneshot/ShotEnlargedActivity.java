package com.cop4331.oneshot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by nadeen on 12/3/16.
 */
public class ShotEnlargedActivity extends Activity {
    private String path = null;
    private Bitmap bitmap = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enlarged_shot);

        path = getIntent().getStringExtra("Shot");

        //Load the bitmap
        bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null) {
            //Display full screen image
            ImageView myImage = (ImageView) findViewById(R.id.imageView);
            myImage.setImageBitmap(bitmap);
        }
    }
}
