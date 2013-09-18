package com.menor.easyfacebookconnect.model;

import com.menor.easyfacebookconnect.EasyActionListener;

import android.graphics.Bitmap;

public class PageImageItem extends PageItem {

	private Bitmap image;

	public PageImageItem(EasyActionListener listener, String pageUrl, String message, Bitmap image) {
		super(listener, pageUrl, message);
		this.image = image;
	}

	public Bitmap getImage() {
		return image;
	}

	@Override
	public EasyActionListener getListener() {
		return super.getListener();
	}

	@Override
	public String getMessage() {
		return super.getMessage();
	}

	@Override
	public String getPageUrl() {
		return super.getPageUrl();
	}

}
