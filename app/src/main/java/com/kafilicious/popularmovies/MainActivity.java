package com.kafilicious.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kafilicious.popularmovies.Models.MovieList;
import com.kafilicious.popularmovies.Models.MoviesResponse;
import com.kafilicious.popularmovies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private TextView mMovieCountTextView, mPageTextView, mCurrentPageTextView, ofTextView;
    private ArrayList<MovieList> movie_list;
    private Toast mToast;
    private MoviesResponse moviesResponse;
    private ImageView rightArrow, leftArrow;
    int currentPageNo = 1, totalPageNo = 1;
    private String SORT_POPULAR = "movie/popular";
    private String SORT_RATED = "movie/top_rated";
    String sortType = SORT_POPULAR;
    MovieListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Popular Movies");
        setSupportActionBar(toolbar);


        //Initializing variables for the various views used in the MainActivity

        movie_list = new ArrayList<MovieList>();
        moviesResponse = new MoviesResponse();
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mMovieCountTextView = (TextView) findViewById(R.id.total_movies);
        mPageTextView = (TextView) findViewById(R.id.tv_total_pages);
        mPageTextView.setVisibility(View.GONE);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);
        rightArrow.setOnClickListener(this);
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        leftArrow.setVisibility(View.GONE);
        leftArrow.setOnClickListener(this);
        ofTextView = (TextView) findViewById(R.id.tv_of);
        ofTextView.setVisibility(View.GONE);
        mCurrentPageTextView = (TextView) findViewById(R.id.tv_page_no);

        if(currentPageNo == 1){
            leftArrow.setVisibility(View.GONE);
        }


        //Setting the RecyclerView to a fixed size
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_posters);
        mRecyclerView.setHasFixedSize(true);
        //Setting the layout manager for the RecyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        //Setting the adapter for the RecyclerView
        adapter = new MovieListAdapter(movie_list);
        mRecyclerView.setAdapter(adapter);

        if (isNetworkAvailable()){
            loadMovies(sortType,currentPageNo);

        }else{

        }
    }

    public  void showViews(){
        mPageTextView.setVisibility(View.VISIBLE);
        ofTextView.setVisibility(View.VISIBLE);
    }
    public void setTextViews(long pages, long results){
        mMovieCountTextView.setText(String.valueOf(results));
        mPageTextView.setText(String.valueOf(pages));
    }
    public void updateUI(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
    public void showLoading(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void loadMovies(String sort, int page){
        URL movieRequestUrl = null;
        movieRequestUrl = NetworkUtils.buildUrl(sort, page);
        new FetchMovieTask().execute(movieRequestUrl);
    }

    public class FetchMovieTask extends AsyncTask<URL, Void, Void>{
        long totalPages = 0;
        long totalResults = 0;
        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected Void doInBackground(URL... params) {
            URL movieRequestUrl = params[0];
            String jsonResponse = null;
            try{
                jsonResponse =
                        NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                try {
                    JSONObject object = new JSONObject(jsonResponse);
                    totalPages = object.getLong("total_pages");
                    totalPageNo = (int) totalPages;
                    totalResults = object.getLong("total_results");
                    JSONArray jsonArray = object.getJSONArray("results");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);


                        MovieList addMovies = new MovieList();
                        addMovies.overview = obj.getString("overview");
                        addMovies.releaseDate = obj.getString("release_date");
                        addMovies.title = obj.getString("title");
                        addMovies.voteAverage = obj.getDouble("vote_average");
                        addMovies.voteCount = obj.getLong("vote_count");
                        addMovies.id = obj.getInt("id");
                        addMovies.posterPath = obj.getString("poster_path");
                        movie_list.add(addMovies);

                    }

                }catch (JSONException j){
                    j.printStackTrace();
                    return null;
                }
                return null;
            }catch (IOException e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateUI();
            showViews();
            MovieListAdapter adapter = new MovieListAdapter(movie_list);
            mRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            setTextViews(totalPages, totalResults);
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.right_arrow:
                if (currentPageNo < totalPageNo) {
                    currentPageNo++;
                    mCurrentPageTextView.setText(String.valueOf(currentPageNo));
                    leftArrow.setVisibility(View.VISIBLE);
                    adapter.setMovieData(null);
                    loadMovies(sortType, currentPageNo);
                    break;
                } else if (currentPageNo == (totalPageNo - 1)) {
                    currentPageNo++;
                    mCurrentPageTextView.setText(String.valueOf(currentPageNo));
                    rightArrow.setVisibility(View.GONE);
                    adapter.setMovieData(null);
                    loadMovies(sortType, currentPageNo);
                    break;
                }
            case R.id.left_arrow:
                if (currentPageNo > 1) {
                    currentPageNo--;
                    mCurrentPageTextView.setText(String.valueOf(currentPageNo));
                    adapter.setMovieData(null);
                    loadMovies(sortType, currentPageNo);
                    break;
                } else if (currentPageNo == 2) {
                    leftArrow.setVisibility(View.INVISIBLE);
                    currentPageNo--;
                    mCurrentPageTextView.setText(String.valueOf(currentPageNo));
                    adapter.setMovieData(null);
                    loadMovies(sortType, currentPageNo);
                    break;
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                //will start about activity yet to be added
                return true;
            case R.id.action_settings:
                //if setting will be later added this will implement it
                return true;
            case R.id.action_sort_popular:
                if (sortType != SORT_POPULAR) {
                    sortType = SORT_POPULAR;
                    adapter.setMovieData(null);
                    loadMovies(sortType, currentPageNo);
                }
                return true;
            case R.id.action_sort_top_rated:
                if (sortType != SORT_RATED){
                    sortType = SORT_RATED;
                    adapter.setMovieData(null);
                    loadMovies(sortType, currentPageNo);
                }
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isNetworkAvailable(){
        boolean status = false;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            status = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }
}