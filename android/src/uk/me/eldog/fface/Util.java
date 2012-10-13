package uk.me.eldog.fface;

import java.io.IOException;

import android.app.ProgressDialog;
import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.Log;

public final class Util
{
    private static final String TAG = Util.class.getSimpleName();

    private Util()
    {
    } // Util

    public static ProgressDialog createBasicProgressDialog(int stringId, 
                                                           Activity activity)
    {
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setMessage(activity.getString(stringId));
        return dialog;
    } // createBasicProgressDialog

    public static Bitmap getImageBitmap(ContentResolver cr, Uri imageUri)
    {
        Bitmap imageBitmap;
        try
        {
            imageBitmap = Images.Media.getBitmap(cr, imageUri);
        } // try
        catch (IOException e)
        {
            Log.e(TAG, "Could not open bitmap file", e);
            return null;
        } // catch
        return Bitmap.createScaledBitmap(imageBitmap, 
                                                imageBitmap.getWidth() / 4,
                                                imageBitmap.getHeight() / 4,
                                                true);
    }

} // class Util
