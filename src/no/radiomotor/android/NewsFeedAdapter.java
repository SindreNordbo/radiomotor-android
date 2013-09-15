package no.radiomotor.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import static no.radiomotor.android.RadiomotorXmlParser.Item;

public class NewsFeedAdapter extends ArrayAdapter {

	private final int resource;
	private final LayoutInflater inflater;

	public NewsFeedAdapter(final Context ctx, final int resourceId, final List<Item> objects) {
		super(ctx, resourceId, objects);
		resource = resourceId;
		inflater = LayoutInflater.from(ctx);
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		convertView = inflater.inflate(resource, null);

		Item item = (Item) getItem(position);

		TextView itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
		itemTitle.setText(item.title);

		return convertView;
	}
}
