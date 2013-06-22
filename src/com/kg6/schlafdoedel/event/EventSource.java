package com.kg6.schlafdoedel.event;

import java.util.HashMap;

public class EventSource {
	public enum SourceType {
		Music,
		Image,
	}
	
	private final String URL;
	private final SourceType TYPE;
	
	private HashMap<String, Object> attributes;
	
	public EventSource(SourceType type, String url) {
		URL = url;
		TYPE = type;
		
		this.attributes = new HashMap<String, Object>();
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

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof EventSource)) {
			return false;
		}
		
		EventSource peer = (EventSource)o;
		
		return peer.TYPE == TYPE;
	}
}
