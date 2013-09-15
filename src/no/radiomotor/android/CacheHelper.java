package no.radiomotor.android;

import android.content.Context;
import android.os.Parcel;

import java.io.*;
import java.util.ArrayList;

import static no.radiomotor.android.RadiomotorXmlParser.Item;

public class CacheHelper {

	private File cacheFile;

	public CacheHelper(Context ctx) {
		this.cacheFile = new File(ctx.getFilesDir(), "cache");
	}
	public void writeNewsItems(ArrayList<Item> items) throws IOException {
		FileOutputStream fout = new FileOutputStream(cacheFile);

		Parcel parcel = Parcel.obtain();
		ArrayList<Item> list = new ArrayList<Item>(items);
		parcel.writeList(list);
		byte[] data = parcel.marshall();
		fout.write(data);
	}

	ArrayList<Item> readNewsItems() throws IOException {
		FileInputStream fin;
		try {
			fin = new FileInputStream(cacheFile);
		} catch (FileNotFoundException e) {
			return new ArrayList<Item>();
		}
		byte[] data = new byte[(int) cacheFile.length()];
		fin.read(data);

		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(data, 0, data.length);
		parcel.setDataPosition(0);
		return parcel.readArrayList(Object.class.getClassLoader());
	}

}
