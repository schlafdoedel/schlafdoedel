package com.kg6.schlafdoedel.event;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class EventExecutor extends Thread {
	private final Activity CONTEXT;
	private final FrameLayout CONTAINER;
	private final EventScheduler EVENT_SCHEDULER;
	private final Event EVENT;
	
	private MediaPlayer mediaPlayer;
	private ImageView imageView;
	private boolean enabled;	
	
	public EventExecutor(Activity context, EventScheduler eventScheduler, Event event, FrameLayout container) {
		CONTEXT = context;
		CONTAINER = container;
		EVENT_SCHEDULER = eventScheduler;
		EVENT = event;
		
		this.mediaPlayer = null;
		this.imageView = null;
		
		this.enabled = true;
	}
	
	public Event getEvent() {
		return EVENT;
	}
	
	public void dismiss() {
		this.enabled = false;
	}
	
	public boolean isDismissed() {
		return !this.enabled;
	}
	
	public void run() {
		List<EventSource> eventSourceList = EVENT.getEventSourceList();
		
		for(EventSource source : eventSourceList) {
			switch(source.getSourceType()) {
				case Image:
					showImage(source);
					break;
				case Music:
					startPlayback(source);
					break;
			}
		}
		
		while(this.enabled) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
		
		for(EventSource source : eventSourceList) {
			switch(source.getSourceType()) {
				case Image:
					hideImage();
					break;
				case Music:
					stopPlayback();
					break;
			}
		}
	}
	
	private void showImage(EventSource source) {
		final Bitmap bitmap = loadBitmap(source);
		
		if(bitmap != null) {
			CONTEXT.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					for(int i = 0; i < CONTAINER.getChildCount(); i++) {
						CONTAINER.getChildAt(i).setVisibility(View.GONE);
					}
					
					if(imageView != null) {
						CONTAINER.removeView(imageView);
					}
					
					imageView = new ImageView(CONTEXT);
					imageView.setImageBitmap(bitmap);
					
					CONTAINER.addView(imageView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				}
			});
		} else {
			EVENT_SCHEDULER.raiseEventError(EVENT, "Unable to load the image");
		}
	}
	
	private void hideImage() {
		if(this.imageView != null) {
			CONTEXT.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					CONTAINER.removeView(imageView);
					
					for(int i = 0; i < CONTAINER.getChildCount(); i++) {
						CONTAINER.getChildAt(i).setVisibility(View.VISIBLE);
					}
				}
			});
		}
	}
	
	private void startPlayback(EventSource source) {
		final String url = source.getUrl();
		
		try {
			this.mediaPlayer = new MediaPlayer();
			this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.mediaPlayer.setDataSource(url);
			this.mediaPlayer.prepare();
			this.mediaPlayer.start();
		} catch (Exception e) {
			Log.e("EventExecutor.java", String.format("Unable to play audio from URL %s", url), e);
			
			EVENT_SCHEDULER.raiseEventError(EVENT, "Unable to play audio");
		}
	}
	
	private void stopPlayback() {
		if(this.mediaPlayer != null) {
			this.mediaPlayer.stop();
			this.mediaPlayer.release();
		}
	}
	
	public static Bitmap loadBitmap(EventSource source) {
        Bitmap bitmap = null;
        
        InputStream inputStream = null;
        
        try {
        	inputStream = new URL(source.getUrl()).openStream();
            
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if(inputStream != null) {
        		try {
					inputStream.close();
				} catch (IOException e) {
					
				}
        	}
        }
        
        return bitmap;
	}
}
