package com.kg6.schlafdoedel.event;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
	private final Event EVENT;
	private final Activity CONTEXT;
	private final FrameLayout CONTAINER;
	
	private MediaPlayer mediaPlayer;
	private ImageView imageView;
	private boolean enabled;	
	
	public EventExecutor(Event event, Activity context, FrameLayout container) {
		EVENT = event;
		CONTEXT = context;
		CONTAINER = container;
		
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
		return this.enabled;
	}
	
	public void run() {
		switch(EVENT.getType()) {
			case Image:
				showImage();
				break;
			case Music:
			case Radio:
				startPlayback();
				break;
		}
		
		while(this.enabled) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
		
		switch(EVENT.getType()) {
			case Image:
				hideImage();
				break;
			case Music:
			case Radio:
				stopPlayback();
				break;
		}
	}
	
	private void showImage() {
		final Bitmap bitmap = loadBitmap(EVENT.getSource());
		
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
	
	private void startPlayback() {
		final String url = EVENT.getSource().getUrl();
		
		try {
			this.mediaPlayer = new MediaPlayer();
			this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.mediaPlayer.setDataSource(url);
			this.mediaPlayer.prepare();
			this.mediaPlayer.start();
		} catch (Exception e) {
			Log.e("EventExecutor.java", String.format("Unable to play audio from URL %s", url), e);
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
