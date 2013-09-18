package com.menor.easyfacebookconnect.model;

import android.content.Context;
import com.facebook.model.GraphUser;
import com.menor.easyfacebookconnect.preference.UserPreference;

public class FacebookUser {

    UserPreference userPreference;

    public FacebookUser(Context context) {
        userPreference = new UserPreference(context);
    }

    public void storeUser(GraphUser user) {
    	String location = null;
    	if (user.getLocation() != null) {
    		location = (String) user.getLocation().getProperty("name");
    	}
        userPreference.storeUser(user.getFirstName(), user.getLastName(), user.getBirthday(), user.getId(), user.getUsername(), location);
    }

    public String getFirstName() {
        return userPreference.getFirstName();
    }

    public String getLastName() {
        return userPreference.getLastName();
    }

    public String getBirthday() {
        return userPreference.getBirthday();
    }

    public String getId() {
        return userPreference.getId();
    }

    public String getUserName() {
        return userPreference.getUserName();
    }

    public String getCity() {
        return userPreference.getCity();
    }


}
