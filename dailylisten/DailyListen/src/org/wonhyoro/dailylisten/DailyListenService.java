package org.wonhyoro.dailylisten;

import java.io.IOException;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.os.PowerManager;

public class DailyListenService extends Service implements OnCompletionListener, OnPreparedListener, OnErrorListener {

	private static final String ACTION_PLAY = "org.wonhyoro.dailylisten.PLAY";
	private static final String ACTION_STOP = "org.wonhyoro.dailylisten.STOP";
	MediaPlayer player;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void initPlayer()
	{
		player = new MediaPlayer();
		player.setOnCompletionListener( this );
		player.setOnErrorListener( this );
		player.setWakeMode( getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK );
	}
	
	@Override
	public void onCreate() {
		initPlayer();
		super.onCreate();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if ( intent.getAction().equals( ACTION_PLAY ) ) {
			
			if ( player.isPlaying() )
			{
				System.out.println( "Already playing..." );
				return super.onStartCommand(intent, flags, startId);
			}
			
			String filename = intent.getStringExtra( "filename" );
			
			try {
				player.setDataSource( filename );
				player.setOnPreparedListener( this );
				player.prepareAsync();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			PendingIntent pi = PendingIntent.getActivity( getApplicationContext(), 0, 
					new Intent( getApplicationContext(), MainActivity.class ), 
					PendingIntent.FLAG_UPDATE_CURRENT );
			Notification notification = new Notification();
			notification.tickerText = "playing... " + filename;
			notification.icon = R.drawable.ic_launcher;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.setLatestEventInfo( getApplicationContext(), "DailyListen", 
					filename, pi );
			startForeground( 1, notification );
		}
		else if ( intent.getAction().equals( ACTION_STOP ) ) {
			System.out.println("In service: stop playing" );
			player.stop();
			player.reset();
			stopForeground( true );
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Intent testIntent = new Intent( "org.wonhyoro.dailylisten.play_fin" );
		sendBroadcast( testIntent );
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		player.start();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return false;
	}
}
