package no.radiomotor.android;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.OnActivityResult;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.widget.Toast.LENGTH_LONG;
import static no.radiomotor.android.RadioService.ACTION_PLAY;
import static no.radiomotor.android.RadioService.ACTION_STARTED;
import static no.radiomotor.android.RadioService.ACTION_STOP;
import static no.radiomotor.android.RadioService.ACTION_STOPPED;
import static no.radiomotor.android.RadioService.ACTION_STOPPED_ERROR;
import static no.radiomotor.android.RadiomotorXmlParser.Item;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.main)
public class MyActivity extends FragmentActivity {
    public static final String IS_RADIO_PLAYING_KEY = "isRadioPlaying";

    @ViewById ListView newsFeedList;
    @ViewById TextView noNewsTextView;

    private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+File.separator + "radiomotor.jpg";
    private final int PICTURE_REQUEST_CODE = 1;
    private CacheHelper cacheHelper;
    boolean isRadioPlaying;
    MenuItem refresh;
    MenuItem radioControl;
    private boolean imageReadyForDisplay = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRadioPlaying = SharedPreferencesHelper.get(this).getBoolean(IS_RADIO_PLAYING_KEY, false);
    }

    @AfterViews
    void getRss() {
        cacheHelper = new CacheHelper(getApplicationContext());
        downloadNewsfeed("http://www.radiomotor.no/feed/");

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STARTED);
        intentFilter.addAction(ACTION_STOPPED);
        intentFilter.addAction(ACTION_STOPPED_ERROR);
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
        i.setAction(isRadioPlaying ? ACTION_STOP : ACTION_PLAY);
        if (!isRadioPlaying) {
            radioControl.setActionView(R.layout.actionbar_indeterminate_progress);
        }
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
        radioControl = menu.findItem(R.id.action_play);
        changeRadioStatus(isRadioPlaying);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (imageReadyForDisplay) {
            imageReadyForDisplay = false;
            ImageFragment imageFragment = new ImageFragment();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("image");
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (prev != null) {
                ft.remove(prev);
            }
            imageFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            imageFragment.show(ft, "image");
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RadioService.ACTION_STARTED)) {
                changeRadioStatus(true);
            } else if (intent.getAction().equals(ACTION_STOPPED)) {
                changeRadioStatus(false);
            } else if (intent.getAction().equals(ACTION_STOPPED_ERROR)) {
                Toast.makeText(getApplicationContext(), getString(R.string.playback_error), LENGTH_LONG).show();
                changeRadioStatus(false);
            }
        }
    };

    private void changeRadioStatus(boolean playing) {
        radioControl.setActionView(null);
        radioControl.setIcon(playing ? R.drawable.ic_action_av_stop : R.drawable.ic_action_av_play);
        radioControl.setTitle(playing ? R.string.action_stop : R.string.action_play);
        SharedPreferencesHelper.get(this).putBoolean(IS_RADIO_PLAYING_KEY, playing);
        isRadioPlaying = playing;
    }

    @OnActivityResult(PICTURE_REQUEST_CODE)
    void onResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            imageReadyForDisplay = true;
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
