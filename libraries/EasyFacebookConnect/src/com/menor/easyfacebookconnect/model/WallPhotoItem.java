package com.menor.easyfacebookconnect.model;

import android.graphics.Bitmap;

import com.menor.easyfacebookconnect.EasyActionListener;

public class WallPhotoItem extends Item {

	private String title;
	private Bitmap photo;
	
	public WallPhotoItem(EasyActionListener listener, Bitmap photo) {
		this(listener, null, photo);
	}
	
	public WallPhotoItem(EasyActionListener listener, String title, Bitmap photo) {
		super(listener);
		this.title = title;
		this.photo = photo;
	}

	public String getTitle() {
		return title;
	}

	public Bitmap getPhoto() {
		return photo;
	}

	@Override
	public EasyActionListener getListener() {
		return super.getListener();
	}

}
