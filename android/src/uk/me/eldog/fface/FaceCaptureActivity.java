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

import uk.me.eldog.fface.R;

public class FaceCaptureActivity extends Activity
{
    private final static String TAG = FaceCaptureActivity.class.getSimpleName();

    final static String EXTERNAL_CAMERA_DIR = "/Camera/";

    private Uri mImageUri = null;

    private static final int INTENT_PICTURE_TAKEN = 0;

    private class EnsureExternalDirectory extends AsyncTask<Void, Void, Uri>
    {
        protected Uri doInBackground(Void... params)
        {
            if (Environment.MEDIA_MOUNTED.equals(
                        Environment.getExternalStorageState()))
            {
                File imageDir = new File(Environment.getExternalStorageDirectory(),
                                      Environment.DIRECTORY_DCIM 
                                      + EXTERNAL_CAMERA_DIR);
                boolean createdDirs = imageDir.mkdirs();
                Log.i(TAG, imageDir + (createdDirs ? " created" : " exists"));
                
                Uri fileUri = Uri.fromFile(new File(imageDir, getImageFileName()));
                Log.i(TAG, "Image uri will be " + fileUri);
                return fileUri;

            } // if
            else
            {
                return null;
            } // else
        } // doInBackground

        protected void onProgressUpdate(Void... progress)
        {
        } // onProgressUpdate

        protected void onPostExecute(Uri result)
        {
            if (result != null)
            {
                mImageUri = result;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(intent, INTENT_PICTURE_TAKEN);
            } // if
            else
            {
                Log.e(TAG, "Could not get access to external directory");
            } // else
        } // onPostExecute

    } // EnsureExternalDirectory
   
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case INTENT_PICTURE_TAKEN:
                if (resultCode == FaceCaptureActivity.RESULT_OK)
                {
                    Log.i(TAG, "Picture taken succesfully");
                    Intent selectFaceIntent = new Intent(FaceCaptureActivity.this,
                                                         SelectFaceActivity.class);
                    selectFaceIntent.putExtra(
                            SelectFaceActivity.BUNDLE_EXTRA_IMAGE_URI, mImageUri);
                    startActivity(selectFaceIntent);
                } // if
                else
                {
                    Log.w(TAG, "Picture was not taken");
                } // else
        } // switch
    } // onActivityResult
  
    private static SimpleDateFormat mFormat 
            = new SimpleDateFormat("yyyyMMdd_HHmmssZ");
    private static long mLastDate = 0L;
    private static int mSameSecondCount = 0;
    private static String getImageFileName()
    {
        // Number of names generated for the same second.
        Long dateTaken = System.currentTimeMillis();
        Date date = new Date(dateTaken);
        String result = "IMG" + mFormat.format(date);

        // If the last name was generated for the same second,
        // we append _1, _2, etc to the name.
        if (dateTaken / 1000 == mLastDate / 1000) 
        {
            mSameSecondCount++;
            result += "_" + mSameSecondCount;
        } // if
        else 
        {
            mLastDate = dateTaken;
            mSameSecondCount = 0;
        } // else
        return result + ".jpg";
    } // getImageFileName

    private Button mCaptureFaceButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_capture_activity);
        mCaptureFaceButton = (Button) findViewById(R.id.capture_face_button);
        mCaptureFaceButton.setOnClickListener(
            new OnClickListener()
            {
                public void onClick(View v)
                {
                    new EnsureExternalDirectory().execute();
                } // onClick
            });
    } // onCreate

} // class FaceCaptureActivity

