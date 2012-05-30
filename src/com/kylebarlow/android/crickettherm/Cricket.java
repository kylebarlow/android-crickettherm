package com.kylebarlow.android.crickettherm;

import 	android.os.SystemClock;

/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class Cricket{

	private boolean mIsChirping;
	private long mChirpStartTime;
	private double mTemperature;
	private boolean mIsTemperatureReady;
	private int mNumberChirps;
	private int mNumberChirpsAtLastTempCalc;
	private double mPreviousTemperature;
	static private final int mNumberOfSecondsToChirp = 5; // minimum number of seconds of chirping needed before temperature is ready
	static private final double mAcceptableTempChange = 2.0; // calculated temperature is ready to be returned when this is the abs difference with the last calculated temperature
	static private final int mMinimumNumberOfChirps = 4; // minimum number of chirps needed to calculate temperature
	
	/**
	 * 
	 */
	public Cricket() {
		reset();
	}
	
	public void chirp(){
		// This function is called once per cricket chirp
		// Checks if cricket is already chirping. If not, initializes cricket
		if(isChirping()==false){
			mChirpStartTime = SystemClock.uptimeMillis();
			mIsChirping = true;
		}
		mNumberChirps++;
		calculateTemperature();
	}
	
	public boolean isChirping(){
		// Returns true/false depending on if this cricket is currently chirping
		return mIsChirping;
	}
	
	public double getCTemperature(){
		// returns temperature in celsius
		return getTemperature();
	}
	
	public double getFTemperature(){
		// returns temperature in fahrenheit
		return (getTemperature()*1.8)+32.0;
	}
	public boolean isTemperatureReady(){
		// If temperature is already ready, returns true. If not, checks to see if readiness conditions
		// are met and if so, returns true
		if(mIsTemperatureReady)
			return true;
		double elapsedMillis = elapsedMillis();
		if(mIsChirping&&(elapsedMillis/1000>=mNumberOfSecondsToChirp)
				&&
				(java.lang.StrictMath.abs(mTemperature-mPreviousTemperature)<=mAcceptableTempChange)
				&&
				mNumberChirps>=mMinimumNumberOfChirps){
			mIsTemperatureReady=true;
			//mIsChirping=false;
			return true;
		}
		return false;
	}
	
	public void reset(){
		mIsChirping = false;
		mChirpStartTime = -1;
		mTemperature=0;
		mIsTemperatureReady=false;
		mNumberChirps=0;
		mPreviousTemperature=0;
		mNumberChirpsAtLastTempCalc=0;
	}
	
	public int numberOfChirps(){
		return mNumberChirps;
	}
	
	public double elapsedSeconds(){
		return elapsedMillis()/1000;
	}
	
	private void calculateTemperature(){
		if(mNumberChirpsAtLastTempCalc==mNumberChirps)
			return; // Nothing needs to be done, chirp number has not changed
		double elapsedMinutes = elapsedMillis()/60000;
		if(elapsedMinutes<0) { //checks for uptime clock reset, which can happen occasionally to prevent overflow
			reset(); //unpredictable functionality
		}
		mPreviousTemperature=mTemperature;
		mNumberChirpsAtLastTempCalc=mNumberChirps;
		mTemperature = 10.0+((mNumberChirps/elapsedMinutes-40.0)/7.0);
	}
	
	private double elapsedMillis(){
		return SystemClock.uptimeMillis()-mChirpStartTime;
	}
	
	private double getTemperature(){
		// Before calling this function, it is important to check isTemperatureReady
		// Otherwise, returns temperature in celsius
		calculateTemperature();
		return mTemperature;
	}
}
