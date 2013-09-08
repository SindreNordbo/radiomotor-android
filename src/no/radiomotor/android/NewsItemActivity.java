package no.radiomotor.android;

import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.googlecode.androidannotations.annotations.*;

import static no.radiomotor.android.RadiomotorXmlParser.Item;

@EActivity(R.layout.news_item_activity)
public class NewsItemActivity extends Activity {

	@ViewById
	WebView newsItemContent;

	@Extra("newsItem") Item newsItem;

	@AfterViews
	void afterViews() {
		newsItemContent.loadData(newsItem.content, "text/html; charset=UTF-8", null);

		WebSettings webViewSettings = newsItemContent.getSettings();
		webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webViewSettings.setJavaScriptEnabled(true);
		webViewSettings.setBuiltInZoomControls(true);
		webViewSettings.setPluginState(WebSettings.PluginState.ON);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(newsItem.title);
	}

	@OptionsItem(android.R.id.home)
	void homeClick() {
		NavUtils.navigateUpFromSameTask(this);
	}
}
