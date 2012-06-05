package com.kylebarlow.android.crickettherm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class DataDBAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_CRICKETCTEMP = "cricketctemp";
    public static final String KEY_NUMCHIRPS = "numchirps";
    public static final String KEY_NUMSECS = "numsecs";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LOCATIONACCURACY = "locationaccuracy";
    public static final String KEY_WEATHERAPI = "weatherapi";
    public static final String KEY_CTEMP = "ctemp";
    public static final String KEY_CONDITION = "condition";
    public static final String KEY_HUMIDITY = "humidity";
    public static final String KEY_WINDCONDITION = "windcondition";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_SERVERID = "serverid"; // 0 if not synced with server db
    public static final String KEY_MANUALTEMP = "manualtemp";
    
    private static final String TAG = "DataDBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    @SuppressWarnings("unused")
	private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "logs";
    @SuppressWarnings("unused")
	private static final int DATABASE_VERSION = 2;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE 
            + " ("+KEY_ROWID+" integer primary key autoincrement, "
                  	+KEY_CRICKETCTEMP+" float not null, "
                  	+KEY_NUMCHIRPS+" integer not null, "
                  	+KEY_NUMSECS+" integer not null, "
                  	+KEY_LATITUDE+" float, "
                  	+KEY_LONGITUDE+" float, "
                  	+KEY_LOCATIONACCURACY+" float, "
                  	+KEY_WEATHERAPI+" text, "
                  	+KEY_CTEMP+" float, "
                  	+KEY_CONDITION+" text, "
                  	+KEY_HUMIDITY+" text, "
                  	+KEY_WINDCONDITION+" text, "
                  	+KEY_TIMESTAMP+" timestamp not null, "
                  	+KEY_MANUALTEMP+" float, "
                    +KEY_SERVERID+" integer not null);";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

      public DatabaseHelper(Context context, String name, CursorFactory
          factory, int version) {
                     super(context, name, factory, version);
                 }
        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DataDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DataDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx, DATABASE_TABLE, null,
            2);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new data entry using the params provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createLogEntry(Double ctemp, String condition, 
			String humidity, String windcondition, Double cricketctemp,
			int numchirps, int numsecs, Double latitude, Double longitude,
			Double locationaccuracy, String weatherapi, String manualtemp) {
    	DateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String crntDate = dateFormatISO8601.format(new Date());
    	
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CRICKETCTEMP, cricketctemp);
        initialValues.put(KEY_NUMCHIRPS, numchirps);
        initialValues.put(KEY_NUMSECS, numsecs);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);
        initialValues.put(KEY_LOCATIONACCURACY, locationaccuracy);
        initialValues.put(KEY_WEATHERAPI, weatherapi);
        initialValues.put(KEY_CTEMP, ctemp);
        initialValues.put(KEY_CONDITION, condition);
        initialValues.put(KEY_HUMIDITY, humidity);
        initialValues.put(KEY_WINDCONDITION, windcondition);
        initialValues.put(KEY_TIMESTAMP, crntDate);
        initialValues.put(KEY_SERVERID, 0);
        Double manTempDouble=null;
        try {
        	manTempDouble = new Double(manualtemp);
        }
        catch (Exception e){
        	initialValues.putNull(KEY_MANUALTEMP);
        }
        if (manTempDouble!=null)
        	initialValues.put(KEY_MANUALTEMP, new Double(manualtemp));
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /*
    public long createLogEntryNoLocation(Double ctemp, String condition, 
			String humidity, String windcondition, Double cricketctemp,
			int numchirps, int numsecs, String weatherapi, String manualtemp) {
    	DateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String crntDate = dateFormatISO8601.format(new Date());
    	
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CRICKETCTEMP, cricketctemp);
        initialValues.put(KEY_NUMCHIRPS, numchirps);
        initialValues.put(KEY_NUMSECS, numsecs);
        initialValues.putNull(KEY_LATITUDE);
        initialValues.putNull(KEY_LONGITUDE);
        initialValues.putNull(KEY_LOCATIONACCURACY);
        initialValues.put(KEY_WEATHERAPI, weatherapi);
        initialValues.put(KEY_CTEMP, ctemp);
        initialValues.put(KEY_CONDITION, condition);
        initialValues.put(KEY_HUMIDITY, humidity);
        initialValues.put(KEY_WINDCONDITION, windcondition);
        initialValues.put(KEY_TIMESTAMP, crntDate);
        initialValues.put(KEY_SERVERID, 0);
        initialValues.put(KEY_MANUALTEMP, new Double(manualtemp));
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    } */
    
    public long createLogEntryNoWeather(Double cricketctemp,
			int numchirps, int numsecs, Double latitude, Double longitude,
			Double locationaccuracy, String manualtemp) {
    	DateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String crntDate = dateFormatISO8601.format(new Date());
    	
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CRICKETCTEMP, cricketctemp);
        initialValues.put(KEY_NUMCHIRPS, numchirps);
        initialValues.put(KEY_NUMSECS, numsecs);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);
        initialValues.put(KEY_LOCATIONACCURACY, locationaccuracy);
        initialValues.putNull(KEY_WEATHERAPI);
        initialValues.putNull(KEY_CTEMP);
        initialValues.putNull(KEY_CONDITION);
        initialValues.putNull(KEY_HUMIDITY);
        initialValues.putNull(KEY_WINDCONDITION);
        initialValues.put(KEY_TIMESTAMP, crntDate);
        initialValues.put(KEY_SERVERID, 0);
        Double manTempDouble=null;
        try {
        	manTempDouble = new Double(manualtemp);
        }
        catch (Exception e){
        	initialValues.putNull(KEY_MANUALTEMP);
        }
        if (manTempDouble!=null)
        	initialValues.put(KEY_MANUALTEMP, new Double(manualtemp));
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    public long createLogEntryNoWeatherOrLocation(Double cricketctemp,
			int numchirps, int numsecs, String manualtemp) {
    	DateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String crntDate = dateFormatISO8601.format(new Date());
    	
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CRICKETCTEMP, cricketctemp);
        initialValues.put(KEY_NUMCHIRPS, numchirps);
        initialValues.put(KEY_NUMSECS, numsecs);
        initialValues.putNull(KEY_LATITUDE);
        initialValues.putNull(KEY_LONGITUDE);
        initialValues.putNull(KEY_LOCATIONACCURACY);
        initialValues.putNull(KEY_WEATHERAPI);
        initialValues.putNull(KEY_CTEMP);
        initialValues.putNull(KEY_CONDITION);
        initialValues.putNull(KEY_HUMIDITY);
        initialValues.putNull(KEY_WINDCONDITION);
        initialValues.put(KEY_TIMESTAMP, crntDate);
        initialValues.put(KEY_SERVERID, 0);
        Log.i("DB", "Manualtemp: "+manualtemp);
        Double manTempDouble=null;
        try {
        	manTempDouble = new Double(manualtemp);
        }
        catch (Exception e){
        	initialValues.putNull(KEY_MANUALTEMP);
        }
        if (manTempDouble!=null)
        	initialValues.put(KEY_MANUALTEMP, new Double(manualtemp));
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all logs in the database
     * 
     * @return Cursor over all logs
     */
    public Cursor fetchAllLogs() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CRICKETCTEMP,
        		KEY_NUMCHIRPS, KEY_NUMSECS, KEY_LATITUDE,
        		KEY_LONGITUDE,KEY_LOCATIONACCURACY,KEY_WEATHERAPI,
        		KEY_CTEMP,KEY_CONDITION,KEY_HUMIDITY,KEY_WINDCONDITION,
        		KEY_TIMESTAMP,KEY_SERVERID, KEY_MANUALTEMP}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the log that matches the given rowId
     * 
     * @param rowId id of log to retrieve
     * @return Cursor positioned to matching log, if found
     * @throws SQLException if log could not be found/retrieved
     */
    public Cursor fetchLog(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CRICKETCTEMP,
                		KEY_NUMCHIRPS, KEY_NUMSECS, KEY_LATITUDE,
                		KEY_LONGITUDE,KEY_LOCATIONACCURACY,KEY_WEATHERAPI,
                		KEY_CTEMP,KEY_CONDITION,KEY_HUMIDITY,KEY_WINDCONDITION,
                		KEY_TIMESTAMP,KEY_SERVERID, KEY_MANUALTEMP}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the log using the details provided. The log to be updated is
     * specified using the rowId, and it is altered to use the
     * values passed in
     * 
     * @param rowId id of log to update
     * @return true if the log was successfully updated, false otherwise
     */
    public boolean updateLog(long rowId, Double ctemp, String condition, 
			String humidity, String windcondition, Double cricketctemp,
			int numchirps, int numsecs, Double latitude, Double longitude,
			Double locationaccuracy, String weatherapi, String timestamp,
			long serverid, String manualtemp) {
        ContentValues args = new ContentValues();
        args.put(KEY_CRICKETCTEMP, cricketctemp);
        args.put(KEY_NUMCHIRPS, numchirps);
        args.put(KEY_NUMSECS, numsecs);
        args.put(KEY_LATITUDE, latitude);
        args.put(KEY_LONGITUDE, longitude);
        args.put(KEY_LOCATIONACCURACY, locationaccuracy);
        args.put(KEY_WEATHERAPI, weatherapi);
        args.put(KEY_CTEMP, ctemp);
        args.put(KEY_CONDITION, condition);
        args.put(KEY_HUMIDITY, humidity);
        args.put(KEY_WINDCONDITION, windcondition);
        args.put(KEY_TIMESTAMP, timestamp);
        args.put(KEY_SERVERID, serverid);
        args.put(KEY_MANUALTEMP, new Double(manualtemp));

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
