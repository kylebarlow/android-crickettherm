package com.kylebarlow.android.crickettherm;

import com.kylebarlow.android.crickettherm.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;

public class CricketTherm extends Activity {
	// General program todos
	// TODO add config setting to choose between celsius and fahrenheit display
	// TODO Menu buttons
	// TODO add link to http://en.wikipedia.org/wiki/Dolbear%27s_Law reference
	// TODO add activity state compliance
	// TODO reference http://entnemdept.ufl.edu/walker/buzz/585a.htm
	// TODO http://www.openclipart.org/
	// TODO add EULA and copyright info
	// TODO add data saving feature in sqllite database
	// TODO sync data with master database
	
	// Class todos
	// TODO break out view changing from button onclick listeners, make general update values class
	
	Cricket mCricket;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCricket = new Cricket();
        
        
        setContentView(R.layout.main);
        final Button chirpbutton = (Button) findViewById(R.id.chirpbutton);
        chirpbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		mCricket.chirp();
        		TextView temperatureReading = (TextView) findViewById(R.id.temperaturereading);
        		TextView chirpsReading = (TextView) findViewById(R.id.chirpsreading);
        		TextView secondsReading = (TextView) findViewById(R.id.secondsreading);
        		if (mCricket.isTemperatureReady())
        			temperatureReading.setText(String.format("%d",Math.round(mCricket.getFTemperature())));
        		chirpsReading.setText(String.format("%d",mCricket.numberOfChirps()));
        		secondsReading.setText(String.format("%d",Math.round(mCricket.elapsedSeconds())));
        	}
        });
        
        final Button resetbutton = (Button) findViewById(R.id.resetbutton);
        resetbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		mCricket.reset();
        		TextView temperatureReading = (TextView) findViewById(R.id.temperaturereading);
        		TextView chirpsReading = (TextView) findViewById(R.id.chirpsreading);
        		TextView secondsReading = (TextView) findViewById(R.id.secondsreading);
        		temperatureReading.setText(getText(R.string.temperature_not_ready));
        		chirpsReading.setText(R.string.not_applicable);
        		secondsReading.setText(R.string.not_applicable);
        	}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
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