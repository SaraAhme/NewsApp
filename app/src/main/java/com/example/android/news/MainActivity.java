package com.example.android.news;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<NewsFeed>> {
    ListView newsfeed;
    NewsAdapter adapter;
    TextView nodata;
    ProgressDialog progressDialog;
    private static int LOADER_ID = 1;
    private String mSearchQuery;
    SharedPreferences preferences;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nodata = (TextView) findViewById(R.id.nodata);
        newsfeed = (ListView) findViewById(R.id.newslist);
        progressDialog = new ProgressDialog(this);
        ArrayList<NewsFeed> list = new ArrayList<>();
        adapter = new NewsAdapter(this, list);
        if (isNetworkOnline()) {
            LoaderManager loaderManager = getSupportLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(LOADER_ID, null, this).forceLoad();
        } else {
            Toast.makeText(this, R.string.check_your_connection, Toast.LENGTH_LONG).show();
            progressDialog.hide();
        }


    }

    public void updateUI(ArrayList<NewsFeed> list) {
        adapter = new NewsAdapter(this, list);
        newsfeed.setAdapter(adapter);
    }

    public boolean isNetworkOnline() {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;

    }

    @Override
    public Loader<ArrayList<NewsFeed>> onCreateLoader(int id, Bundle args) {
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String section = sharedPreferences.getString(getString(R.string.settings_section_news_key), getString(R.string.settings_section_news_default));
        Uri baseUri = Uri.parse("http://content.guardianapis.com/search?q=debates&section=politics&show-tags=contributor&api-key=9f66f90d-99d5-4f16-bd63-8bbdb28f3321");
        Uri.Builder uriBuilder = baseUri.buildUpon();
        if (!section.equals(getString(R.string.settings_section_news_default))) {
            uriBuilder.appendQueryParameter("section", section);
        }


        return new NewsLoader(MainActivity.this, uriBuilder.toString());
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<NewsFeed>> loader, ArrayList<NewsFeed> data) {
        progressDialog.hide();
        // Clear the adapter of previous books data
        adapter.clear();
        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            adapter.addAll(data);
            updateUI(data);
//            preferences.edit().remove("query").apply();
        } else {
            nodata.setVisibility(View.VISIBLE);
            newsfeed.setVisibility(View.GONE);

        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<NewsFeed>> loader) {
        adapter.clear();
    }
}

class NewsLoader extends AsyncTaskLoader<ArrayList<NewsFeed>> {

    private String mUrl;

    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    public ArrayList<NewsFeed> loadInBackground() {
        URL url = createUrl(mUrl);
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        ArrayList<NewsFeed> newsFeed;
        if (mUrl == null) {
            return null;
        }
        try {
            jsonResponse = makeHttpRequest(url);
            Log.e("response", jsonResponse);

        } catch (IOException e) {
        }
        newsFeed = extractFeatureFromJson(jsonResponse);
        return newsFeed;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e("MainTAg", "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
        } catch (IOException e) {
            // TODO: Handle the exception
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private ArrayList<NewsFeed> extractFeatureFromJson(String earthquakeJSON) {
        try {
            ArrayList<NewsFeed> list = new ArrayList<>();
            JSONObject baseJsonResponse = new JSONObject(earthquakeJSON);
            JSONObject response = baseJsonResponse.getJSONObject("response");
            JSONArray resultsArray = response.getJSONArray("results");

            // If there are results in the features array
            if (resultsArray.length() > 0) {
                for (int i = 0; i < resultsArray.length(); i++) {
                    // Extract out the first feature (which is an earthquake)
                    JSONObject firstnews = resultsArray.getJSONObject(i);
                    // Extract out the title, time, and tsunami values
                    String title = firstnews.getString("webTitle");
                    String type = firstnews.getString("type");
                    String sectionName = firstnews.getString("sectionName");
                    String auther = "";
                    String date = firstnews.getString("webPublicationDate");
                    String weburl = firstnews.getString("webUrl");
                    if (firstnews.has("tags")) {
                        JSONArray tages = firstnews.getJSONArray("tags");
                        for (int j = 0; j < tages.length(); j++) {
                            JSONObject tg = (JSONObject) tages.get(j);
                            Log.e("tag", tg.toString());
                            auther = tg.getString("webTitle");
                        }
                    }
                    NewsFeed newsFeed = new NewsFeed(title, type, auther, date, weburl, sectionName);
                    list.add(newsFeed);
                    Log.e("MainTag", newsFeed.toString());
                }

                // Create a new {@link Event} object
                return list;
            }
        } catch (JSONException e) {
            Log.e("MainTag", "Problem parsing the earthquake JSON results", e);
        }
        return null;
    }


}
