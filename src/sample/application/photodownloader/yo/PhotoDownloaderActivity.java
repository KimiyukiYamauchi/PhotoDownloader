package sample.application.photodownloader.yo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

public class PhotoDownloaderActivity extends Activity {

	Context mContext = this;
	Object[] objects;
	static String strUrl = "https://api.datamarket.azure.com/Bing/Search/Image?$format=json&$top=10";
	char[] accountKey = "otI1qkcCwoIQUjg698igtHhxo8jVC/Ku5XN/rA14kzM="
			.toCharArray();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setText(Html.fromHtml(getResources().getString(
				R.string.label_branding)));
		tv.setMovementMethod(LinkMovementMethod.getInstance());

		View button1 = findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Log.d("PhotoDownloadActivity!!!", "onClick(View v)");
				doSearch();
			}
		});

		View editText1 = findViewById(R.id.editText1);
		editText1.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					doSearch();
					return true;
				}
				return false;
			}
		});

		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				PasswordAuthentication pa = new PasswordAuthentication("",
						accountKey);
				// Log.d("PhotoDownloadActivity!!!", "pa = " + pa);
				return pa;
			}
		});

	}

	void doSearch() {

		Log.d("PhotoDownloadActivity!!!", "doSearch()");

		URL url;
		String q = null;
		EditText et = (EditText) this.findViewById(R.id.editText1);

		try {
			q = URLEncoder.encode(et.getText().toString(), "UTF-8");
			url = new URL(strUrl + "&Query='" + q + "'");
			// Log.d("PhotoDownloadActivity!!!", "doSearch() url = " + url);
			new jsonTask().execute(url);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private class jsonTask extends AsyncTask<URL, Integer, String> {

		@Override
		protected String doInBackground(URL... params) {
			HttpURLConnection connection = null;

			try {
				connection = (HttpURLConnection) params[0].openConnection();
				connection.setDoInput(true);
				connection.connect();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream(),
								"UTF-8"));
				String jsonText = reader.readLine();
				// Log.d("PhotoDownloadActivity!!!",
				// "doInBackground(URL... params) jsonText = " + jsonText);
				reader.close();
				return (jsonText);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			// Log.d("PhotoDownloadActivity!!!", "onPostExecute() result = "
			// + result);

			if (result != null) {

				try {
					JSONObject jo = new JSONObject(result).getJSONObject("d");
					JSONArray jsonArray = null;
					jsonArray = jo.getJSONArray("results");

					objects = new Object[jsonArray.length()];
					URL[] thumbUrls = new URL[jsonArray.length()];

					for (int i = 0; i < jsonArray.length(); i++) {
						objects[i] = jsonArray.getJSONObject(i);
						thumbUrls[i] = new URL(((JSONObject) objects[i])
								.getJSONObject("Thumbnail").getString(
										"MediaUrl"));
					}

					Log.d("PhotoDownloadActivity!!!",
							"onPostExecute() objects = " + objects);

					new getThumbTask().execute(thumbUrls);

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		private class getThumbTask extends AsyncTask<URL, Integer, Bitmap[]> {

			@Override
			protected Bitmap[] doInBackground(URL... params) {

				Log.d("getThumbTask", "doInBackground() params = " + params);

				HttpURLConnection connection = null;
				Bitmap[] bm = new Bitmap[params.length];

				try {
					for (int i = 0; i < params.length; i++) {
						connection = (HttpURLConnection) params[i]
								.openConnection();
						connection.setDoInput(true);
						connection.connect();
						InputStream si = connection.getInputStream();
						bm[i] = BitmapFactory.decodeStream(si);
						si.close();
					}
					return bm;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}

				return null;
			}

			@Override
			protected void onPostExecute(Bitmap[] result) {
				super.onPostExecute(result);

				final ThumbnailAdapter adapter = new ThumbnailAdapter(mContext,
						objects, result);
				GridView lv = (GridView) findViewById(android.R.id.list);
				lv.setAdapter(adapter);

				lv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {

						Uri downloadUri;
						try {
							downloadUri = Uri.parse(((JSONObject) adapter
									.getItem(position)).getString("MediaUrl"));
							DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
							DownloadManager.Request dr = new DownloadManager.Request(
									downloadUri);
							String fn = URLDecoder.decode(
									downloadUri.getLastPathSegment(), "UTF-8");
							dr.setDestinationInExternalPublicDir(
									Environment.DIRECTORY_DOWNLOADS, fn);
							dm.enqueue(dr);
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}

					}
				});
			}

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
