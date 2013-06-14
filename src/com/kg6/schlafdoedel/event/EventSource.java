package com.kg6.schlafdoedel.event;

import java.util.HashMap;

public class EventSource {
	private final String URL;
	
	private HashMap<String, Object> attributes;
	
	public EventSource(String url) {
		URL = url;
		
		this.attributes = new HashMap<String, Object>();
	}

	public String getUrl() {
		return URL;
	}
	
	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}
}
