package com.menor.easyfacebookconnect.model;

import com.menor.easyfacebookconnect.EasyActionListener;

public class WallStatusItem extends Item {

	private String status;

	public WallStatusItem(EasyActionListener listener, String status) {
		super(listener);
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
	
	@Override
	public EasyActionListener getListener() {
		return super.getListener();
	}

}
