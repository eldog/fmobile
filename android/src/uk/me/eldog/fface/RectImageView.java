package uk.me.eldog.fface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.view.MotionEvent;

public class RectImageView extends ImageView
{
    private static final String TAG = RectImageView.class.getSimpleName();
    private int mBitmapWidth = -1;
    private int mBitmapHeight = -1;
    private Map<Integer, RectF> mRectFMap = new HashMap<Integer, RectF>();
    private Map<Integer, RectF> mDrawRectFMap = new HashMap<Integer, RectF>();
    private List<RectTouchListener> mRectTouchList 
                                     = new ArrayList<RectTouchListener>();
    private Paint mPaint = new Paint();
    {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(3.0f);
    }

    interface RectTouchListener
    {
        public boolean onRectTouchEvent(int rectId, MotionEvent event);
    } // RectTouchListener

    public RectImageView(Context context)
    {
        super(context);
    } // RectFView

    public RectImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    } // RectFView

    public RectImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    } // RectFView

    public void setImageBitmap(Bitmap bitmap)
    {
        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();
        Log.d(TAG, "Setting image bitmap! width: " +  mBitmapWidth
                   + " height: " + mBitmapHeight);
        super.setImageBitmap(bitmap);
    } // setBitmap

    public void setImageDrawable(BitmapDrawable bitmapDrawable)
    {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();
        super.setImageDrawable(bitmapDrawable);
    } // setDrawable

    public void addRectTouchListener(RectTouchListener rectTouchListener)
    {
        mRectTouchList.add(rectTouchListener);
    } // addRectTouchListener

    public void removeRectTouchListener(RectTouchListener rectTouchListener)
    {
        mRectTouchList.remove(rectTouchListener);
    } // removeRectTouchListener

    public void addRect(int id, RectF rect)
    {
        mRectFMap.put(id, new RectF(rect));
    } // addRect

    public void addRect(int id, Rect rect)
    {
        this.addRect(id, new RectF((float) rect.left, 
                                   (float) rect.top,
                                   (float) rect.right,
                                   (float) rect.bottom));
    } // addRect

    public RectF removeRectById(int id)
    {
        return mRectFMap.remove(id);
    } // removeRect

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
       
        Log.d(TAG, "OnDraw width: " + mBitmapWidth
                   + " height: " + mBitmapHeight);
        // Check if we haven't set our bitmap yet
        if (mBitmapHeight == -1 || mBitmapWidth == -1)
        {
            return;
        }

        int height = canvas.getHeight();
        int width = canvas.getWidth();

        Log.d(TAG, "Canvas width: " + height + " height: " + width);
        
        float xRatio = width / (float) mBitmapWidth;
        float yRatio = height / (float) mBitmapHeight;
       
        // update the sizes
        for (Entry<Integer, RectF> entry : mRectFMap.entrySet())
        {
            int id = entry.getKey();
            RectF rect = entry.getValue();
            Log.d(TAG, "rect" + rect.left + rect.top + rect.right + rect.bottom);
            mDrawRectFMap.put(id, new RectF(rect.left * xRatio,
                                        rect.top * yRatio,
                                        rect.right * xRatio,
                                        rect.bottom * yRatio));
        } // for

        for (RectF rect : mDrawRectFMap.values())
        {
            canvas.drawRect(rect, mPaint);
        } // for
    } // onDraw

    public boolean onTouchEvent (MotionEvent event)
    {
        for (Entry<Integer, RectF> entry : mDrawRectFMap.entrySet())
        {
            int id = entry.getKey();
            RectF rect = entry.getValue();
            if (rect.contains(event.getX(), event.getY()))
            {
                for (RectTouchListener rTL : mRectTouchList)
                {
                    rTL.onRectTouchEvent(id, event);
                } // for
            } // if
        } // for
        return true;
    } // onTouchEvent
 
} // class RectImageView

