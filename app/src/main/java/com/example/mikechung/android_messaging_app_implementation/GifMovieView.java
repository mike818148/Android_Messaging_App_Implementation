package com.example.mikechung.android_messaging_app_implementation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.util.jar.Attributes;

import static com.example.mikechung.android_messaging_app_implementation.R.drawable.loading;

/**
 * Created by MikeChung on 15/5/29.
 */
public class GifMovieView extends View {

    private Movie gifMovie;
    private InputStream gifInputStream;
    private int movieWidth, movieHeight;
    private long movieDuration;
    private long movieStart;

    public GifMovieView(Context context){
        super(context);
        init(context);
    }

    public GifMovieView(Context context, AttributeSet attrs){
        super(context,attrs);
        init(context);
    }

    public GifMovieView(Context context,  AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        setFocusable(true);

        movieWidth = gifMovie.width();
        movieHeight = gifMovie.height();
        movieDuration = gifMovie.duration();
    }

    private void setImageSource(int id){
        gifInputStream = getResources().openRawResource(id);
        gifMovie = Movie.decodeStream(gifInputStream);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(movieWidth, movieHeight);
    }

    public int getMovieWidth(){
        return movieWidth;
    }

    public int getMovieHeight(){
        return movieHeight;
    }

    public long getMovieDuration(){
        return movieDuration;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawColor(Color.TRANSPARENT);
        //super.onDraw(canvas);
        long now = SystemClock.uptimeMillis();

        if(movieStart == 0){
            movieStart = now;
        }

        if(gifMovie != null){
            int dur = gifMovie.duration();
            if(dur == 0){
                dur = 1000;
            }

            int relTime = (int) ((now - movieStart) % dur);
            gifMovie.setTime(relTime);
            gifMovie.draw(canvas, 0, 0);
            invalidate();
        }
    }


}
