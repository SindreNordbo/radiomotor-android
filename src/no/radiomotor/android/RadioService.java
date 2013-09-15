package no.radiomotor.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;

import static android.media.AudioManager.*;
import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

public class RadioService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

	public static final String ACTION_PLAY = "no.radiomotor.android.play";
	public static final String ACTION_STARTED = "no.radiomotor.android.started";
	public static final String ACTION_STOP = "no.radiomotor.android.stop";
	public static final String ACTION_STOPPED = "no.radiomotor.android.stopped";
	public static final String ACTION_STOPPED_ERROR = "no.radiomotor.android.stopped_error";
	public static final String WIFI_LOCK = "radiomotor.wifilock";

	private MediaPlayer mediaPlayer;
	private WifiLock wifiLock;
	private AudioManager am;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.getAction().equals(ACTION_PLAY)) {

				am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				int result = am.requestAudioFocus(this, STREAM_MUSIC, AUDIOFOCUS_GAIN);

				if (result == AUDIOFOCUS_REQUEST_GRANTED) {
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setWakeMode(getApplicationContext(), PARTIAL_WAKE_LOCK);
					mediaPlayer.setAudioStreamType(STREAM_MUSIC);
					String url = "http://servicepark.serverroom.us:6476";
					try {
						mediaPlayer.setDataSource(url);
					} catch (IOException e) {
						e.printStackTrace(); //TODO
					}
					mediaPlayer.setOnErrorListener(this);
					mediaPlayer.setOnPreparedListener(this);
					mediaPlayer.prepareAsync();
					wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK);
					wifiLock.acquire();
				} else {
					// TODO
				}
			}
			else if (intent.getAction().equals(ACTION_STOP)) {
				am.abandonAudioFocus(this);
				wifiLock.release();
				mediaPlayer.reset();
				LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STOPPED));
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaPlayer.start();
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STARTED));
	}

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) mediaPlayer.release();
		wifiLock.release();
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STOPPED));
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		wifiLock.release();
		mp.reset();
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STOPPED_ERROR));
		return true;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		if (focusChange == AUDIOFOCUS_LOSS) {
			wifiLock.release();
			mediaPlayer.reset();
		} else if (focusChange == AUDIOFOCUS_GAIN) {
			mediaPlayer.prepareAsync();
		}
	}
}
