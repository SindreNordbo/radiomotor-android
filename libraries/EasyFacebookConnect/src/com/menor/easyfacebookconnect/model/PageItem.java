package com.menor.easyfacebookconnect.model;

import com.menor.easyfacebookconnect.EasyActionListener;

public class PageItem extends Item {

	private String pageUrl;
	private String message;

	public PageItem(EasyActionListener listener, String pageUrl, String message) {
		super(listener);
		this.pageUrl = pageUrl;
		this.message = message;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public EasyActionListener getListener() {
		return super.getListener();
	}

}
