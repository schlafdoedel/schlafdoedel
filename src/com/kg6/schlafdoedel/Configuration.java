package com.kg6.schlafdoedel;

public class Configuration {
	public static final String COMMAND_SLEEPING_PHASE_AWAKE = "COMMAND_SLEEPING_PHASE_AWAKE";
	public static final String COMMAND_SLEEPING_PHASE_DEEP = "COMMAND_SLEEPING_PHASE_DEEP";
	public static final String COMMAND_SLEEPING_PHASE_SHALLOW = "COMMAND_SLEEPING_PHASE_SHALLOW";
	
	public static final int EVENT_MAXIMUM_DURATION = 1000 * 60 * 60 * 2; //in ms
	
	public static final float WINDOW_MAX_BRIGHTNESS = 0.8f;
	public static final float WINDOW_MIN_BRIGHTNESS = 0f;
	
	public static final int FILE_CHOOSER_MUSIC_RESPONSE_CODE = 0;
	public static final int FILE_CHOOSER_IMAGE_RESPONSE_CODE = 1;
}
