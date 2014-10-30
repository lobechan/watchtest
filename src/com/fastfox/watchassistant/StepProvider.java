package com.fastfox.watchassistant;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class StepProvider extends ContentProvider {

	public static final int STEP = 0;
	public static final String AUTHORITY = 
			"com.cherrysports.providers.stepprovider";
	
	private static final String DATABASE_NAME = "personal.db";
	
	private static final int DATABASE_VERSION = 11;
	
	public static final String[] TABLE_NAMES = new String[] { "steps"};
	public static final Uri CONTENT_STEPS_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAMES[STEP]);
	
	public static class ColumnsSteps {
		public static final String _ID = "_id";
		public static final String START_TIME = "start_time";
		public static final String END_TIME = "end_time";
		public static final String STEP_TYPE = "step_type";
		public static final String STEPS = "steps";
		public static final String CREATE_TIME = "create_time";
		
	}
	private static final String STEPS_CREATE_TABLE ="CREATE TABLE "
			+ TABLE_NAMES[STEP] + " (" + ColumnsSteps._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			//+ ColumnsCatalog.CATALOG_TYPE + " TEXT, "
			+ ColumnsSteps.START_TIME + " BIGINT, "
			+ ColumnsSteps.END_TIME + " BIGINT, " 
			+ ColumnsSteps.STEP_TYPE+ " INTEGER, "
			+ ColumnsSteps.STEPS+ " INTEGER, "
			+ ColumnsSteps.CREATE_TIME + "  BIGINT"+ ");";
	private static final UriMatcher sUriMatcher;
	private static final int URI_MATCH_STEPS = 0;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher
				.addURI(AUTHORITY, TABLE_NAMES[STEP], URI_MATCH_STEPS);
		
	}
	
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;

	private Context mContext;
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		int match = sUriMatcher.match(uri);
		String id = null;
		switch (match) {
		case URI_MATCH_STEPS:
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
			
		}
		count = mDb.delete(TABLE_NAMES[STEP], selection, selectionArgs);
		if (count > 0) {
			mContext.getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = sUriMatcher.match(uri);
		long rowId = 0;
		Uri baseUri = uri;
		switch (match) {
		case URI_MATCH_STEPS: {
			rowId = mDb.insert(TABLE_NAMES[STEP], null, values);
			// baseUri = CONTENT_BOOKMARK_URI;
			break;
		}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (rowId > 0) {
			Uri rowUri = ContentUris.withAppendedId(baseUri, rowId);
			mContext.getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreate() {
		mContext = getContext();
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		mDb.enableWriteAheadLogging();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs,
			String sortOrder) {

		int match = sUriMatcher.match(uri);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (match) {
		case -1:
			throw new IllegalArgumentException("Unknown URL");
			
		case URI_MATCH_STEPS:
			qb.setTables(TABLE_NAMES[STEP]);
			break;
		}
		Cursor c = qb.query(mDb, projectionIn, selection, selectionArgs, null,
				null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		int match = sUriMatcher.match(uri);
		switch (match) {
		case URI_MATCH_STEPS:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// case URI_MATCH_ARTICLES:
			// StringBuilder sb = new StringBuilder();
			// if (selection != null && selection.length() > 0) {
			// sb.append("( ");
			// sb.append(selection);
			// sb.append(" ) AND ");
			// }
			// String id = uri.getPathSegments().get(1);
			// sb.append("_id = ");
			// sb.append(id);
			// selection = sb.toString();
			// break;
		}
		count = mDb.update(TABLE_NAMES[STEP], values, selection,
				selectionArgs);
		if (count > 0) {
			mContext.getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		Context con;

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			Log.d("debug09","---create database---");
			con = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("xxxx","################## database Create!!!!");
			db.execSQL(STEPS_CREATE_TABLE);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			boolean processed = false;
			Log.d("xxxxxx","################## database onUpgrade oldVersion-!!!!"+oldVersion+" newVersion"+
					newVersion);
			
			if(!processed){
				for (int i = 0; i < TABLE_NAMES.length; i++) {
					db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAMES[i]);
				}
				onCreate(db);
			}
		}
	}
	
}
