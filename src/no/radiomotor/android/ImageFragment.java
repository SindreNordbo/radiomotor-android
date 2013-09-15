package no.radiomotor.android;

import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

public class ImageFragment extends DialogFragment {
    private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+ File.separator + "radiomotor.jpg";
    ImageView imageView;

    public ImageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        getDialog().setTitle("Hva vil du gjøre med bildet?");
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(IMAGE_PATH));
        imageView.invalidate();

        return view;
    }
}
