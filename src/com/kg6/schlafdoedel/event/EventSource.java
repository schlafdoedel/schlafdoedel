package com.kg6.schlafdoedel.event;

import java.util.HashMap;

import com.kg6.schlafdoedel.Configuration;

import android.util.Log;

public class EventSource {
	public enum SourceType {
		Music,
		Image,
		Weather,
	}
	
	private final String URL;
	private final SourceType TYPE;
	
	private HashMap<String, Object> attributes;
	
	public EventSource(SourceType type, String url) {
		URL = url;
		TYPE = type;
		
		this.attributes = new HashMap<String, Object>();
		
		initializeAttributes();
	}
	
	private void initializeAttributes() {
		if(TYPE == SourceType.Music) {
			setAttribute("volume", Configuration.EVENT_ALARM_VOLUME);
		}
	}

	public String getUrl() {
		return URL;
	}
	
	public SourceType getSourceType() {
		return TYPE;
	}
	
	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}
	
	public float getFloatAttribute(String key) {
		try {
			if(this.attributes.containsKey(key)) {
				return (Float) this.attributes.get(key);
			}
		} catch (Exception e) {
			Log.e("EventSource.java", "Unable to parse event source float attributes", e);
		}
		
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof EventSource)) {
			return false;
		}
		
		EventSource peer = (EventSource)o;
		
		return peer.TYPE == TYPE;
	}
}
