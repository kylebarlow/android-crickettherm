/*******************************************************************************
 * Copyright (c) 2013 Kyle Barlow.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Kyle Barlow - initial API and implementation
 ******************************************************************************/
package com.kylebarlow.android.crickettherm;

import 	android.os.SystemClock;

/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class Cricket{

	protected boolean mChirpTimeout;
	private boolean mIsChirping;
	private long mChirpStartTime;
	private long mLastChirpTime;
	private double mTemperature;
	private boolean mIsTemperatureReady;
	private int mNumberChirps;
	private int mNumberChirpsAtLastTempCalc;
	private double mPreviousTemperature;
	static private final int mNumberOfSecondsToChirp = 4; // minimum number of seconds of chirping needed before temperature is ready
	static private final double mAcceptableTempChange = 2.0; // calculated temperature is ready to be returned when this is the abs difference with the last calculated temperature
	static private final int mMinimumNumberOfChirps = 4; // minimum number of chirps needed to calculate temperature
	static private final int mMaxWaitTimeBetweenChirps = 5000; // max time to allow between user chirps in ms
	
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
		else if((isChirping()==true)&&( (mLastChirpTime+mMaxWaitTimeBetweenChirps)<SystemClock.uptimeMillis() )) {
			reset();
			mChirpTimeout=true;
			mChirpStartTime = SystemClock.uptimeMillis();
			mIsChirping = true;
		}
		mNumberChirps++;
		mLastChirpTime = SystemClock.uptimeMillis();
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
		return convertCToF(getTemperature());
	}
	
	public double convertCToF(double cTempToConvert){
		// converts given temperature in celsius to fahrenheit
		return (cTempToConvert*1.8)+32.0;
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
		mChirpTimeout=false;
	}
	
	public int numberOfChirps(){
		return mNumberChirps;
	}
	
	public double elapsedSeconds(){
		return elapsedMillis()/1000;
	}
	
	public void calculateTemperature(){
		if(mNumberChirpsAtLastTempCalc==mNumberChirps)
			return; // Nothing needs to be done, chirp number has not changed
		double elapsedMinutes = elapsedMillis();
		elapsedMinutes = elapsedMinutes/60000;
		if(elapsedMinutes<0) { //checks for uptime clock reset, which can happen occasionally to prevent overflow
			reset(); //unpredictable functionality
			return;
		}
		mPreviousTemperature=mTemperature;
		mNumberChirpsAtLastTempCalc=mNumberChirps;
		mTemperature = 10.0+((mNumberChirps/elapsedMinutes-40.0)/7.0);
	}
	
	private double elapsedMillis(){
		return mLastChirpTime-mChirpStartTime;
	}
	
	private double getTemperature(){
		// Before calling this function, it is important to check isTemperatureReady
		// Otherwise, returns temperature in celsius
		return mTemperature;
	}
}
