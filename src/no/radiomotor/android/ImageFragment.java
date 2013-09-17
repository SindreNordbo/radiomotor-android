package no.radiomotor.android;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.widget.LoginButton;

import java.io.File;

public class ImageFragment extends DialogFragment {
    private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+ File.separator + "radiomotor.jpg";
    ImageView imageView;
    LoginButton loginButton;

    public ImageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setFragment(this);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(IMAGE_PATH));
        getDialog().setTitle(R.string.upload);
        imageView.invalidate();

        return view;
    }
}
