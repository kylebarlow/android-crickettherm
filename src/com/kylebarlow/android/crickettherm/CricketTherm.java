package com.kylebarlow.android.crickettherm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class CricketTherm extends Activity {
	// General program todos
	// TODO add link to http://en.wikipedia.org/wiki/Dolbear%27s_Law reference
	// TODO add activity state compliance for mCricket
	// TODO reference http://entnemdept.ufl.edu/walker/buzz/585a.htm
	// TODO http://www.openclipart.org/
	// TODO add EULA and copyright info
	// TODO Add help explaining what information is saved to database
	
	// TODO add weather fetching
	// TODO add data saving feature in sqllite database
	// TODO sync data with master database
	
	// Class todos
	// TODO break out view changing from button onclick listeners, make general update values class
	
	Cricket mCricket;
	// Preferences file variables
	private boolean cTemp=false;
	private boolean mFirstLaunch=true;
	
	// Hard coded constants
	protected static final String PREFS_NAME = "MyPrefsFile";
	protected static final int MINMSECSLOCUPDATE = 10000;
	protected static final int MINMETERSLOCUPDATE = 100;
	
	// Class variables
	protected LocationManager mLocationManager;
	protected LocationListener mLocationListener;
	// True if temperature to be displayed in celsius
	protected Location mCurrentLocation;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCricket = new Cricket();
        loadPrefs();
        
        setContentView(R.layout.main);
        final Button chirpbutton = (Button) findViewById(R.id.chirpbutton);
        chirpbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		mCricket.chirp();
        		TextView temperatureReading = (TextView) findViewById(R.id.temperaturereading);
        		TextView chirpsReading = (TextView) findViewById(R.id.chirpsreading);
        		TextView secondsReading = (TextView) findViewById(R.id.secondsreading);
        		if (mCricket.isTemperatureReady()) {
        			int stringid;
        			double temperature;
        			mCricket.calculateTemperature();
        			if (cTemp){
        				stringid=R.string.degc;
        				temperature=mCricket.getCTemperature();
        			}
        			else {
        				stringid=R.string.degf;
        				temperature=mCricket.getFTemperature();
        			}
        			temperatureReading.setText(String.format("%d "+getText(stringid),Math.round(temperature)));
        		}
        		else {
        			if (temperatureReading.getText()!=getText(R.string.temperature_not_ready)) {
        				makeShortToast((String) getText(R.string.delaytoolong));
        			}
        			temperatureReading.setText(getText(R.string.temperature_not_ready));
        		}
        		chirpsReading.setText(String.format("%d",mCricket.numberOfChirps()));
        		secondsReading.setText(String.format("%d",Math.round(mCricket.elapsedSeconds())));
        	}
        });
        
        final Button resetbutton = (Button) findViewById(R.id.resetbutton);
        resetbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		reset();
        	}
        });
        
        final Button loggerbutton = (Button) findViewById(R.id.loggerbutton);
        loggerbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (mCricket.isTemperatureReady()){
        			Intent i = new Intent(CricketTherm.this,Logger.class);
        			i.putExtra("LOCATION", mCurrentLocation);
        			i.putExtra("CRICKETTEMP", mCricket.getCTemperature());
        			i.putExtra("NUMCHIRPS", mCricket.numberOfChirps());
        			i.putExtra("NUMSECS", mCricket.elapsedSeconds());
        			startActivity(i);
        		}
        		else {
        			makeLongToast((String) getText(R.string.tempnotready));
        		}
        	}
        });
    
        /* //Testing code to detect built in thermometers
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> thermlist = sm.getSensorList(13);
        if (thermlist.isEmpty()==false){
        	Toast.makeText(this, "Thermometer exists",Toast.LENGTH_SHORT).show();
        }
        else {
        	Toast.makeText(this, "No thermometer exists",Toast.LENGTH_SHORT).show();
        } */

        // Acquire a reference to the system Location Manager
        // TODO Check if provider exists and is active
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        // Define a listener that responds to location updates
        mLocationListener = new MyLocationListener();

        // Register the listener with the Location Manager to receive location updates
        // Production code using NETWORK_PROVIDER
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { 
        	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MINMSECSLOCUPDATE, MINMETERSLOCUPDATE, mLocationListener);
        }
        
        // Testing code using GPS_PROVIDER
        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MINMSECSLOCUPDATE, MINMETERSLOCUPDATE, mLocationListener);
        
        // Wunderground key settings
        // http://www.wunderground.com/weather/api/d/8edf4c3cd56d3b83/edit.html
        
        // If first launch, ask for opt-in permission to share data
        if (mFirstLaunch){
        	firstLaunchYesNoAlert();
        }
    }
    
    /* Function to do inital program launch permission asking */
    public void firstLaunchYesNoAlert() {
    	CharSequence title = getText(R.string.firstlaunchtitle);
    	CharSequence message = getText(R.string.firstlaunchmessage);
    	CharSequence yes = getText(R.string.yes);
    	CharSequence no = getText(R.string.no);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case DialogInterface.BUTTON_POSITIVE:
                        	firstLaunch(true);
                        	break;

                        case DialogInterface.BUTTON_NEGATIVE:
                        	firstLaunch(false);
                        	break;	
                    }
                }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setPositiveButton(yes, dialogClickListener).setNegativeButton(no, dialogClickListener).setTitle(title).show();
    }
    
    /* Sets first launch settings */
    private void firstLaunch(Boolean optIn) {
    	SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shareData", optIn);
        mFirstLaunch=false;
        editor.putBoolean("firstLaunch", false);
        editor.putBoolean("cTemp", false);
        editor.putLong("lastWeatherUpdate", 0);

        // Commit the edits!
        editor.commit();
    }
    
    protected final class MyLocationListener implements LocationListener {
    	public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            mCurrentLocation = location;
            /* Testing code
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            newLocation(latitude,longitude);*/
          }

          public void onStatusChanged(String provider, int status, Bundle extras) {}

          public void onProviderEnabled(String provider) {}

          public void onProviderDisabled(String provider) {}
    }
    
    /* Testing code
    private void newLocation(double latitude, double longitude){
        String message = String.format("Lat: %f Long: %f", latitude,longitude);
        makeShortToast(message);
    }*/
    
    private void makeShortToast(String message){
    	Toast.makeText(this, message,Toast.LENGTH_SHORT).show();
    }
    
    private void makeLongToast(String message){
    	Toast.makeText(this, message,Toast.LENGTH_LONG).show();
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
    
    private void loadPrefs(){
    	// Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        cTemp = settings.getBoolean("cTemp", false);
        mFirstLaunch = settings.getBoolean("firstLaunch", true);
    }
    
    private void reset(){
    	mCricket.reset();
		TextView temperatureReading = (TextView) findViewById(R.id.temperaturereading);
		TextView chirpsReading = (TextView) findViewById(R.id.chirpsreading);
		TextView secondsReading = (TextView) findViewById(R.id.secondsreading);
		temperatureReading.setText(getText(R.string.temperature_not_ready));
		chirpsReading.setText(R.string.not_applicable);
		secondsReading.setText(R.string.not_applicable);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        loadPrefs();
        reset();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        loadPrefs();
        reset();
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