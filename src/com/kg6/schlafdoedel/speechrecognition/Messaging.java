package com.kg6.schlafdoedel.speechrecognition;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kg6.schlafdoedel.Configuration;

import android.util.Log;

public class Messaging {
	private static final Random RANDOM = new Random();
	
	public static String GetCommandActivatedMessage() {
		String[] availableMessages = new String[] {
			"How can I help you, master?",
			"What is it now, master?",
			"You summoned me, master?",
			"Have you called my name, master?",
		};
		
		return GetRandomMessage(availableMessages);
	}
	
	public static String GetCommandNeverMindMessage() {
		String[] availableMessages = new String[] {
			"Ok!",
			"Ok, don't waste my time!",
			"Ok, if you need me, I will be right here!",
		};
		
		return GetRandomMessage(availableMessages);
	}
	
	public static String GetCommandUnknownMessage() {
		String[] availableMessages = new String[] {
			"Sorry, I don't know how to handle this command",
		};
		
		return GetRandomMessage(availableMessages);
	}
	
	public static String GetCommandWeatherMessage(String weatherInformation) {
		StringBuilder currentConditionTextBuilder = new StringBuilder();
		
		try {
			JSONObject weatherData = new JSONObject(weatherInformation);
            JSONObject data = weatherData.getJSONObject("data");
            JSONObject currentCondition = data.getJSONArray("current_condition").getJSONObject(0);
            JSONArray weatherForNextDays = data.getJSONArray("weather");
            
            currentConditionTextBuilder.append("The current condition is ");
            currentConditionTextBuilder.append(currentCondition.getJSONArray("weatherDesc").getJSONObject(0).getString("value"));
            currentConditionTextBuilder.append(" at ");
            currentConditionTextBuilder.append(currentCondition.getString("temp_C"));
            currentConditionTextBuilder.append(" degrees celcius. ");
            
            if (weatherForNextDays.length() > 0) {
            	currentConditionTextBuilder.append("The current forecast for tomorrow is ");
            	currentConditionTextBuilder.append(weatherForNextDays.getJSONObject(0).getJSONArray("weatherDesc").getJSONObject(0).getString("value"));
            	currentConditionTextBuilder.append(" at a minimum of ");
            	currentConditionTextBuilder.append(weatherForNextDays.getJSONObject(0).getString("tempMinC"));
            	currentConditionTextBuilder.append(" degrees and a maximum of ");
            	currentConditionTextBuilder.append(weatherForNextDays.getJSONObject(0).getString("tempMaxC"));
            	currentConditionTextBuilder.append(" degrees celcius. ");
            	
            	if (weatherForNextDays.length() > 1) {
            		currentConditionTextBuilder.append("And finally, the current forecast for the day after tomorrow is ");
            		currentConditionTextBuilder.append(weatherForNextDays.getJSONObject(1).getJSONArray("weatherDesc").getJSONObject(0).getString("value"));
            		currentConditionTextBuilder.append(" at a minimum of ");
            		currentConditionTextBuilder.append(weatherForNextDays.getJSONObject(1).getString("tempMinC"));
            		currentConditionTextBuilder.append(" degrees and a maximum of ");
            		currentConditionTextBuilder.append(weatherForNextDays.getJSONObject(1).getString("tempMaxC"));
            		currentConditionTextBuilder.append(" degrees celcius. ");
	            }
	            else {
	            	currentConditionTextBuilder.append("Sorry dude, the weather report for the following day is not available. ");
	            }
            }
            else {
            	currentConditionTextBuilder.append("Sorry dude, the weather report for the following two days is not available. ");
            }
            
            currentConditionTextBuilder.append("Enjoy your day, dude.");
		} catch (Exception e) {
			Log.e("InformationRequest.java", "Unable to parse weather JSON response", e);
		}
		
		return currentConditionTextBuilder.toString();
	}
	
	public static String GetCommandNewsMessage(String newsInformation) {
		StringBuilder newsTextBuilder = new StringBuilder();
		
		try {
			JSONArray newsData = new JSONObject(newsInformation).getJSONObject("response").getJSONArray("results");
            int numberOfNews = Configuration.TTS_NUMBER_OF_NEWS;
            
            if (newsData.length() < numberOfNews) {
            	numberOfNews = newsData.length();
            }
			
            for (int i = 0; i < numberOfNews; i++) {
            	newsTextBuilder.append(newsData.getJSONObject(i).getString("webTitle") + ". ");
            	newsTextBuilder.append(newsData.getJSONObject(i).getJSONObject("fields").getString("trailText").replaceAll("<.*>", "") + ". ");
            }
            
		} catch (Exception e) {
			Log.e("InformationRequest.java", "Unable to parse weather JSON response", e);
		}
		
		return newsTextBuilder.toString();
	}
	
	private static String GetRandomMessage(String[] messages) {
		return messages[RANDOM.nextInt(messages.length)];
	}
}
