package no.radiomotor.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OnActivityResult;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;

import java.io.File;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.main)
public class MyActivity extends Activity {
    private final int PICTURE_REQUEST_CODE = 1;
    private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+File.separator + "radiomotor.jpg";

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

}
