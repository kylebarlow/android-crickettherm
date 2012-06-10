package com.kylebarlow.android.crickettherm;



/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class WeatherData {
	
	protected Boolean mDataReady;
	private Double mCTemp;
	@SuppressWarnings("unused")
	private Double mFTemp;
	protected String mCondition;
	protected String mHumidity;
	protected String mWindCondition;
	
	WeatherData() {
		mDataReady = false;
	}
	
	WeatherData(Double cTemp, Double fTemp, String condition, 
			String humidity, String wind_condition){
		mCTemp = cTemp;
		mFTemp = fTemp;
		// TODO Could possibly check here to see if cTemp about equals fTemp
		mCondition=condition;
		mHumidity = humidity;
		mWindCondition = wind_condition;
		mDataReady = true;
	}
	
	protected Double getCTemperature(){
		return mCTemp;
	}
	
	protected Double getFTemperature(){
		return convertCToF(mCTemp);
	}
	
	protected double convertCToF(double cTempToConvert){
		// converts given temperature in celsius to fahrenheit
		return (cTempToConvert*1.8)+32.0;
	}

}
