package org.wonhyoro.dailylisten;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener, OnCompletionListener {
	Button playButton;
	Button stopButton;
	Button nextButton;
	Button prevButton;
	TextView audioTitle;
	MediaPlayer mediaPlayer;
	ArrayList<String> list;
	
	final static String BASE = "/storage/external_SD/MUSIC/ebs";
	
	int curTrack;
	
	private void readAudioFileList()
	{
		list = new ArrayList<String>();
		
		String filename;
		
		File dir = new File( BASE );
		if ( dir.isDirectory() == false )
		{
			return;
		}
			
		File[] fileList = dir.listFiles();
		
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
        
        audioTitle = (TextView) findViewById( R.id.textView1 );
        
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener( this );
        
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

    private void playAudio()
    {
    	String path = list.get( curTrack );
    	audioTitle.setText( path );
		try {
			mediaPlayer.setDataSource( path );
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if ( v == playButton ) {
			System.out.println( list.get( curTrack ) );
			playAudio();
		}
		else if ( v == stopButton) {
			if ( mediaPlayer.isPlaying() )
			{
				mediaPlayer.stop();
				mediaPlayer.reset();
			}
		}
		else if ( v == nextButton ) 
		{
			if ( mediaPlayer.isPlaying() )
			{
				mediaPlayer.stop();
				mediaPlayer.reset();
				curTrack++;
				if ( curTrack == list.size() )
				{
					curTrack = 0;
				}
				playAudio();
			}
		}
		else if ( v == prevButton ) 
		{
			if ( mediaPlayer.isPlaying() )
			{
				mediaPlayer.stop();
				mediaPlayer.reset();
				curTrack--;
				if ( curTrack < 0 )
				{
					curTrack = list.size() - 1;
				}
				playAudio();
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if ( curTrack == list.size() )
		{
			mp.release();
		}
		else
		{
			mediaPlayer.stop();
			mediaPlayer.reset();
			curTrack++;
			if ( curTrack == list.size() )
			{
				curTrack = 0;
			}
			playAudio();

		}
	}
}
