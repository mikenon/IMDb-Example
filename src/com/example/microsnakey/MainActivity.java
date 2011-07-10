package com.example.microsnakey;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import android.net.Uri;

import com.example.microsnakey.RestClient;
import com.example.microsnakey.RestClient.RequestMethod;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// this dialog will show the full size movie poster
	Dialog dialog = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("onCreate", "1");
		setContentView(R.layout.main);
		Log.i("onCreate", "2");

		Button btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String strTitle = ((EditText) findViewById(R.id.txtTitle))
						.getText().toString().trim();
				String strYear = ((EditText) findViewById(R.id.txtYear))
						.getText().toString().trim();
				String query = "";

				if (strTitle != "") {
					strTitle = Uri.encode(strTitle);
					query = "?t=" + strTitle;
				}
				if (strYear.length() > 1) {
					strYear = Uri.encode(strYear);
					query = query + "&y=" + strYear;
				}

				Log.i("Query", query);

				getTask task = new getTask();
				task.applicationContext = MainActivity.this;
				task.execute(query);
			}
		});
	}

	private Bitmap loadThumb(String url, int reduceBy) {
		Bitmap thumb = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = reduceBy;
		try {
			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.connect();
			BufferedInputStream stream = new BufferedInputStream(
					c.getInputStream());
			thumb = BitmapFactory.decodeStream(stream, null, opts);
			stream.close();
		} catch (MalformedURLException e) {
			Log.e("Threads03", "malformed url: " + url);
		} catch (IOException e) {
			Log.e("Threads03", "An error has occurred downloading the image: "
					+ url);
		}
		return thumb;
	}

	public void displayResults(final movieInfo result) {
		EditText txtTitle = (EditText) findViewById(R.id.txtTitle);
		EditText txtYear = (EditText) findViewById(R.id.txtYear);
		txtTitle.setText(result.Title);
		txtYear.setText(result.Year);

		String strSummary = "Written by " + result.Writer + "\nDirected by "
				+ result.Director + "\nStarring " + result.Actors
				+ "\nReleased: " + result.Released + "\nRated: " + result.Rated
				+ "\nGenre" + result.Genre + "\n" + result.Plot + "\n"
				+ result.Runtime;
		TextView txtAbout = (TextView) findViewById(R.id.txtAbout);
		txtAbout.setText(strSummary);

		ImageView mainImage = (ImageView) findViewById(R.id.imageView1);
		Bitmap thumb = loadThumb(result.Poster, 2);
		mainImage.setImageBitmap(thumb);
		mainImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				openDieahlog(result.Poster, result.Title + ", " + result.Year);
			}
		});
	}

	public class movieInfo {
		public String Title;
		public String Year;
		public String Rated;
		public String Released;
		public String Genre;
		public String Director;
		public String Writer;
		public String Actors;
		public String Plot;
		public String Poster;
		public String Runtime;
		public String Rating;
		public String Votes;
		public String ID;
		public String Resonse;

		@Override
		public String toString() {
			return Title + ", " + Year + "\nWritten by " + Writer
					+ ", Directed by " + Director + "\nStarring " + Actors;
		}
	}

	private class getTask extends AsyncTask<String, Void, movieInfo> {
		// http://www.imdbapi.com/?t=True%20Grit&y=1969
		private ProgressDialog dialog;
		protected Context applicationContext;

		@Override
		protected void onPreExecute() {

			this.dialog = ProgressDialog.show(applicationContext,
					"IMDb Example", "Requesting Movie Info", true);
		}

		@Override
		protected movieInfo doInBackground(String... theQryStr) {
			movieInfo result = null;
			RestClient client = new RestClient("http://www.imdbapi.com/"
					+ theQryStr[0]);
			client.AddHeader("User-Agent", "com.example.microsnakey/Android");
			try {
				client.Execute(RequestMethod.GET);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String jason = client.getResponse();
			Log.d("HTTP Response", "" + client.getResponseCode());
			Log.i("JSON Response", jason);
			try {
				result = new Gson().fromJson(jason, movieInfo.class);
			} catch (Exception e) {
			}
			return result;
		}

		protected void onPostExecute(movieInfo result) {
			this.dialog.cancel();
			int duration = Toast.LENGTH_SHORT;
			this.dialog.cancel();
			if (result == null) {
				Toast toast = Toast.makeText(applicationContext,
						"Request Failed", duration);
				toast.show();
				this.cancel(true);
			}
			Toast toast = Toast.makeText(applicationContext, "Request Success",
					duration);
			toast.show();
			Log.i("MovieInfo", result.toString());
			displayResults(result);
		}
	}

	public void openDieahlog(String imageUrl, String movieTitle) {
		dialog = new Dialog(MainActivity.this);
		dialog.setContentView(R.layout.fullimage);
		dialog.setTitle(movieTitle);
		dialog.setCancelable(true);
		ImageView posterImage = (ImageView) dialog
				.findViewById(R.id.posterImage);
		Bitmap thumb = loadThumb(imageUrl, 1);
		posterImage.setImageBitmap(thumb);
		Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.i("Buhton", "Close Clicked");
				dialog.dismiss();
			}
		});
		dialog.show();
	}
}