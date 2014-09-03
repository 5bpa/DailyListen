package org.wonhyoro.dailylisten;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener {
	Button playButton;
	Button stopButton;
	Button nextButton;
	Button prevButton;
	Button downloadButton;
	TextView audioTitle;
	
	Intent playServiceIntent;
	
	ArrayList<String> list;
	
	final static String URLBASE = "http://wonhyoro.iptime.org/~jaeswith/ebs/";
	final static int EXPIRE_DAYS = 10;
	
   private static final int MSG_PLAY_FIN = 0;
	private static final String ACTION_PLAY = "org.wonhyoro.dailylisten.PLAY";
	private static final String ACTION_STOP = "org.wonhyoro.dailylisten.STOP";
	
	int curTrack;
	
	private void readAudioFileList()
	{
		list = new ArrayList<String>();
		
		String filename;
		
		File[] fileList = getFilesDir().listFiles();
		
		if ( fileList == null )
			return;
		
		for ( File f : fileList )
		{
			filename = f.getName();
			if ( filename.endsWith( ".mp3" ) )
			{
				System.out.println( "Filename: " + f.getPath() );
				list.add( f.getPath() );
			}
		}
		
		Collections.sort( list, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return rhs.compareTo( lhs );
			}
		} );
		
		for ( String s : list )
		{
			System.out.println( s );
		}
		
		curTrack = 0;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        playButton = (Button)findViewById( R.id.button1 );
        playButton.setOnClickListener( this );
        
        stopButton = (Button)findViewById( R.id.button2 );
        stopButton.setOnClickListener( this );
        
        nextButton = (Button)findViewById( R.id.button3 );
        nextButton.setOnClickListener( this );
        
        prevButton = (Button)findViewById( R.id.button4 );
        prevButton.setOnClickListener( this );
        
        downloadButton = (Button)findViewById( R.id.button5 );
        downloadButton.setOnClickListener( this );
        
        audioTitle = (TextView) findViewById( R.id.textView1 );

        readAudioFileList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    class AudioDownloader extends AsyncTask<String, String, String> {

    	private Context mContext;
    	    	
    	public AudioDownloader( Context c ) {
    		mContext = c;
		}
    	
    	private void removeOldFiles()
    	{
    		for ( int days = EXPIRE_DAYS; days < EXPIRE_DAYS+3; days++ )
    		{
	    		long epoch = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000; 
	    		String date = new java.text.SimpleDateFormat("yyMMdd_EEE", Locale.US ).format(new java.util.Date (epoch));
	    		
	    		String filename = date + "_easy_power.mp3";
				
				File file = new File( mContext.getFilesDir(), filename );
				if ( file.exists() )
					file.delete();			
    		}
    		
    	}
    	
		@Override
		protected String doInBackground(String... params) {
	    	 int count;
	    	 for ( String filename : params )
	    	 {
	    		 File file = new File( mContext.getFilesDir(), filename );
	    		 if ( file.exists() )
	    		 {
	    			 System.out.println( file.getPath() + " exists" );
	    			 continue;
	    		 }
	    		 
	    		 removeOldFiles();
	    		 
	    		 try {
	    			 URL url = new URL( URLBASE + filename );
	    			 URLConnection conection = url.openConnection();
	    			 conection.connect();

	    			 InputStream input = new BufferedInputStream(url.openStream(), 8192);
	    			 OutputStream output = new FileOutputStream( file );

	    			 byte data[] = new byte[1024];

	    			 while ((count = input.read(data)) != -1) {
	    				 output.write(data, 0, count);
	    			 }

	    			 output.flush();

	    			 output.close();
	    			 input.close();

	    		 } catch (Exception e) {
	    			 Log.e("Error: ", e.getMessage());
	    		 }
	    	 }

	    	 return null;
		}

		@Override
		protected void onPostExecute(String result) {
			audioTitle.setText( "Download done." );
			readAudioFileList();
			super.onPostExecute(result);
		}
    }

    private void playAudio()
    {
    	String path = list.get( curTrack );
    	audioTitle.setText( path );
		playServiceIntent = new Intent( this, DailyListenService.class );
		playServiceIntent.setAction( ACTION_PLAY );
		System.out.println( "playAudio: " + path );
		playServiceIntent.putExtra( "filename", path );
		startService( playServiceIntent );
    }
    
    private void goNext()
    {
		curTrack++;
		if ( curTrack == list.size() )
		{
			curTrack = 0;
		}
    }
    
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if ( v == playButton ) {
			System.out.println( list.get( curTrack ) );
			playAudio();
		}
		else if ( v == stopButton) {
			System.out.println( "Stop pressed" );
			playServiceIntent = new Intent( this, DailyListenService.class );
			playServiceIntent.setAction( ACTION_STOP );
			startService( playServiceIntent );
		}
		else if ( v == nextButton ) 
		{
			playServiceIntent = new Intent( this, DailyListenService.class );
			playServiceIntent.setAction( ACTION_STOP );
			startService( playServiceIntent );
			
			curTrack++;
			if ( curTrack == list.size() )
			{
				curTrack = 0;
			}
			playAudio();
		}
		else if ( v == prevButton ) 
		{
			playServiceIntent = new Intent( this, DailyListenService.class );
			playServiceIntent.setAction( ACTION_STOP );
			startService( playServiceIntent );
			curTrack--;
			if ( curTrack == -1 )
			{
				curTrack = 0;
			}
			
			playAudio();
		}
		else if ( v == downloadButton )
		{
			String date = new java.text.SimpleDateFormat("yyMMdd_EEE", Locale.US ).format(new java.util.Date (System.currentTimeMillis()));
			String filename = date + "_easy_power.mp3";
			audioTitle.setText( "Download " + date + " ..." );
			new AudioDownloader( this ).execute( filename );
		}
	}
	
	public static class MessageReceiver extends BroadcastReceiver
	{
		Handler handler;

		public MessageReceiver( Handler h ) {
			handler = h;
		}

		@Override
		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();

			if ( action.equals( "org.wonhyoro.dailylisten.play_fin" ) )
			{
				Message msg = handler.obtainMessage( MSG_PLAY_FIN );
				handler.sendMessage( msg );
			}
		}
	}

	static class MessageHandler extends Handler 
	{
		WeakReference<MainActivity> main;

		public MessageHandler( MainActivity main ) {
			this.main = new WeakReference<MainActivity>( main );
		}

		@Override
		public void handleMessage( Message msg )
		{
			MainActivity main = this.main.get();
			switch( msg.what )
			{
			case MSG_PLAY_FIN:
				main.goNext();
				main.playAudio();
				break;
			}
		};
	}
}
