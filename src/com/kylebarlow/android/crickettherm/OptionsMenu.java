package com.kylebarlow.android.crickettherm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class OptionsMenu extends Activity {
	private boolean cTemp=false;
	private boolean mShareData=false;
	private RadioButton fbutton;
	private RadioButton cbutton;
	private CheckBox shareDataBox;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.optionsmenu);
        
        loadPrefs();
        
        final Button exitbutton = (Button) findViewById(R.id.optionmenu_exitbutton);
        exitbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });
        
        fbutton = (RadioButton) findViewById(R.id.optionmenu_fbutton);
        fbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		cTemp=false;
        		savePrefs();
        	}
        });
        
        cbutton = (RadioButton) findViewById(R.id.optionmenu_cbutton);
        cbutton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		cTemp=true;
        		savePrefs();
        	}
        });
        
        shareDataBox = (CheckBox) findViewById(R.id.optionmenu_shareData);
        shareDataBox.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		mShareData=shareDataBox.isChecked();
        		savePrefs();
        	}
        });
        
        setButtons();
    }
    
    private void loadPrefs(){
    	// Restore preferences
        SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        cTemp = settings.getBoolean("cTemp", false);
        mShareData = settings.getBoolean("shareData", false);
    }
    
    private void savePrefs(){
    	SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("cTemp", cTemp);
        editor.putBoolean("shareData", mShareData);

        // Commit the edits!
        editor.commit();
    }
    
    protected void setButtons(){
    	// Set button states from preferences
        if (cTemp) {
        	cbutton.setChecked(true);
        	fbutton.setChecked(false);
        }
        else {
        	cbutton.setChecked(false);
        	fbutton.setChecked(true);
        }
        
        if(mShareData){
        	shareDataBox.setChecked(true);
        }
        else {
        	shareDataBox.setChecked(false);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        setButtons();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        setButtons();
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
