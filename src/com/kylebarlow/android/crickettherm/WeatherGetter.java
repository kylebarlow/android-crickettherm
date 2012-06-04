package com.kylebarlow.android.crickettherm;



/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class WeatherGetter {
	
	private boolean mWeatherReady=false;
	private double mCurrentCTemp;
	protected long mLastTempTime;
	
	// Interval to wait between fetching weather updates from API
	protected static double WEATHER_UPDATE_INTERVAL = 300000;

	public WeatherGetter() {
		mLastTempTime=0;
	}
	
	public WeatherGetter(long lastTempTime) {
		mLastTempTime=lastTempTime;
	}
	
	public double getCTemperature(){
		// TODO stub
		if (mWeatherReady && (((new java.util.Date().getTime()-mLastTempTime)-WEATHER_UPDATE_INTERVAL)>0)){
			return mCurrentCTemp;
		}
		else {
			fetchWeather();
			return mCurrentCTemp;
		}
	}
	
	private void fetchWeather(){
		// TODO stub
		mWeatherReady=true;
		mLastTempTime=new java.util.Date().getTime();
		mCurrentCTemp=37.0; //remove
	}
}
