package com.menor.easyfacebookconnect.model;

import com.menor.easyfacebookconnect.EasyActionListener;

public class PageShareItem extends PageItem {

	private String title;
	private String caption;
	private String description;
	private String link;
	private String imageUrl;
	
	public PageShareItem(EasyActionListener listener, String pageUrl, String message, String title, String caption, String description, String link, String imageUrl) {
		super(listener, pageUrl, message);
		this.title = title;
		this.caption = caption;
		this.description = description;
		this.link = link;
		this.imageUrl = imageUrl;
	}

	public String getTitle() {
		return title;
	}

	public String getCaption() {
		return caption;
	}

	public String getDescription() {
		return description;
	}

	public String getLink() {
		return link;
	}

	public String getImageUrl() {
		return imageUrl;
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
