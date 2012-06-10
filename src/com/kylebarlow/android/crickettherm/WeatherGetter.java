package com.kylebarlow.android.crickettherm;

import java.net.URL;
import java.text.DecimalFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.location.Location;


/**
 * @author Kyle Barlow
 * kylebarlow.com
 * 
 */
public class WeatherGetter {
	
	private String mApiToUse;
	private Double mLatitude;
	private Double mLongitude;
	//private WeatherData mWeatherData = new WeatherData();
	//private long mLastWeatherUpdate=0;
	
	private static final String GOOGLEBASEURL="http://www.google.com/ig/api?weather=,,,";
	//private static final long TIMEBETWEENUPDATES=300000; // Time between fetches in ms
	
	WeatherGetter(String apiToUse){
		mApiToUse=apiToUse;
	}
	
	/*
	WeatherGetter(String apiToUse, long lastWeatherUpdate){
		mApiToUse=apiToUse;
		mLastWeatherUpdate = lastWeatherUpdate;
	}*/
	
	protected WeatherData getCurrentWeather(Location currentLocation){
		
		//Log.i("WeatherGetter", "Launching");
		
		//mLastWeatherUpdate=System.currentTimeMillis();
		if (currentLocation==null)
			return new WeatherData();
		mLatitude=currentLocation.getLatitude();
		mLongitude=currentLocation.getLongitude();
		if (mApiToUse.equalsIgnoreCase("google")){
			//Log.i("WeatherGetter", "Getting google weather");
			return getGoogleWeather();
		}
		return new WeatherData();
	}
	
	private String convertLatLongToGoog(Double coord){
		return new DecimalFormat("#.######").format(coord).replace(".", "");
	}
	
	private WeatherData getGoogleWeather(){
		
		String latitude = convertLatLongToGoog(mLatitude);
		String longitude = convertLatLongToGoog(mLongitude);
		String url=GOOGLEBASEURL+latitude+","+longitude;
		
		// Based on http://tutorials.jenkov.com/java-xml/dom-document-object.html
		
		DocumentBuilderFactory builderFactory =
		        DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
		    builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		    e.printStackTrace();  
		}
		
		Document document;
		try {
		    document = builder.parse(new URL(url).openStream());
		} catch (Exception e) {
			e.printStackTrace();
			//Log.e("WeatherGetter", latitude);
			//Log.e("WeatherGetter", longitude);
			return new WeatherData();
		}
		
		Element rootElement = document.getDocumentElement();
		Element weatherElement = getNode(rootElement,"weather");
		if (weatherElement==null) {
			//Log.i("XMLParse", "no match in: "+rootElement.getNodeName());
			return new WeatherData();
		}
		//Log.i("XMLParse", "Found node: "+weatherElement.getNodeName());
		
		Element currentConditionsElement = getNode(weatherElement,"current_conditions");
		if (currentConditionsElement==null) {
			return new WeatherData();
		}
		
		NodeList conditionNodes = currentConditionsElement.getChildNodes();
		
		String condition="";
		String temp_f_str=null;
		String temp_c_str=null;
		String humidity="";
		String wind_condition="";
		Double temp_f;
		Double temp_c;
		
		// Save conditions
		for(int i=0; i<conditionNodes.getLength(); i++){
			  Node node = conditionNodes.item(i);

			  if(node.getNodeName().equals("condition")){
				  Element child = (Element) node;
				  condition = child.getAttribute("data");
				  //Log.i("XMLParse", "condition = "+condition);
			  }
			  
			  if(node.getNodeName().equals("temp_f")){
				  Element child = (Element) node;
				  temp_f_str = child.getAttribute("data");
			  }
			  
			  if(node.getNodeName().equals("temp_c")){
				  Element child = (Element) node;
				  temp_c_str = child.getAttribute("data");
			  }
			  
			  if(node.getNodeName().equals("humidity")){
				  Element child = (Element) node;
				  humidity = child.getAttribute("data");
			  }
			  
			  if(node.getNodeName().equals("wind_condition")){
				  Element child = (Element) node;
				  wind_condition = child.getAttribute("data");
			  }
			}
		
		// Both temperature reading failed, return empty weatherdata
		if ((temp_c_str==null)&&(temp_f_str==null))
			return new WeatherData();
		
		// Only temp_c is null
		if (temp_c_str==null) {
			temp_f=new Double(temp_f_str);
			temp_c = (temp_f - 32.0) * (5.0/9.0); 
		}
		// Only temp_f is null
		else if (temp_f_str==null) {
			temp_c=new Double(temp_c_str);
			temp_f = (temp_c * 1.8) + 32.0;
		}
		// Neither are null
		else {
			temp_f=new Double(temp_f_str);
			temp_c=new Double(temp_c_str);
		}
		
		//mLastWeatherUpdate = System.currentTimeMillis();
		return new WeatherData(temp_c, temp_f, condition, humidity, wind_condition);
	}
	
	private Element getNode(Element element, String nodeName){
		NodeList nodes = element.getChildNodes();
		
		for(int i=0; i<nodes.getLength(); i++){
			  Node node = nodes.item(i);

			  if(node.getNodeName().equals(nodeName)){
			    return (Element) node;
			  }
			}
		return null;
	}
}
