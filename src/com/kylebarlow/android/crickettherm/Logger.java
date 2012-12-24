package com.kylebarlow.android.crickettherm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class Logger extends Activity {
	private boolean cTemp=false;
	private Bundle mExtras;
	private double mCricketTemp = 0.0;
	private int mNumChirps = 0;
	private double mNumSecs = 0;
	private Location mCurrentLocation;
	private int mStringId;
	private long mLastWeatherUpdate=0;
	Cricket mCricket;
	WeatherData mWD;
	private WeatherGetter mWeatherGetter;
	private DataDBAdapter mDbHelper;
	
	// Hard coded constants
	private static final String APITOUSE = "wunderground";
	private static final String wunderlink = "http://www.wunderground.com/?apiref=9ec642ec0a15ce3c";
	private static final long TIMEBETWEENUPDATES=600000; // Time between fetches in ms
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logger);
        loadPrefs();
        
        mDbHelper = new DataDBAdapter(this);
        
        mWeatherGetter = new WeatherGetter(APITOUSE);
        mWD = new WeatherData();
        loadWeather();
        mCricket=new Cricket();
        mStringId=0;
        
        loadExtras();
		
        final Button exitbutton = (Button) findViewById(R.id.logger_exitbutton);
        exitbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });
        
        final Button logItButton = (Button) findViewById(R.id.logger_logit);
        logItButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		saveLogData();
        	}
        });
        
        final Button viewerButton = (Button) findViewById(R.id.logger_viewerbutton);
        viewerButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Intent i = new Intent(Logger.this,DataViewer.class);
    			startActivity(i);
        	}
        });
        
        final ImageView logo = (ImageView) findViewById(R.id.imageView1);
        logo.setOnTouchListener(new OnTouchListener() {
        	@Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
        		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wunderlink));
        		startActivity(browserIntent);
        		return true;
        	}
        });
    }
    
    private void saveLogData(){
    	Boolean locationReady=true;
    	try {
    		mCurrentLocation.getLatitude();
    	}
    	catch (Exception e){
    		locationReady=false;
    	}
    	final EditText manualTemp = (EditText) findViewById(R.id.logger_manreport);
    	//Log.i("Logger","Mantempfield: "+manualTemp.getText().toString());
        Double manTempDouble;
        try {
        	manTempDouble = Double.valueOf(manualTemp.getText().toString());
        	if (cTemp==false) {
        		manTempDouble = (manTempDouble-32.0)*(5.0/9.0);
        	}
        }
        catch (Exception e){
        	manTempDouble=null;
        }
        mDbHelper.open();
    	if (mWD.mDataReady&&(locationReady)){
    		mDbHelper.createLogEntry(mWD.getCTemperature(), mWD.mCondition, 
    				mWD.mHumidity, mWD.mWindCondition, mCricketTemp,
    				mNumChirps, mNumSecs, mCurrentLocation.getLatitude(), 
    				mCurrentLocation.getLongitude(),
    				Double.valueOf(mCurrentLocation.getAccuracy()), APITOUSE,
    				manTempDouble);
    	}
    	else if ((mWD.mDataReady==false)&&locationReady){
    		mDbHelper.createLogEntryNoWeather(mCricketTemp, mNumChirps, mNumSecs,
    				mCurrentLocation.getLatitude(), 
    				mCurrentLocation.getLongitude(),
    				Double.valueOf(mCurrentLocation.getAccuracy()),
    				manTempDouble);
    	}
    	else if ((mWD.mDataReady==true)&&(locationReady==false)){
    		mDbHelper.createLogEntryNoLocation(mWD.getCTemperature(), mWD.mCondition, 
    				mWD.mHumidity, mWD.mWindCondition, mCricketTemp, 
    				mNumChirps, mNumSecs, APITOUSE, manTempDouble);
    	}
    	else {
    		mDbHelper.createLogEntryNoWeatherOrLocation(mCricketTemp,
    				mNumChirps, mNumSecs, manTempDouble);
    	}
    	mDbHelper.close();
    	Toast.makeText(this, getText(R.string.datalogged),Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mWeatherGetter;
    }
    
    private void loadExtras(){
        mExtras = this.getIntent().getExtras();
        
        mCricketTemp = mExtras.getDouble("CRICKETTEMP", 0.0);
    	mNumChirps = mExtras.getInt("NUMCHIRPS", 0);
    	mNumSecs = mExtras.getDouble("NUMSECS",0);
    	mCurrentLocation = (Location) mExtras.get("LOCATION");
    }
    
    private void setText(){
        double cricketTemp;
        if (cTemp){
			mStringId=R.string.degc;
			cricketTemp=mCricketTemp;
		}
		else {
			mStringId=R.string.degf;
			cricketTemp=mCricket.convertCToF(mCricketTemp);
		}
        TextView cricketTempText = (TextView) findViewById(R.id.logger_temperaturereading);
        TextView manReportDeg = (TextView) findViewById(R.id.logger_manreportdeg);
        
		cricketTempText.setText(String.format("%d "+getText(mStringId),Math.round(cricketTemp)));
		manReportDeg.setText(getText(mStringId));
		
		fetchWeather();
    }
    
    private void fetchWeather(){
		if ((System.currentTimeMillis()-mLastWeatherUpdate)<TIMEBETWEENUPDATES){
			// Too soon to last update
			//Log.i("Logger", "Using cached weather data (if exists)");
		}
		else {
			//Log.i("Logger", "Fetching fresh weather");
			new Weather(mCurrentLocation,mWeatherGetter).execute(APITOUSE);
		}
		if (mWD.mDataReady)
    		updateWeatherText();
    }
    
    private void weatherFetched(WeatherData wd){
    	if (wd.mDataReady){
    		mWD=wd;
    		saveWeather();
    	}
    	else {
    		if (mWD.mDataReady==false)
    			return;
    	}
    	updateWeatherText();
    }
    
    private void updateWeatherText(){
    	Double realTemp;
    	TextView weatherTemp = (TextView) findViewById(R.id.logger_weathertemp);
    	if (cTemp){
			realTemp=mWD.getCTemperature();
		}
		else {
			realTemp=mWD.getFTemperature();
		}
    	weatherTemp.setText(String.format("%d "+getText(mStringId),Math.round(realTemp)));
    }
    
    private void loadPrefs(){
    	// Restore preferences
        SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        cTemp = settings.getBoolean("cTemp", false);
        mLastWeatherUpdate = settings.getLong("lastWeatherUpdate", 0);
    }
    
    private void savePrefs(){
    	SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastWeatherUpdate", mLastWeatherUpdate);

        // Commit the edits!
        editor.commit();
    }
    
    private void loadWeather(){
    	// Restore preferences
        SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        Boolean dataready = settings.getBoolean("savedDataReady", false);
        if (dataready) {
        	Double ctemp = Double.valueOf(settings.getFloat("savedCTemp", 0));
        	Double ftemp = Double.valueOf(settings.getFloat("savedFTemp", 0));
        	String condition = settings.getString("savedCondition","");
        	String humidity = settings.getString("savedHumidity", "");
        	String windcondition = settings.getString("savedWindCondition", "");
        	mWD = new WeatherData(ctemp,ftemp,condition,humidity,windcondition);
        }
        else {
        	return;
        }
    }
    
    private void saveWeather(){
    	if (mWD.mDataReady==false){
    		return;
    	}
    	SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("savedCTemp", new Float(mWD.getCTemperature()));
        editor.putFloat("savedFTemp", new Float(mWD.getFTemperature()));
        editor.putString("savedCondition", mWD.mCondition);
        editor.putString("savedHumidity", mWD.mHumidity);
        editor.putString("savedWindCondition", mWD.mWindCondition);
        editor.putBoolean("savedDataReady", true);

        // Commit the edits!
        editor.commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.options:
            	Intent i = new Intent(this,OptionsMenu.class);
        		startActivity(i);
                return true;
            case R.id.dataviewer:
            	Intent i2 = new Intent(this,DataViewer.class);
        		startActivity(i2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private class Weather extends AsyncTask<String, Void, WeatherData> {
    	
    	Location mCurrentLocation;
    	WeatherGetter mWeatherGetter;
    	
    	Weather(Location currentLocation, WeatherGetter wg){
    		mCurrentLocation=currentLocation;
    		mWeatherGetter = wg;
    	}
    	
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
       protected WeatherData doInBackground(String... params) {
    	   return mWeatherGetter.getCurrentWeather(mCurrentLocation);
       }
       
       /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
       protected void onPostExecute(WeatherData wd) {
    	   if (wd.mDataReady){
    		   mLastWeatherUpdate=System.currentTimeMillis();
    		   savePrefs();
    	   }
           weatherFetched(wd);
       }

    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        loadPrefs();
        loadExtras();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        loadPrefs();
        loadExtras();
        setText();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }
}
