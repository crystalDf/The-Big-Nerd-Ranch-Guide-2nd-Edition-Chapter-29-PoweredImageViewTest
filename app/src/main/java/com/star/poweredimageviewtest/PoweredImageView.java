package com.star.poweredimageviewtest;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

public class PoweredImageView extends ImageView {

    private Movie mMovie;

    private boolean mIsAutoPlay;

    private Bitmap mStartBitmap;

    private int mImageWidth;
    private int mImageHeight;

    private boolean mIsPlaying;

    private long mMovieStart;

    public PoweredImageView(Context context) {
        super(context);
    }

    public PoweredImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PoweredImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PoweredImageView);

        int resourceId = getResourceId(attrs);

        if (resourceId != 0) {
            InputStream inputStream = getResources().openRawResource(resourceId);

            mMovie = Movie.decodeStream(inputStream);

            if (mMovie != null) {
                mIsAutoPlay = typedArray.getBoolean(R.styleable.PoweredImageView_auto_play, false);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                mImageWidth = bitmap.getWidth();
                mImageHeight = bitmap.getHeight();

                bitmap.recycle();

                if (!mIsAutoPlay) {
                    mStartBitmap = BitmapFactory.decodeResource(getResources(),
                            R.drawable.start_play);

                    setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mIsPlaying = true;
                            invalidate();
                        }
                    });
                }
            }
        }

        if (typedArray != null) {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mMovie != null) {
            setMeasuredDimension(mImageWidth, mImageHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie == null) {
            super.onDraw(canvas);
        } else {
            if (mIsAutoPlay) {
                playMovie(canvas);
                invalidate();
            } else if (mIsPlaying) {
                if (playMovie(canvas)) {
                    mIsPlaying = false;
                }
                invalidate();
            } else {
                mMovie.setTime(0);
                mMovie.draw(canvas, 0, 0);

                canvas.drawBitmap(mStartBitmap,
                        (mImageWidth - mStartBitmap.getWidth()) / 2,
                        (mImageHeight - mStartBitmap.getHeight()) / 2,
                        null);
            }
        }

    }

    private int getResourceId(AttributeSet attrs) {
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            if (attrs.getAttributeName(i).equals("src")) {
                return attrs.getAttributeResourceValue(i, 0);
            }
        }

        return 0;
    }

    private boolean playMovie(Canvas canvas) {

        long now = System.currentTimeMillis();

        if (mMovieStart == 0) {
            mMovieStart = now;
        }

        int duration = mMovie.duration();

        if (duration == 0) {
            duration = 1000;
        }

        int realTime = (int) ((now - mMovieStart) % duration);

        mMovie.setTime(realTime);
        mMovie.draw(canvas, 0, 0);

        if (now - mMovieStart > duration) {
            mMovieStart = 0;
            return true;
        }

        return false;
    }

}
