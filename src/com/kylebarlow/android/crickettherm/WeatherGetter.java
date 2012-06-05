package com.kylebarlow.android.crickettherm;


/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class WeatherGetter{
	
	String mApiToUse;
	
	WeatherGetter(String apiToUse){
		mApiToUse=apiToUse;
	}
	
	protected WeatherData getCurrentWeather(){
		return new WeatherData();
	}

}
