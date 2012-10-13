package uk.me.eldog.fface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

public class ViewScoreActivity extends Activity
{
    public static final String BUNDLE_EXTRA_BITMAP = "BUNDLE_EXTRA_BITMAP";
    public static final String BUNDLE_EXTRA_FACE = "BUNDLE_EXTRA_FACE";

    private static final String TAG = ViewScoreActivity.class.getSimpleName();

    private static final int DIALOG_ANALYSING_ATTRACTIVENESS_ID = 0;
    private final static int DIALOG_LOAD_FACE_DETECTOR_ID = 1;
    private final static String EXTERNAL_STORAGE_DIR
                                    = "/Android/data/uk.me.eldog.fface/files/";
    private final static String CASCADE_FILE = "lbpcascade_frontalface.xml";
    private final static String CNN_FILE = "cnn.xml";
    private final static Map<String, Integer> sDataFileMap;

    static
    {
        sDataFileMap = new HashMap<String, Integer>();
        sDataFileMap.put(CASCADE_FILE, R.raw.lbpcascade_frontalface);
        sDataFileMap.put(CNN_FILE, R.raw.cnn);
    } // static

    private ImageView mFaceImageView;
    private TextView mScoreTextView;

    private class CropAndScaleBitmap 
            extends AsyncTask<Object[], Void, Object[]>
    {
        protected void onPreExecute()
        {
            showDialog(DIALOG_ANALYSING_ATTRACTIVENESS_ID);
        } // onPreExecute

        protected Object[] doInBackground(Object[]... params)
        {   
            ContentResolver cr = (ContentResolver) params[0][2];
            Bitmap bitmap = Util.getImageBitmap(cr, (Uri) params[0][0]);
            if (bitmap == null)
            {
                Log.e(TAG, "Unable to load bitmap!");
                return null;
            } // if
            RectF faceRect = (RectF) params[0][1];
            Log.d(TAG, "CReating cropped bitmap");
            Bitmap faceBitmap = Bitmap.createBitmap(bitmap, 
                                                    (int)faceRect.left,
                                                    (int)faceRect.top,
                                                    (int) (faceRect.right 
                                                           - faceRect.left),
                                                    (int) (faceRect.bottom 
                                                           - faceRect.top));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(faceBitmap, 
                                                            128, 
                                                            128, 
                                                            true);
            Log.d(TAG, "Calculating attractiveness");
            int[] pixels = new int[128 * 128];
            scaledBitmap.getPixels(pixels, 0, 128, 0, 0, 128, 128);
            Log.d(TAG, "pixels length = " + pixels.length);
            double score = -1000.0;
            if (Environment.MEDIA_MOUNTED.equals(
                        Environment.getExternalStorageState()))
            {
                File externalStorageDir = new File(
                            Environment.getExternalStorageDirectory(),
                            EXTERNAL_STORAGE_DIR);
                boolean createdDir = externalStorageDir.mkdirs();
                Log.i(TAG, createdDir 
                       ? "Created new directory for files" 
                       : "Directory exists");
                for (Map.Entry<String, Integer> dataEntry : sDataFileMap.entrySet())
                {
                    String fileName = dataEntry.getKey();
                    Integer resource = dataEntry.getValue(); 
                    File file = new File(externalStorageDir, fileName);
                    if (file.exists())
                    {
                        Log.i(TAG, "File " + fileName + " exists");
                        continue;
                    } // if
                    InputStream is = null;
                    FileOutputStream os = null;
                    
                    try
                    {
                        is = ViewScoreActivity.this
                                .getResources().openRawResource(resource);
                        os = new FileOutputStream(file);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1)
                        {
                            os.write(buffer, 0, bytesRead);
                        } // while
                    } // try
                    catch (IOException e)
                    {
                        Log.e(TAG, 
                              "Problem happened writing file " + fileName, 
                              e);
                        break;
                    } // catch
                    finally
                    {
                        if (is != null)
                        {
                            try
                            {
                                is.close();
                            } // try
                            catch (IOException e)
                            {
                                Log.e(TAG, "Problem closing input stream", e);
                            } // catch
                        } // if
                        if (os != null)
                        {
                            try
                            {
                                os.close();
                            } // try
                            catch (IOException e)
                            {
                                Log.e(TAG, "Problem closing output stream", e);
                            } // catch
                        } // if
                    } // finally
                } // for

                score = runConv(new File(externalStorageDir, CNN_FILE).toString(), 
                                       pixels);
                Log.d(TAG, "SCORE = " + score);
            }
            else
            {
                Log.e(TAG, "Could not access external storage - unable to score!");
            } // else
            return new Object[]{faceBitmap, score};
        } // doInBackground

        protected void onPostExecute(Object[] result)
        {
            Bitmap bitmap = (Bitmap) result[0];
            double score = (Double) result[1];
            mFaceImageView.setImageBitmap(bitmap);
            mScoreTextView.setText(String.format("%.1f " + 
                                                ViewScoreActivity.this.
                                                    getString(
                                                        R.string.out_of), 
                                            (score + 3) * (10/6.0) ));
            removeDialog(DIALOG_ANALYSING_ATTRACTIVENESS_ID);
            Log.d(TAG, "Post exectutsed");
        } // onPostExecute

    } // CropAndScaleBitmap

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_score_activity);
        mFaceImageView = (ImageView) findViewById(R.id.view_score_image_view);
        mScoreTextView = (TextView) findViewById(R.id.view_score_text_view);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            Uri imageUri = (Uri) extras.get(BUNDLE_EXTRA_BITMAP);
            RectF face = (RectF) extras.get(BUNDLE_EXTRA_FACE);
            new CropAndScaleBitmap().execute(new Object[] {imageUri,
                                                           face,
                                                           getContentResolver()});
        } // if
    } // onCreate
    
    protected Dialog onCreateDialog(int id)
    {
        ProgressDialog dialog;
        switch(id)
        {
            case DIALOG_ANALYSING_ATTRACTIVENESS_ID:
                dialog = Util.createBasicProgressDialog(
                            R.string.analyzing_attractiveness, this);
                break;
            default:
                dialog = null;
        } // switch
        return dialog;
    } // onCreateDialog

    static native double runConv(String cnnFile, int[] data);

    static
    {
        System.loadLibrary("FaceCapture");
    } // static

} // ViewScoreActivity
