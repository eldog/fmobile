package uk.me.eldog.fface;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class FmobileContentProvider extends ContentProvider
{
    private static final String TAG = 
                              FmobileContentProvider.class.getSimpleName();
    private static final String AUTHORITY = "uk.me.eldog.fmobile.provider";
    private static final String DATABASE_NAME = "fmobile.db";
    private static final int DATABASE_VERSION = 1;

    private static final int FACE = 0;
    private static final int FACE_ID = 1;

    public static class Face implements BaseColumns
    {
        private static final String TABLE_NAME = "faces";
        private static final String COLUMN_TIMESTAMP = "timestamp";
        private static final String COLUMN_FILE_PATH = "file_path";
        private static final String COLUMN_PROCESSING_STATE = "processing_"
                                                              + "state";
        private static final String COLUMN_SCORE = "score";

        private static final String SCHEME = "content://";
        private static final String FACES_PATH = "/" + TABLE_NAME;
        private static final String FACES_ID_PATH = "/" + TABLE_NAME + "/";

        private static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
                                                   + "vnd.eldog.face";
        private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor."
                                                        + "item/vnd.eldog.face";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME 
                                                        + AUTHORITY 
                                                        + FACES_PATH);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME 
                                                            + AUTHORITY
                                                            + FACES_ID_PATH);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME
                                                                + AUTHORITY
                                                                + FACES_ID_PATH
                                                                  + "/#");

        private static final int PROCESSING_STATE_UNPROCESSED = 0;
        private static final int PROCESSING_STATE_PROCESSING = 1;
        private static final int PROCESSING_STATE_PROCESSED = 2;

        private Face()
        {
            assert false;
        } // Face
    } // class Face

    private static final UriMatcher sUriMatcher = 
                                        new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sUriMatcher.addURI(AUTHORITY, Face.TABLE_NAME, FACE);
        sUriMatcher.addURI(AUTHORITY, Face.TABLE_NAME + "/#", FACE_ID);
    } // static

    private SQLiteOpenHelper mOpenHelper;

    private SQLiteDatabase mDb = null;
    private class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null /* factory */, DATABASE_VERSION);
        } // DatabaseHelper

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE "
                       + Face.TABLE_NAME
                       + "("
                       + Face._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                       + Face.COLUMN_TIMESTAMP + " INTEGER, "
                       + Face.COLUMN_FILE_PATH + " TEXT, "
                       + Face.COLUMN_PROCESSING_STATE + " INTEGER, "
                       + Face.COLUMN_SCORE + " REAL"
                       + ");");
        } // onCreate

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading db from version " + oldVersion + " to " 
                       + newVersion + ", old data will be eradicated");
            db.execSQL("DROP TABLE IF EXISTS" + Face.TABLE_NAME);
            onCreate(db);
        } // onUpgrade
    
    } // class DatabaseHelper

    public boolean onCreate()
    {
        try
        {
            mDb = new DatabaseHelper(getContext()).getWritableDatabase();
        } // try
        catch (SQLException e)
        {
            Log.e(TAG, "Unable to get writable database", e);
            return false;
        } // catch

        return true;
    } // onCreate


    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        /**
         * We expect a face to be inserted just by it's image URI
         */
        if (sUriMatcher.match(uri) != FACE)
        {
            throw new IllegalArgumentException("Unknown uri " + uri);
        } // if

        ContentValues values;

        if (initialValues != null)
        {
            values = new ContentValues(initialValues);
        } // if
        else
        {
            values = new ContentValues();
        } // else

        if ((values.containsKey(Face.COLUMN_FILE_PATH) == false))
        {
            throw new IllegalArgumentException("No file path provided");
        } // if

        Long now = Long.valueOf(System.currentTimeMillis());

        if ((values.containsKey(Face.COLUMN_TIMESTAMP) == false))
        {
            values.put(Face.COLUMN_TIMESTAMP, now);
        } // if

        if ((values.containsKey(Face.COLUMN_PROCESSING_STATE) == false))
        {
            values.put(Face.COLUMN_PROCESSING_STATE, 
                       Face.PROCESSING_STATE_UNPROCESSED);
        } // if

        long rowId = mDb.insert(Face.TABLE_NAME,
                                Face.COLUMN_SCORE /* null column hack */,
                                values);

        if (rowId > 0)
        {
            Uri faceUri = ContentUris.withAppendedId(Face.CONTENT_ID_URI_BASE, 
                                                     rowId);
            getContext().getContentResolver().notifyChange(
                                                   faceUri, 
                                                   null /* content observer */);
            return faceUri;
        } // if

        throw new SQLException("Failed to insert row from uri: " + uri);     
    } // insert

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri))
        {
            case FACE_ID:
                qb.appendWhere(Face._ID + " = " + uri.getLastPathSegment());
            case FACE:
                qb.setTables(Face.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        } // switch
        
        Cursor c = qb.query(
                mDb,
                projection,
                selection,
                selectionArgs,
                null /* group by rows */,
                null /* filter by rows */,
                null /* order by */
        );

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    } // query

    @Override
    public int delete(Uri uri, String where, String[] whereArgs)
    {
        final int rowsDeleted;
        switch (sUriMatcher.match(uri))
        {
            case FACE:
                rowsDeleted = mDb.delete(Face.TABLE_NAME,
                                   where,
                                   whereArgs);
                break;
            case FACE_ID:
                String finalWhere = Face._ID + " = " + uri.getLastPathSegment();
                if (where != null)
                {
                    finalWhere = finalWhere + " AND " + where;
                } // if

                rowsDeleted = mDb.delete(Face.TABLE_NAME,
                                   finalWhere,
                                   whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri);
        } // switch
        getContext().getContentResolver().notifyChange(uri, null /* content observer */);
        return rowsDeleted;
    } // delete

    @Override
    public int update(Uri uri,
                      ContentValues values,
                      String where,
                      String[] whereArgs)
    {
        final int rowsUpdated;
        switch (sUriMatcher.match(uri))
        {
            case FACE:
                rowsUpdated = mDb.update(Face.TABLE_NAME,
                                         values,
                                         where,
                                         whereArgs);
                break;
            case FACE_ID:
                String finalWhere = Face._ID + " + " + uri.getLastPathSegment();
                if (where != null)
                {
                    finalWhere = finalWhere + " AND " + where;
                } // if
                rowsUpdated = mDb.update(Face.TABLE_NAME,
                                         values,
                                         finalWhere,
                                         whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        } // switch
        getContext().getContentResolver().notifyChange(uri, null /* content observer */);
        return rowsUpdated;
    } // update

    @Override
    public String getType(Uri uri)
    {
        switch(sUriMatcher.match(uri))
        {
            case FACE:
                return Face.CONTENT_TYPE;
            case FACE_ID:
                return Face.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        } // switch
    } // getType

} // class FmobileContentProvider

