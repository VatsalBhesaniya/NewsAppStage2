package com.example.android.newsappstage2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {
    public static final String LOG_TAG = MainActivity.class.getName();
    private static String GAURDIAN_REQUEST_URL = "https://content.guardianapis.com/";
    private static int NEWS_LOADER_ID = 1;
    RelativeLayout relativeLayout;
    TextView toolbarTitle;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsAdapter mAdapter;
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbarTitle = findViewById(R.id.toolbar_title);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Drawable settingsIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_settings);
        toolbar.setOverflowIcon(settingsIcon);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        relativeLayout = findViewById(R.id.main_content);
        recyclerView = findViewById(R.id.recycler_view);
        mEmptyStateTextView = findViewById(R.id.empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new NewsAdapter(this, new ArrayList<News>());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //Calling method to fetch data.
        fetchData();
        //onClickListner to refresh data
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetchData() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    public void refreshFetchedData() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            // Restart the loader.
            loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    public void refreshData() {
        refreshFetchedData();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "TEST: onCreateLoader() called");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String section = sharedPrefs.getString(
                getString(R.string.settings_sections_key),
                getString(R.string.settings_sections_default));
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        // Setting toolbar title text.
        toolbarTitle.setText(setSectionName(section));
        // building  the URL string.
        Uri baseUri = Uri.parse(GAURDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath(section);
        uriBuilder.appendQueryParameter("api-key", "a9e74512-e71b-478a-af66-5b7ac6f2cffa");
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("show-fields", "thumbnail,body");
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        Log.i(LOG_TAG, "TEST: onLoadFinished() called");
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
        mEmptyStateTextView.setText(R.string.no_news);
        mAdapter.updateData(null);
        // If there is a valid list of {@link News}, then add them to the adapter's data set.
        // This will trigger the RecyclerView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.updateData(news);
            mEmptyStateTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        Log.i(LOG_TAG, "TEST: onLoaderReset() called");
        mAdapter.updateData(null);
    }

    private String setSectionName(String section) {
        String[] sectionsKeysArray = getResources().getStringArray(R.array.sections_keys);
        String[] sectionsValuesArray = getResources().getStringArray(R.array.sections_values);
        String sectionName = null;
        if (section.equals(sectionsValuesArray[0])) {
            sectionName = sectionsKeysArray[0];
        } else if (section.equals(sectionsValuesArray[1])) {
            sectionName = sectionsKeysArray[1];
        } else if (section.equals(sectionsValuesArray[2])) {
            sectionName = sectionsKeysArray[2];
        } else if (section.equals(sectionsValuesArray[3])) {
            sectionName = sectionsKeysArray[3];
        } else if (section.equals(sectionsValuesArray[4])) {
            sectionName = sectionsKeysArray[4];
        } else if (section.equals(sectionsValuesArray[5])) {
            sectionName = sectionsKeysArray[5];
        } else if (section.equals(sectionsValuesArray[6])) {
            sectionName = sectionsKeysArray[6];
        } else if (section.equals(sectionsValuesArray[7])) {
            sectionName = sectionsKeysArray[7];
        }
        return sectionName;
    }
}
