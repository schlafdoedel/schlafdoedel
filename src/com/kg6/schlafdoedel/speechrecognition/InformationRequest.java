package com.kg6.schlafdoedel.speechrecognition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

import com.kg6.schlafdoedel.Configuration;
import com.kg6.schlafdoedel.event.EventScheduler;

public class InformationRequest implements OnInitListener {

	public static void RequestWeatherInformation(final Activity context) {
		Thread requestingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				String response = ExecuteRequest(String.format(Configuration.TTS_REQUEST_WEATHER_URL, "Linz"));
				
				if(response == null) {
					return;
				}
				
				StringBuilder currentConditionTextBuilder = new StringBuilder();
				
				try {
					JSONObject weatherData = new JSONObject(response);
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
				
				ExecuteTextToSpeech(context, "WEATHER", currentConditionTextBuilder.toString());
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
				
				StringBuilder newsTextBuilder = new StringBuilder();
				
				try {
					JSONArray newsData = new JSONObject(response).getJSONObject("response").getJSONArray("results");
		            int numberOfNews = 3;
		            
		            if (newsData.length() < 3) {
		            	numberOfNews = newsData.length();
		            }
					
		            for (int i = 0; i < numberOfNews; i++) {
		            	newsTextBuilder.append(newsData.getJSONObject(i).getString("webTitle") + ". ");
		            	newsTextBuilder.append(newsData.getJSONObject(i).getJSONObject("fields").getString("trailText").replaceAll("<.*>", "") + ". ");
		            }
		            
				} catch (Exception e) {
					Log.e("InformationRequest.java", "Unable to parse weather JSON response", e);
				}
				
				ExecuteTextToSpeech(context, "NEWS", newsTextBuilder.toString());
			}
			
		});
		
		requestingThread.setName("Schlafdoedel - Request news information thread");
		requestingThread.start();
	}
	
	@SuppressWarnings("deprecation")
	public static void ExecuteTextToSpeech(final Context context, final String requestKey, final String text) {
		final float previousEventAudioVolume = GetMusicVolume();
		
		try {
			ChangeMusicVolume(0);
			
	        final TextToSpeech tts = new TextToSpeech(context, new OnInitListener() {
				
				@Override
				public void onInit(int status) {
					if (status != TextToSpeech.SUCCESS) {
						Log.v("InformationRequest.java", "Unable to initialize text to speech");
					}
				}
				
			});
	        
	        tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
				
				@Override
				public void onUtteranceCompleted(String utteranceId) {
					if(utteranceId.compareTo(requestKey) != 0) {
						return;
					}
					
					tts.stop();
					tts.shutdown();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						
					}
			        
			        ChangeMusicVolume(previousEventAudioVolume);
				}
			});
	        
	        HashMap<String, String> ttsHash = new HashMap<String, String>();
	        ttsHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
	        ttsHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, requestKey);
	        
	        tts.setLanguage(Locale.UK);
	        tts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsHash);
		} catch (Exception e) {
			Log.e("InformationRequest.java", "Unable to execute text to speech", e);
			
			ChangeMusicVolume(previousEventAudioVolume);
		}
	}
	
	private static void ChangeMusicVolume(float volume) {
		EventScheduler scheduler = EventScheduler.CreateInstance();
        
        if(scheduler != null) {
        	scheduler.setEventAudioVolume(volume);
        }
	}
	
	private static float GetMusicVolume() {
		EventScheduler scheduler = EventScheduler.CreateInstance();
        
        if(scheduler != null) {
        	return scheduler.getEventAudioVolume();
        }
        
        return 0;
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

	@Override
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
