package com.menor.easyfacebookconnect;

import com.facebook.FacebookRequestError;
import com.facebook.Response;

public class EasyActionListener implements EasyActionFacebookListener {

	@Override
	public void onStart() { }

	@Override
	public void onSuccess(Response response) { }

	@Override
	public void onFinish() { }

	@Override
	public void onError(FacebookRequestError error, Exception exception) { }

}
