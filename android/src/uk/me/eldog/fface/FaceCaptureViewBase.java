package uk.me.eldog.fface;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class FaceCaptureViewBase extends SurfaceView 
    implements SurfaceHolder.Callback, Runnable
{
    private static final String TAG = FaceCaptureViewBase.class.getSimpleName();
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int mFrameWidth;
    private int mFrameHeight;
    private byte[] mFrame;
    private boolean mThreadRun;

    public FaceCaptureViewBase(Context context)
    {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        Log.i(TAG, "Created new instance");
    } // FaceCaptureViewBase

    public int getFrameHeight()
    {
        return mFrameHeight;
    } // getHeight

    public int getFrameWidth()
    {
        return mFrameWidth;
    } // getWidth

    public void surfaceChanged(SurfaceHolder holder, 
                              int format,
                              int width,
                              int height)
    {
        Log.i(TAG, "Surface changed called");
        if (mCamera != null)
        {
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            mFrameWidth = width;
            mFrameHeight = height;
            
            double minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes)
            {
                if (Math.abs(size.height - height) < minDiff)
                {
                    mFrameWidth = size.width;
                    mFrameHeight = size.height;
                    minDiff = Math.abs(size.height - height);
                } // if
            } // for

            params.setPreviewSize(mFrameWidth, mFrameHeight);
            mCamera.setParameters(params);
            try
            {
                mCamera.setPreviewDisplay(null /* removes previews surface */);
            } // try
            catch (IOException e)
            {
                Log.e(TAG, "Could not remove preview surface", e);
            } // catch
            mCamera.startPreview();
        } // if
    } // surfaceChanged

    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.i(TAG, "Surface created called");
        mCamera = Camera.open();
        mCamera.setPreviewCallback(new PreviewCallback()
                {
                    public void onPreviewFrame(byte[] data, Camera camera)
                    {
                        synchronized(FaceCaptureViewBase.this)
                        {
                            mFrame = data;
                            FaceCaptureViewBase.this.notify();
                        } // synchronized
                    } // onPreviewFrame
                });
        (new Thread(this)).start();
    } // surfaceCreated

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.i(TAG, "Surface destroyed called");
        mThreadRun = false;
        if (mCamera != null)
        {
            synchronized (this)
            {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null /* clear preview */);
                mCamera.release();
                mCamera = null;
            } // synchronized
        } // if
    } // surfaceDestroyed

    protected abstract Bitmap processFrame(byte[] data);

    public void run()
    {
        Log.i(TAG, "Starting processing thread");
        mThreadRun = true;
        while (mThreadRun)
        {
            Bitmap bmp = null;
            synchronized (this)
            {
                try
                {
                    this.wait();
                    bmp = processFrame(mFrame);
                } // try
                catch (InterruptedException e)
                {
                    Log.e(TAG, "Processing thread interrupted", e);
                } // catch
            } // synchronized
            if (bmp != null)
            {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null)
                {
                    canvas.drawBitmap(bmp, 
                                     (canvas.getWidth() - mFrameWidth) / 2,
                                     (canvas.getHeight() - mFrameHeight) / 2,
                                     null /* paint */);
                    mHolder.unlockCanvasAndPost(canvas);
                } // if
                bmp.recycle();
            } // if
        } // while
    } // run

} // class FaceCaptureViewBase

