package com.kg6.schlafdoedel.speechrecognition;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.kg6.schlafdoedel.Configuration;

public class InformationRequest {

	public static void RequestWeatherInformation(final Activity context) {
		Thread requestingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				String response = ExecuteRequest(String.format(Configuration.TTS_REQUEST_WEATHER_URL, "Linz"));
				
				if(response == null) {
					return;
				}
				
				ExecuteTextToSpeech(context, "WEATHER", Messaging.GetCommandWeatherMessage(response));
			}
			
		});
		
		requestingThread.setName("Schlafdoedel - Request weather information thread");
		requestingThread.start();
	}
	
	public static void RequestNewsInformation(final Context context) {
		Thread requestingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				String response = ExecuteRequest(Configuration.TTS_REQUEST_NEWS_URL);
				
				if(response == null) {
					return;
				}

				ExecuteTextToSpeech(context, "NEWS", Messaging.GetCommandNewsMessage(response));
			}
			
		});
		
		requestingThread.setName("Schlafdoedel - Request news information thread");
		requestingThread.start();
	}
	
	public static void ExecuteTextToSpeech(final Context context, final String requestKey, final String text) {
		Thread requestingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				TextToSpeechWrapper.Speak(context, requestKey, text);
			}
			
		});
		
		requestingThread.setName("Schlafdoedel - Speaking stuff");
		requestingThread.start();
	}
	
	private static String ExecuteRequest(String url) {
		BufferedReader inputReader = null;
		
		HttpClient client = new DefaultHttpClient();
		HttpGet getCommand = new HttpGet(url);
		
		HttpResponse response;
	    
	    try {
	        response = client.execute(getCommand);
	        HttpEntity entity = response.getEntity();
	        
	        if (entity != null) {
	        	inputReader = new BufferedReader(new InputStreamReader(entity.getContent()));
	        	
			    StringBuilder finalString = new StringBuilder();
			    
			    String currentLine = null;
			    
		        while ((currentLine = inputReader.readLine()) != null) {
		        	finalString.append(currentLine + "\n");
		        }
			    
			    return finalString.toString();
	        }
	    } catch (Exception e) {
	    	Log.e("InformationRequest.java", "Unable to execute request", e);
	    } finally {
	    	try {
		    	if(inputReader != null) {
		    		inputReader.close();
		    	}
	    	} catch (Exception e) {
	    		
	    	}
	    }
	    
	    return null;
	}
}
