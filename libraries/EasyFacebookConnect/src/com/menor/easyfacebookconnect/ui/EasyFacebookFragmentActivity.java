package com.menor.easyfacebookconnect.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.facebook.*;
import com.facebook.Session.StatusCallback;
import com.facebook.model.GraphUser;
import com.menor.easyfacebookconnect.EasyActionListener;
import com.menor.easyfacebookconnect.EasyLoginListener;
import com.menor.easyfacebookconnect.PendingAction;
import com.menor.easyfacebookconnect.model.*;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class EasyFacebookFragmentActivity extends FragmentActivity implements StatusCallback {

	/**
     * **********************************
     * *********** Constants ************
     * **********************************
     */
	private static final String PERMISSION_PUBLISH = "publish_actions";
	private static final String PERMISSION_LOCATION = "user_location";
	private static final String PERMISSION_BIRTHDAY = "user_birthday";
	private static final String PERMISSION_EMAIL = "email";
	protected static List<String> WRITE_PERMISSIONS = Arrays.asList(PERMISSION_PUBLISH);
	protected static List<String> READ_PERMISSIONS = Arrays.asList(PERMISSION_BIRTHDAY, PERMISSION_EMAIL, PERMISSION_LOCATION);

    private static final String PENDING_ACTION_BUNDLE_KEY = "com.menor.easyfacebookconnect:PendingAction";
	
	
	/**
     * **********************************
     * *********** Variables ************
     * **********************************
     */
	private PendingAction pendingAction = PendingAction.NONE;
	private Item requestInfo;
	
	private UiLifecycleHelper uiHelper;
	
	private FacebookUser user;
    private FacebookResponse response;
    
	private EasyLoginListener loginListener;

	
    /**
     * **********************************
     * ********* Class Methods **********
     * **********************************
     */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setData(savedInstanceState);
    }
	
    @Override
    public void call(Session session, SessionState state, Exception exception) {
    	onSessionStateChange(session, state, exception);
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    
    /**
     * **********************************
     * ******** Private Methods *********
     * **********************************
     */
    private void setData(Bundle savedInstanceState) {
    	uiHelper = new UiLifecycleHelper(this, this);
        /**
         *  TODO added this to receive user information. We should give option to
         *  change this
         */
//        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        uiHelper.onCreate(savedInstanceState);

        /**
         *  TODO added this to receive user information. We should give option to
         *  change this
         */
//        Session session = Session.getActiveSession();
//        if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
//            session.openForRead(new Session.OpenRequest(this).setCallback(this).setPermissions(READ_PERMISSIONS));
//        }

    	user = new FacebookUser(this);
        response = new FacebookResponse(this);
        
        if (savedInstanceState != null) {
        	String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }    
    }
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	if (pendingAction == PendingAction.NONE) {
    		if (state.equals(SessionState.CLOSED) && loginListener != null) {
    			loginListener.onClosed(session, state, exception);
    		} else if (state.equals(SessionState.CLOSED_LOGIN_FAILED) && loginListener != null) {
    			loginListener.onClosedLoginFailed(session, state, exception);
    		} else if (state.equals(SessionState.OPENED) && loginListener != null) {
    			loginListener.onOpened(session, state, exception);
    			makeMeRequest(session);
    		} else if (state.equals(SessionState.CREATED) && loginListener != null) {
    			loginListener.onCreated(session, state, exception);
    		}
    	} else if (pendingAction != PendingAction.NONE && (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
    		requestInfo.getListener().onError(null, exception);
    		pendingAction = PendingAction.NONE;
    	} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
    		handlePendingAction();
    	}
    }
    
    private void handlePendingAction() {
    	/*
    	 * These actions may re-set pendingAction if they are still pending, but we assume they will succeed.
    	 */
    	PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;
        
    	if (previouslyPendingAction == PendingAction.POST_PHOTO_PAGE) {
    		postPhotoToPage();
    	} else if (previouslyPendingAction == PendingAction.POST_PHOTO_WALL) {
    		postPhotoToWall();
    	} else if (previouslyPendingAction == PendingAction.POST_STATUS_PAGE) {
    		postStatusToPage();
    	} else if (previouslyPendingAction == PendingAction.POST_STATUS_WALL) {
    		postStatusToWall();
    	}
    }
    
    private void makeMeRequest(final Session session) {
        loginListener.onStart();
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(GraphUser facebookUser, Response facebookResponse) {
                if (facebookUser != null) {
                    user.storeUser(facebookUser);
                }
                if (facebookResponse != null) {
                    response.storeResponse(facebookResponse);
                }
                if (session == Session.getActiveSession()) {
                    loginListener.onSuccess(facebookResponse);
                }
                if (facebookResponse.getError() != null) {
                	loginListener.onError(facebookResponse.getError());
                }
                loginListener.onFinish();
            }

        });
        request.executeAsync();
    }
    
    private void postPhotoToWall() {
    	WallPhotoItem item = (WallPhotoItem) requestInfo;
    	postPhotoToWall(item.getPhoto(), item.getTitle(), item.getListener());
    }
    
    private void postStatusToWall() {
    	WallStatusItem item = (WallStatusItem) requestInfo;
    	postStatusToWall(item.getStatus(), item.getListener());
    }
    
    private void postPhotoToPage() {
    	PageImageItem item = (PageImageItem) requestInfo;
    	shareImageToPage(item.getPageUrl(), item.getImage(), item.getMessage(), item.getListener());
    }
 
    private void postStatusToPage() {
    	PageShareItem item = (PageShareItem) requestInfo;
    	shareSomethingToPage(item.getListener(), item.getPageUrl(), item.getMessage(), item.getTitle(), item.getCaption(), item.getDescription(), item.getLink(), item.getImageUrl());
    }
    
    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains(PERMISSION_PUBLISH);
    }
    
    private void makeWallImageRequest(Bitmap image, String photoDescription, Session session, final EasyActionListener listener) {
        listener.onStart();
        Request request = Request.newUploadPhotoRequest(session, image, new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                if (response.getError() != null) {
                    listener.onError(response.getError(), null);
                } else {
                    listener.onSuccess(response);
                }
                listener.onFinish();
            }

        });
        if (photoDescription != null) {
            Bundle params = request.getParameters();
            params.putString("message", photoDescription);
        }
        request.executeAsync();
    }
    
    private void makeWallStatusRequest(String status, Session session, final EasyActionListener listener) {
    	listener.onStart();        
        Request request = Request.newStatusUpdateRequest(session, status, new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                if (response.getError() != null) {
                    listener.onError(response.getError(), null);
                } else {
                    listener.onSuccess(response);
                }
                listener.onFinish();
            }

        });
        request.executeAsync();
    }
    
    private void makePageImageRequest(final EasyActionListener listener, String pageUrl, String message, Bitmap image, Session session) {
        listener.onStart();
        Bundle postParams = new Bundle();
    	if (message != null) {
    		postParams.putString("message", message);
    	}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();

    	postParams.putByteArray("picture", byteArray);

    	Request.Callback callback = new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				if (response.getError() != null) {
                    listener.onError(response.getError(), null);
                } else {
                    listener.onSuccess(response);
                }
                listener.onFinish();
			}

		};

    	Request request = new Request(session, pageUrl, postParams, HttpMethod.POST, callback);
    	request.executeAsync();
    }
    
    private void makePageStatusRequest(final EasyActionListener listener, String pageUrl, String message, String title, String caption, String description, String link, String imageUrl, Session session) {
        listener.onStart();
        Bundle postParams = new Bundle();
    	if (title != null) {
    		postParams.putString("name", title);
    	}
    	if (caption != null) {
    		postParams.putString("caption", caption);
    	}
    	if (message != null) {
    		postParams.putString("message", message);
    	}
    	if (description != null) {
    		postParams.putString("description", description);
    	}
    	if (link != null) {
    		postParams.putString("link", link);
    	}
    	if (imageUrl != null) {
    		postParams.putString("picture", imageUrl);
    	}
    	Request.Callback callback = new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				if (response.getError() != null) {
                    listener.onError(response.getError(), null);
                } else {
                    listener.onSuccess(response);
                }
                listener.onFinish();
			}

		};
    	
    	Request request = new Request(session, pageUrl, postParams, HttpMethod.POST, callback);
    	request.executeAsync();
    }
    
    
    /**
     * **********************************
     * ******** Public Methods **********
     * **********************************
     */
    public boolean isConnected()  {
        Session session = Session.getActiveSession();
        return session.isOpened();
    }
    
    public void disconnect() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}

    public String getUserFirstName() {
        return (isConnected()) ? user.getFirstName() : null;
    }

    public String getUserLastName() {
        return (isConnected()) ? user.getLastName() : null;
    }

    public String getUserBirth() {
        return (isConnected()) ? user.getBirthday() : null;
    }

    public String getUserId() {
        return (isConnected()) ? user.getId() : null;
    }

    public String getUserName() {
        return (isConnected()) ? user.getUserName() : null;
    }

    public String getUserCity() {
        return (isConnected()) ? user.getCity() : null;
    }

    public String getUserEmail() {
        return (isConnected()) ? response.getEmail() : null;
    }

    public String getUserGender() {
        return (isConnected()) ? response.getGender() : null;
    }

    public String getAccessToken() {
        return (isConnected()) ?response.getAccessToken() : null;
    }

    public String getApplicationId() {
        return (isConnected()) ? response.getAppId() : null;
    }

    public void connect(EasyLoginListener listener)  {
        loginListener = listener;
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(this).setPermissions(READ_PERMISSIONS));
        } else {
            Session.openActiveSession(this, true, this);
        }
        session = Session.getActiveSession();
        if (session.isOpened()) {
            makeMeRequest(session);
        }
    }
    
    public void postPhotoToWall(Bitmap image, EasyActionListener listener) {
    	postPhotoToWall(image, null, listener);
    }
    
    public void postPhotoToWall(Bitmap image, String description, EasyActionListener listener) {
    	Session session = Session.getActiveSession();
        if (session != null) {
            if (hasPublishPermission()) {
            	makeWallImageRequest(image, description, session, listener);
            } else {
            	requestInfo = new WallPhotoItem(listener, description, image);
            	pendingAction = PendingAction.POST_PHOTO_WALL;
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, WRITE_PERMISSIONS).setCallback(this));
            }
        }
    }
	
	public void postStatusToWall(String status, EasyActionListener listener) {
		Session session = Session.getActiveSession();
        if (session != null) {
            if (hasPublishPermission()) {
                makeWallStatusRequest(status, session, listener);
            } else {
            	requestInfo = new WallStatusItem(listener, status);
            	pendingAction = PendingAction.POST_STATUS_WALL;
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, WRITE_PERMISSIONS).setCallback(this));
            }
        }
	}
	
	public void shareImageToPage(String pageUrl, Bitmap image, EasyActionListener listener) {
		shareImageToPage(pageUrl, image, null, listener);
	}
	
	public void shareImageToPage(String pageUrl, Bitmap image, String message, EasyActionListener listener) {	
    	Session session = Session.getActiveSession();
        if (session != null) {
            if (hasPublishPermission()) {
            	makePageImageRequest(listener, pageUrl, message, image, session);
            } else {
            	requestInfo = new PageImageItem(listener, pageUrl, message, image);
            	pendingAction = PendingAction.POST_PHOTO_PAGE;
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, WRITE_PERMISSIONS).setCallback(this));
            }
        }
	}
	
	public void shareLinkToPage(String pageUrl, String link, EasyActionListener listener) {
		shareLinkToPage(pageUrl, null, link, listener);
	}
	
	public void shareLinkToPage(String pageUrl, String title, String link, EasyActionListener listener) {
		shareLinkToPage(pageUrl, title, null, link, listener);
	}
	
	public void shareLinkToPage(String pageUrl, String title, String caption, String link, EasyActionListener listener) {
		shareLinkToPage(pageUrl, title, caption, null, link, listener);
	}
	
	public void shareLinkToPage(String pageUrl, String title, String caption, String description, String link, EasyActionListener listener) {
		shareLinkToPage(pageUrl, title, caption, description, null, link, listener);
	}
	
	public void shareLinkToPage(String pageUrl, String title, String caption, String description, String link, String message, EasyActionListener listener) {
		shareLinkToPage(pageUrl, title, caption, description, link, message, null, listener);
	}
	
	public void shareLinkToPage(String pageUrl, String title, String caption, String description, String link, String message, String imageUrl, EasyActionListener listener) {
		shareSomethingToPage(listener, pageUrl, message, title, caption, description, link, imageUrl);
	}

	public void shareImageUrlToPage(String pageUrl, String imageUrl, EasyActionListener listener) {
		shareImageUrlToPage(pageUrl, imageUrl, null, listener);
	}
	
	public void shareImageUrlToPage(String pageUrl, String imageUrl, String link, EasyActionListener listener) {
		shareImageUrlToPage(pageUrl, imageUrl, link, null, listener);
	}
	
	public void shareImageUrlToPage(String pageUrl, String imageUrl, String link, String title, EasyActionListener listener) {
		shareImageUrlToPage(pageUrl, imageUrl, link, title, null, listener);
	}
	
	public void shareImageUrlToPage(String pageUrl, String imageUrl, String link, String title, String caption, EasyActionListener listener) {
		shareImageUrlToPage(pageUrl, imageUrl, link, title, caption, null, listener);
	}
	
	public void shareImageUrlToPage(String pageUrl, String imageUrl, String link, String title, String caption, String description, EasyActionListener listener) {
		shareImageUrlToPage(pageUrl, imageUrl, link, title, caption, description, null, listener);
	}
	
	public void shareImageUrlToPage(String pageUrl, String imageUrl, String link, String title, String caption, String description, String message, EasyActionListener listener) {
		shareSomethingToPage(listener, pageUrl, message, title, caption, description, link, imageUrl);
	}

	public void shareStatusToPage(String pageUrl, String message, EasyActionListener listener) {
		shareSomethingToPage(listener, pageUrl, message, null, null, null, null, null);
	}

	private void shareSomethingToPage(EasyActionListener listener, String pageUrl, String message, String title, String caption, String description, String link, String imageUrl) {
		Session session = Session.getActiveSession();
        if (session != null) {
            if (hasPublishPermission()) {
                makePageStatusRequest(listener, pageUrl, message, title, caption, description, link, imageUrl, session);
            } else {
            	requestInfo = new PageShareItem(listener, pageUrl, message, title, caption, description, link, imageUrl);            	
            	pendingAction = PendingAction.POST_STATUS_PAGE;
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, WRITE_PERMISSIONS).setCallback(this));
            }
        }
	}
	
}
