package no.radiomotor.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NewsFeedAdapter extends ArrayAdapter {

	private final int resource;
	private final LayoutInflater inflater;
	private final Context context;

	public NewsFeedAdapter(final Context ctx, final int resourceId, final List<RadiomotorXmlParser.Item> objects) {
		super(ctx, resourceId, objects);
		resource = resourceId;
		inflater = LayoutInflater.from(ctx);
		context = ctx;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		convertView = inflater.inflate(resource, null);

		RadiomotorXmlParser.Item item = (RadiomotorXmlParser.Item) getItem(position);

		TextView itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
		itemTitle.setText(item.title);

		return convertView;
	}
}
