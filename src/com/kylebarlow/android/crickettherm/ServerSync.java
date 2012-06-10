package com.kylebarlow.android.crickettherm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


/**
 * Syncs internal database with server
 */
public class ServerSync {

	// Hard coded constants
	// URL for the post command to save data
	private static final String POSTURL = "http://cricketthermometer.appspot.com/datasubmit"; 
	private static final String SUCCESS = "<html><body>SUCCESS";
	
	private DataDBAdapter mDbHelper;
	private Context mCtx;
	
	ServerSync(Context ctx){
		mDbHelper = new DataDBAdapter(ctx);
		mCtx=ctx;
	}
	
	public void syncData(){
		new SyncAll().execute("");
	}
	
	/* Loops through database and checks to see if all entries are synced */
    private class SyncAll extends AsyncTask<String, Void, Integer> {
    	
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
       protected Integer doInBackground(String... params) {
    	   int numberSynced = 0;
    	   long rowId=0;
    	   mDbHelper.open();
    	   Cursor allLogsCursor = mDbHelper.fetchAllLogsToSync();
    	   
    	   if (allLogsCursor.moveToFirst())
    	    {
    	        do {
    	        	rowId = allLogsCursor.getLong(allLogsCursor.getColumnIndexOrThrow(DataDBAdapter.KEY_ROWID));
    	            if(postData(rowId)){
    	            	mDbHelper.updateSyncedStatusTrue(rowId);
    	            	numberSynced+=1;
    	            }
    	        } while (allLogsCursor.moveToNext());
    	    }
    	   else{
    		   Log.i("ServerSync","None to sync");
    	   }
    	   mDbHelper.close();
    	   return numberSynced;
       }
       
       /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
       protected void onPostExecute(Integer numberSynced) {
    	   Log.i("ServerSync","Synced "+numberSynced);
       }

    }
	
    public Boolean postData(Long rowId) {
    	// see http://androidsnippets.com/executing-a-http-post-request-with-httpclient
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(POSTURL);
        Cursor c = mDbHelper.fetchLog(rowId);
        Log.i("ServerSync","Syncing row"+rowId);
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("rowid", rowId.toString()));
            nameValuePairs.add(new BasicNameValuePair("cricketctemp", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CRICKETCTEMP))));
            nameValuePairs.add(new BasicNameValuePair("numchirps", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_NUMCHIRPS))));
            nameValuePairs.add(new BasicNameValuePair("numsecs", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_NUMSECS))));
            nameValuePairs.add(new BasicNameValuePair("latitude", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_LATITUDE))));
            nameValuePairs.add(new BasicNameValuePair("longitude", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_LONGITUDE))));
            nameValuePairs.add(new BasicNameValuePair("locationaccuracy", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_LOCATIONACCURACY))));
            nameValuePairs.add(new BasicNameValuePair("weatherapi", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_WEATHERAPI))));
            nameValuePairs.add(new BasicNameValuePair("ctemp", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CTEMP))));
            nameValuePairs.add(new BasicNameValuePair("condition", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CONDITION))));
            nameValuePairs.add(new BasicNameValuePair("humidity", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_HUMIDITY))));
            nameValuePairs.add(new BasicNameValuePair("windcondition", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_WINDCONDITION))));
            nameValuePairs.add(new BasicNameValuePair("timestamp", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_TIMESTAMP))));
            Log.i("ServerSync","Manualtemp to sync(c):"+c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_MANUALTEMP)));
            nameValuePairs.add(new BasicNameValuePair("manualtemp", c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_MANUALTEMP))));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            StringBuilder response=null;
            // Execute HTTP Post Request
            try {
            	response = inputStreamToString(httpclient.execute(httppost).getEntity().getContent());
            }
            catch (Exception e){
            	
            }
            if (response!=null){
            	Log.i("ServerSync","Response="+response);
            	if (new String(response).startsWith(SUCCESS)){
            		return true;
            	}
            }
            
        } catch (IOException e) {
            return false;
        }
        return false;
    } 

    private StringBuilder inputStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();
        
        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        try {
        while ((line = rd.readLine()) != null) { 
            total.append(line); 
        }
        } catch(Exception e) {
        	
        }
        
        // Return full string
        return total;
    }
    
    protected void finalize() throws Throwable {
        try {
            
        } finally {
            super.finalize();
        }
    }
}
