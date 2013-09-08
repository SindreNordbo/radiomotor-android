package no.radiomotor.android;

import android.app.Activity;
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
import static no.radiomotor.android.RadiomotorXmlParser.Item;

import java.io.File;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.main)
public class MyActivity extends Activity {

	@ViewById ListView newsFeedList;

	private final int PICTURE_REQUEST_CODE = 1;
	private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+File.separator + "radiomotor.jpg";

	private CacheHelper cacheHelper;

	@AfterViews
	void getRss() {
		cacheHelper = new CacheHelper(getApplicationContext());
		updateListview();
		downloadNewsfeed("http://www.radiomotor.no/feed/");
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
    }

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
			e.printStackTrace(); // TODO
		} catch (IOException e) {
			e.printStackTrace(); // TODO
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
		ArrayList<Item> items = new ArrayList<Item>();
		try {
			items = cacheHelper.readNewsItems();
		} catch (IOException e) {
			e.printStackTrace(); // TODO
		}

		newsFeedList.setAdapter(new NewsFeedAdapter(getApplicationContext(), R.layout.row_newsitem, items));
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
