package sample.application.photodownloader.yo;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ThumbnailAdapter extends ArrayAdapter<Object> {
	static final int text1 = android.R.id.text1;
	static final int text2 = android.R.id.text2;
	static final int icon = android.R.id.icon;
	LayoutInflater mInflater;
	Bitmap[] bm;
	Object[] objects;

	public ThumbnailAdapter(Context context, Object[] objects, Bitmap[] bm) {

		super(context, text1, objects);

		Log.d("ThumbnailAdapter", "ThumbnailAdapter() bm = " + bm);

		this.bm = bm;
		this.objects = objects;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Log.d("ThumbnailAdapter", "getView() objects = " + objects);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_with_icon, null);
		}

		TextView tv1 = (TextView) convertView.findViewById(text1);
		TextView tv2 = (TextView) convertView.findViewById(text2);
		ImageView iv = (ImageView) convertView.findViewById(icon);

		JSONObject jo = (JSONObject) objects[position];
		String hostname = "";
		String title = "";

		try {
			URL mediaURL = new URL(jo.getString("MediaUrl"));
			hostname = mediaURL.getHost();
			title = jo.getString("Title");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		tv1.setText(title);
		tv2.setText(hostname);
		iv.setImageBitmap(bm[position]);

		return convertView;
	}

}
