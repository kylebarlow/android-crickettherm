package com.kylebarlow.android.crickettherm;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class DataViewer extends ListActivity {
	//TODO Fix deleting items bug where display is incorrect
	// TODO Add manual temp display, numchirps, and numsecs
	
	private DataDBAdapter mDbHelper;
	private static final int DELETE_ID = Menu.FIRST;
	private Cursor mLogCursor;
	Boolean cTemp;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dataviewer);
        loadPrefs();
        mDbHelper = new DataDBAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }
   
    protected Dialog onCreateDialog(int position) {
    	Cursor c = mLogCursor;
        c.moveToPosition(position);
        
        Context mContext = this;
        Dialog dialog = new Dialog(mContext);

        dialog.setContentView(R.layout.datumviewer);
        dialog.setTitle(getText(R.string.viewer));
        
        TextView crickettempd = (TextView) dialog.findViewById(R.id.datum_crickettempd);
        TextView weathertempd = (TextView) dialog.findViewById(R.id.datum_weathertempd);
        Double cricketTemp;
        Double weatherTemp;
        int mStringId;
        if (cTemp){
			mStringId=R.string.degc;
			cricketTemp=c.getDouble(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CRICKETCTEMP));
			weatherTemp=c.getDouble(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CTEMP));
		}
		else {
			mStringId=R.string.degf;
			cricketTemp=(c.getDouble(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CRICKETCTEMP))*1.8)+32.0;
			weatherTemp=(c.getDouble(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CTEMP))*1.8)+32.0;
		}

        if(c.isNull(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CTEMP))) {
        	crickettempd.setText(String.format("%d "+getText(mStringId),Math.round(cricketTemp)));
        }
        else {
        	crickettempd.setText(String.format("%d "+getText(mStringId),Math.round(cricketTemp)));
        	weathertempd.setText(String.format("%d "+getText(mStringId),Math.round(weatherTemp)));
        }
        
        TextView conditiond = (TextView) dialog.findViewById(R.id.datum_conditiond);
        conditiond.setText(c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_CONDITION)));
        
        TextView humidityd = (TextView) dialog.findViewById(R.id.datum_humidityd);
        humidityd.setText(c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_HUMIDITY)));
        
        TextView windconditiond = (TextView) dialog.findViewById(R.id.datum_windconditiond);
        windconditiond.setText(c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_WINDCONDITION)));
        
        TextView latituded = (TextView) dialog.findViewById(R.id.datum_latituded);
        latituded.setText(String.format("%f", c.getDouble(c.getColumnIndexOrThrow(DataDBAdapter.KEY_LATITUDE))));
        
        TextView longituded = (TextView) dialog.findViewById(R.id.datum_longituded);
        longituded.setText(String.format("%f", c.getDouble(c.getColumnIndexOrThrow(DataDBAdapter.KEY_LONGITUDE))));
        
        TextView timestampd = (TextView) dialog.findViewById(R.id.datum_timestampd);
        timestampd.setText(c.getString(c.getColumnIndexOrThrow(DataDBAdapter.KEY_TIMESTAMP)));
        
        return dialog;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        showDialog(position);
    }
    
    private void loadPrefs(){
    	// Restore preferences
        SharedPreferences settings = getSharedPreferences(CricketTherm.PREFS_NAME, 0);
        cTemp = settings.getBoolean("cTemp", false);
    }
    
    private void fillData() {
        // Get all of the notes from the database and create the item list
    	mDbHelper.close();
    	mDbHelper = new DataDBAdapter(this);
        mDbHelper.open();
        mLogCursor = mDbHelper.fetchAllLogs();
        startManagingCursor(mLogCursor);

        String[] from = new String[] { DataDBAdapter.KEY_TIMESTAMP };
        int[] to = new int[] { R.id.text1};
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter logs =
            new SimpleCursorAdapter(this, R.layout.viewer_row, mLogCursor, from, to);
        setListAdapter(logs);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteLog(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        loadPrefs();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        loadPrefs();
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
        mDbHelper.close();
        
    }
	
}
