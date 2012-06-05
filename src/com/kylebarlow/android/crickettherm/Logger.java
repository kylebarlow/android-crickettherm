package com.kylebarlow.android.crickettherm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.kylebarlow.android.crickettherm.Cricket;

/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class Logger extends Activity {
	private boolean cTemp=false;
	private Bundle mExtras;
	private double mCricketTemp = 0.0;
	private int mStringId;
	Cricket mCricket;
	WeatherData mWD;
	
	// Hard coded constants
	private static final String APITOUSE = "google";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logger);
        
        mCricket=new Cricket();
        mStringId=0;
        
        loadPrefs();
        loadExtras();
        setText();
		
        final Button exitbutton = (Button) findViewById(R.id.logger_exitbutton);
        exitbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });
        
    }
    
    private void loadExtras(){
        mExtras = this.getIntent().getExtras();
        
        mCricketTemp = mExtras.getDouble("CRICKETTEMP", 0.0);
    }
    
    private void setText(){
    	// TODO change weather getter fetching to be asynchronous
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
    	new Weather().execute(APITOUSE);
    }
    
    private void weatherFetched(WeatherData wd){
    	mWD=wd;
    	Double realTemp;
    	TextView weatherTemp = (TextView) findViewById(R.id.logger_weathertemp);
    	if (cTemp){
			realTemp=wd.getCTemperature();
		}
		else {
			realTemp=wd.getFTemperature();
		}
    	weatherTemp.setText(String.format("%d "+getText(mStringId),Math.round(realTemp)));
    }
    
    private void loadPrefs(){
    	// Restore preferences
        SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        cTemp = settings.getBoolean("cTemp", false);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private class Weather extends AsyncTask<String, Void, WeatherData> {
    	
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
       protected WeatherData doInBackground(String... apiToUse) {
    	   WeatherGetter wg = new WeatherGetter(apiToUse[0]);
    	   return wg.getCurrentWeather();
       }
       
       /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
       protected void onPostExecute(WeatherData wd) {
           weatherFetched(wd);
       }

    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        loadPrefs();
        loadExtras();
        setText();
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
