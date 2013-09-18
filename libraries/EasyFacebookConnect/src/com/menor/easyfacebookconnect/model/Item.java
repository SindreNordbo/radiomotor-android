package com.menor.easyfacebookconnect.model;

import com.menor.easyfacebookconnect.EasyActionListener;

public class Item {

	private EasyActionListener listener;

	public Item(EasyActionListener listener) {
		this.listener = listener;
	}

	public EasyActionListener getListener() {
		return listener;
	}

}
