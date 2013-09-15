package no.radiomotor.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.googlecode.androidannotations.annotations.*;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.widget.Toast.LENGTH_LONG;
import static no.radiomotor.android.RadioService.*;
import static no.radiomotor.android.RadiomotorXmlParser.Item;

import java.io.File;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.main)
public class MyActivity extends Activity {

	@ViewById ListView newsFeedList;
	@ViewById TextView noNewsTextView;

	private final int PICTURE_REQUEST_CODE = 1;
	private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+File.separator + "radiomotor.jpg";

	private CacheHelper cacheHelper;
	private boolean podcastPlaying;

	MenuItem refresh;
	MenuItem podcastControl;

	@AfterViews
	void getRss() {
		podcastPlaying = false;
		cacheHelper = new CacheHelper(getApplicationContext());
		updateListview();
		downloadNewsfeed("http://www.radiomotor.no/feed/");

		LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_STARTED);
		intentFilter.addAction(ACTION_STOPPED);
		bManager.registerReceiver(broadcastReceiver, intentFilter);
	}

    @OptionsItem(R.id.action_picture)
    public void cameraSelected() {
        File file = new File(IMAGE_PATH);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

        this.startActivityForResult(cameraIntent, PICTURE_REQUEST_CODE);
    }

    @OptionsItem(R.id.action_play)
    public void radioPlayerSelected() {
		Intent i = new Intent(getApplicationContext(), RadioService.class);
		i.setAction(podcastPlaying ? ACTION_STOP : ACTION_PLAY);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		startService(i);
    }

	@OptionsItem(R.id.action_refresh)
	public void newsRefreshSelected() {
		refresh.setActionView(R.layout.actionbar_indeterminate_progress);
		downloadNewsfeed("http://www.radiomotor.no/feed/");
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		refresh = menu.findItem(R.id.action_refresh);
		podcastControl = menu.findItem(R.id.action_play);
		return true;
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(RadioService.ACTION_STARTED)) {
				podcastControl.setIcon(R.drawable.ic_action_av_stop);
				podcastPlaying = true;
			} else if (intent.getAction().equals(ACTION_STOPPED)) {
				podcastControl.setIcon(R.drawable.ic_action_av_play);
				podcastPlaying = false;
			} else if (intent.getAction().equals(ACTION_STOPPED_ERROR)) {
				Toast.makeText(getApplicationContext(), getString(R.string.playback_error), LENGTH_LONG).show();
				podcastControl.setIcon(R.drawable.ic_action_av_play);
				podcastPlaying = false;
			}
		}
	};

    @OnActivityResult(PICTURE_REQUEST_CODE)
    void onResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ImageFragment imageFragment = new ImageFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, imageFragment).commit();
            getFragmentManager().executePendingTransactions();

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(Uri.parse(IMAGE_PATH));
            imageView.invalidate();
        }
    }

	@Background
	void downloadNewsfeed(String urlString) {
		InputStream stream = null;
		RadiomotorXmlParser parser = new RadiomotorXmlParser();
		ArrayList<Item> entries;
		try {
			stream = downloadUrl(urlString);
			entries = parser.parse(stream);
			cacheHelper.writeNewsItems(entries);
		} catch (XmlPullParserException e) {
			errorMessage(R.string.parse_error);
		} catch (IOException e) {
			errorMessage(R.string.connection_error);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
		updateListview();
	}

	@UiThread
	void updateListview() {
		if (refresh != null) {
			refresh.setActionView(null);
		}
		ArrayList<Item> items = new ArrayList<Item>();
		try {
			items = cacheHelper.readNewsItems();
		} catch (IOException e) {
			Log.e("SBN", e.getLocalizedMessage(), e);
		}

		if (items.size() > 0) {
			noNewsTextView.setVisibility(View.GONE);
			newsFeedList.setAdapter(new NewsFeedAdapter(getApplicationContext(), R.layout.row_newsitem, items));
		} else {
			newsFeedList.setVisibility(View.GONE);
			noNewsTextView.setVisibility(View.VISIBLE);
		}
	}

	@UiThread
	void errorMessage(int resourceId) {
		Toast.makeText(getApplicationContext(), resourceId, LENGTH_LONG).show();
	}

	@ItemClick
	void newsFeedListItemClicked(Item item) {
		NewsItemActivity_.intent(getApplicationContext()).flags(FLAG_ACTIVITY_NEW_TASK).newsItem(item).start();
	}

	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
		conn.connect();
		return conn.getInputStream();
	}

}
