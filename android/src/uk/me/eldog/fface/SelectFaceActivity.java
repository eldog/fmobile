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

public class SelectFaceActivity extends Activity
{
    public static final String BUNDLE_EXTRA_IMAGE_URI 
                                                     = "BUNDLE_EXTRA_IMAGE_URI";

    private static final String TAG = SelectFaceActivity.class.getSimpleName();

    private final static int DIALOG_FINDING_FACES_ID = 0;
    private final static int DIALOG_CROPPING_FACE_ID = 1;
   
    private Uri mImageUri = null;

    private Map<Integer, FaceRect> mFaceMap = new HashMap<Integer, FaceRect>();
    private Bitmap mBitmap = null;
    private final static int MAX_FACES = 5;

    private RectImageView mFaceRectImageView;

    private class InsertImageIntoMediaStore 
            extends AsyncTask<Pair<ContentResolver, Uri>, 
                              Void, 
                              Object[]>
    {
        protected void onPreExecute()
        {
            showDialog(DIALOG_FINDING_FACES_ID);
        } // onPreExecute

        protected Object[]
            doInBackground(Pair<ContentResolver, Uri>... param)
        {
            ContentResolver cr = param[0].first;
            Uri uri = param[0].second;
            getContentResolver().notifyChange(uri, null);
            File imageFile = null;
            try
            {
                // note it's a java.net URI
                imageFile = new File(new URI(uri.toString()));
            }
            catch(URISyntaxException e)
            {
                Log.e(TAG, "Could not create URI from android uri", e);
                return null;
            }
                        
            Uri imageUri = null;
            try
            {
                imageUri = Uri.parse(
                        Images.Media.insertImage(cr, 
                                                 imageFile.toString(), 
                                                 imageFile.getName(), 
                                                 "Taken by FMobile"));
            } // try
            catch (Throwable thr)
            {
                Log.e(TAG, "Unable to write to media store" + thr);
            } // catch

            // Update the media provider to have the date information to display
            // correctly in the gallery application
            ContentValues values = new ContentValues(1);
            values.put(ImageColumns.DATE_TAKEN, imageFile.lastModified());
            cr.update(imageUri, 
                      values, 
                      null /* where */,
                      null /* selectionArgs */);

            Bitmap imageBitmap = Util.getImageBitmap(cr, imageUri);
            if (imageBitmap == null)
            {
                Log.e(TAG, "Unable to open face bitmap for viewing");
                return null;
            } // if

            FaceDetector faceDetector = new FaceDetector(imageBitmap.getWidth(),
                                                         imageBitmap.getHeight(),
                                                         MAX_FACES);
            Face[] faces = new Face[MAX_FACES];
            int numberFacesFound = faceDetector.findFaces(imageBitmap, faces);
            Map<Integer, FaceRect> faceMap 
                = new HashMap<Integer, FaceRect>(numberFacesFound);
            for (int faceIndex = 0; faceIndex < numberFacesFound; faceIndex ++)
            {
                faceMap.put(faceIndex, new FaceRect(faces[faceIndex]));
            } // for

            Log.i(TAG, "Found " + numberFacesFound  + " faces");

            return new Object[] {imageBitmap, faceMap, imageUri};
        } // onPreExecute

        protected void onProgressUpdate(Void... params)
        {
        } // onProgressUpdate

        protected void 
            onPostExecute(Object... result)
        {
            if (result != null)
            {
                Log.i(TAG, "Image succesfully processed");
            } // if
            else
            {
                Log.e(TAG, "Unable to process image");
            } // else
            removeDialog(DIALOG_FINDING_FACES_ID);

            mBitmap= (Bitmap) result[0];
            mFaceMap = (Map) result[1];
            mImageUri = (Uri) result[2];
            
            mFaceRectImageView.setImageBitmap(mBitmap);
            
            for (Entry<Integer, FaceRect> entry : mFaceMap.entrySet())
            {
                int id = entry.getKey();
                FaceRect faceRect = entry.getValue();
                mFaceRectImageView.addRect(id, faceRect);
            } // for
            
            mFaceRectImageView.addRectTouchListener(
                    new RectImageView.RectTouchListener()
                {
                    public boolean onRectTouchEvent(int id, MotionEvent event)
                    {
                        FaceRect face = mFaceMap.get(id);
                        if (face == null)
                        {
                            Log.e(TAG, "We touched a face that wasn't in our map");
                            return false;
                        } // if
                        else if (event.getAction() == MotionEvent.ACTION_UP)
                        {
                            Intent viewScoreIntent 
                                    = new Intent(SelectFaceActivity.this,
                                                 ViewScoreActivity.class);
                            viewScoreIntent.putExtra(
                                ViewScoreActivity.BUNDLE_EXTRA_BITMAP, mImageUri);
                            viewScoreIntent.putExtra(
                                ViewScoreActivity.BUNDLE_EXTRA_FACE, face);
                            SelectFaceActivity.this.startActivity(viewScoreIntent);

                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    } // onRectTouchEvent
                });

        } // onPostExecute
    } // class

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_face_activity);
        mFaceRectImageView = 
                (RectImageView) findViewById(R.id.select_face_rect_image_view);
        // Get the uri of the image
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            Uri imageUri = (Uri) extras.get(BUNDLE_EXTRA_IMAGE_URI);
            new InsertImageIntoMediaStore().execute(
                                new Pair<ContentResolver, Uri>(
                                    getContentResolver(), imageUri));
        } // if
    } // onCreate
    
    protected Dialog onCreateDialog(int id)
    {
        ProgressDialog dialog;
        switch(id)
        {
            case DIALOG_FINDING_FACES_ID:
                dialog = Util.createBasicProgressDialog(
                                                R.string.finding_faces, this);
                break;
            case DIALOG_CROPPING_FACE_ID:
                dialog = Util.createBasicProgressDialog(
                                                R.string.cropping_face, this);
                break;
            default:
                dialog = null;
        } // switch
        return dialog;
    } // onCreateDialog
   
} // SelectFaceActivity

