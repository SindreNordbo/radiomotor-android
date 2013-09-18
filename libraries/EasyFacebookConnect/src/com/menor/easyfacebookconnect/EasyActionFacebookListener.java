package com.menor.easyfacebookconnect;

import com.facebook.FacebookRequestError;
import com.facebook.Response;

public interface EasyActionFacebookListener {

	/**
     * Callbacks
     */
    public void onStart();

    public void onSuccess(Response response);

    public void onError(FacebookRequestError error, Exception exception);
    
    public void onFinish();
	
}
