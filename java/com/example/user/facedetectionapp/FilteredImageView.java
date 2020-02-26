package com.example.user.facedetectionapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by User on 8/23/2017.
 */

public class FilteredImageView extends View {

    Bitmap bitmap;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public FilteredImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(bitmap != null) {
            canvas.rotate(90, 200, 200);
            canvas.translate(0, 0);
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

    }
}
