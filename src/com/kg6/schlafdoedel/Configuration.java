package com.kg6.schlafdoedel;

import com.kg6.schlafdoedel.custom.Util;
import com.kg6.schlafdoedel.event.EventSource;
import com.kg6.schlafdoedel.event.EventSource.SourceType;

public class Configuration {
	public static final String COMMAND_SLEEPING_PHASE_AWAKE = "COMMAND_SLEEPING_PHASE_AWAKE";
	public static final String COMMAND_SLEEPING_PHASE_DEEP = "COMMAND_SLEEPING_PHASE_DEEP";
	public static final String COMMAND_SLEEPING_PHASE_SHALLOW = "COMMAND_SLEEPING_PHASE_SHALLOW";
	
	public static final String TTS_REQUEST_WEATHER_URL = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=%s&format=json&num_of_days=3&key=95vtwa8kve578cfrjhv2mmnx";
	
	public static final int EVENT_MAXIMUM_DURATION = Util.GetHourOffset() * 2; //in ms
	public static final int EVENT_AUTODEFINITION_START_OFFSET = Util.GetHourOffset() * 2; //in ms 
	public static final int EVENT_AUTODEFINITION_END_DURATION = Util.GetMinuteOffset() * 30; //in ms 
	public static final EventSource EVENT_AUTODEFINITION_DEFAULT_SOURCE = new EventSource(SourceType.Music, "http://onair.krone.at/kronehit.mp3");
	
	public static final float WINDOW_MAX_BRIGHTNESS = 0.8f;
	public static final float WINDOW_MIN_BRIGHTNESS = 0f;
	
	public static final int FILE_CHOOSER_MUSIC_RESPONSE_CODE = 0;
	public static final int FILE_CHOOSER_IMAGE_RESPONSE_CODE = 1;
	
	public static final int SPEECH_RECOGNITION_START = 2;
	public static final int SPEECH_RECOGNITION_STOP = 3;
	public static final String SPEECH_RECOGNITION_BROADCAST = "SPEECH_RECOGNITION_BROADCAST";
	
	//All phrases must be defined in lower case
	public static final String SPEECH_RECOGNITION_ACTIVATION_PHRASE = "dude";
	public static final String SPEECH_RECOGNITION_COMMAND_ACTIVATED = "activated";
	public static final String SPEECH_RECOGNITION_COMMAND_DEACTIVATED = "deactivated";
	public static final String SPEECH_RECOGNITION_COMMAND_WEATHER = "weather";
	public static final String SPEECH_RECOGNITION_COMMAND_NEWS = "news";
}
