package com.newer.mediaplayer;

import java.io.IOException;

import org.apache.http.client.CircularRedirectException;

import android.R.anim;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Media;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener, OnSeekBarChangeListener  {

	private static final String TAG = "MainActivity";
	private static final int ID = 1;
	private ListView listView;
	private TextView textView_now;
	private TextView textView_all;
	private TextView textView_name;
	private SimpleCursorAdapter adapter;
	private Cursor cursor;
	private SeekBar bar;
	private int p;
	private MediaPlayer player;
	
	private Runnable action = new Runnable() {
		
		@Override
		public void run() {

			int currentTime = player.getCurrentPosition();	
				
			textView_now.setText(showProgress(currentTime));
			int max = player.getDuration();
			bar.setMax(max);
			bar.setProgress(currentTime);
			bar.postDelayed(action, 1000);

		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
		loadData();
		
		
		//创建MediaPlayer
		player = new MediaPlayer();
	}
	
	/*@Override
	protected void onResume() {
		super.onResume();
		
		//程序一启动就加载第一个
		if (cursor.moveToFirst()) {
			String path = cursor.getString(4);
			try {
				player.setDataSource(path);
				player.prepare();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}*/
	
	
	
	private void loadData() {

		ContentResolver resolver = getContentResolver();

		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = { Media._ID, Media.DISPLAY_NAME, Media.ARTIST,
				 Media.DURATION, Media.DATA };
		cursor = resolver.query(uri, projection, null, null, null);
		
		adapter = new SimpleCursorAdapter(this, R.layout.item, cursor, new String[]{Media.DISPLAY_NAME,Media.ARTIST,Media.DURATION,Media.DATA}, new int[]{R.id.textView1,R.id.textView2,R.id.textView3,R.id.textView4});
		listView.setAdapter(adapter);
		
		/*while (cursor.moveToNext()) {
			String name = cursor.getString(1);
			String artist = cursor.getString(2);
			String album = cursor.getString(3);
			int duration = cursor.getInt(4);
			String data = cursor.getString(5);
			
			String message = String.format("%s , %s , %s , %d , %s\n", name,artist,album,duration,data);
			
			Log.d(TAG, message);
			}	*/
			
		
		
	}

	private void initView() {

		listView = (ListView) findViewById(R.id.listView);
		bar = (SeekBar) findViewById(R.id.seekBar);
		textView_now = (TextView) findViewById(R.id.textView_now);
		textView_all = (TextView) findViewById(R.id.textView_all);
		textView_name = (TextView) findViewById(R.id.textView_name);
//		bar.setMax(max);
//		bar.setProgress(player.getCurrentPosition());
//		runOnUiThread(action);
		
		listView.setOnItemClickListener(this);
		bar.setOnSeekBarChangeListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		player.release();
	}
	
	public void doClick(View view){
		
		switch (view.getId()) {
		case R.id.button_on:
			//start()前一定先加载数据源setDataSource，然后prepare()
			player.start();
			runOnUiThread(action);
			int all = player.getDuration();
			textView_all.setText(showProgress(all));
			//Toast.makeText(this, String.valueOf(p), Toast.LENGTH_SHORT).show();
			sendNotification();
			break;

		case R.id.button_pause:
			if (player.isPlaying()) {
				player.pause();
			}
			
			bar.removeCallbacks(action);
			break;
		case R.id.button_next:
			p++;
			
			if (p < cursor.getCount()) {
				
				next();
			}else{
				p = 0;
				next();
			}
			sendNotification();				
			break;
		}
	}

	private void sendNotification() {

		NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = android.R.drawable.ic_media_play;
		CharSequence tickerText = cursor.getString(1);
		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
		
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, cursor.getString(1), cursor.getString(2), contentIntent);
	
		manager.notify(ID, notification);
	}

	private void next() {
		if(cursor.moveToPosition(p)){
			
			player.reset();
			
			try {
				player.setDataSource(cursor.getString(4));
				player.prepare();

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			textView_name.setText(cursor.getString(1));
			//textView_all.setText(showProgress(0));
			runOnUiThread(action);
			player.start();
			int all = player.getDuration();
			textView_all.setText(showProgress(all));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		p = position;
		player.reset();
		
		try {
			player.setDataSource(cursor.getString(4));
			player.prepare();
			
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		textView_name.setText(cursor.getString(1));
		textView_all.setText(showProgress(0));
		
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		if (fromUser) {
			player.seekTo(progress);
			textView_now.setText(showProgress(progress));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

		player.pause();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		player.start();
	}

	public String showProgress(int progress){
        progress = progress/1000;
        int minute=progress/60;
        int hour=minute/60;
        int second=progress%60;
        return String.format("%02d:%02d", minute, second);
    }
	
	
}
