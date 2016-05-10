package id.kopilet.app.moviefamous.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import id.kopilet.app.moviefamous.BuildConfig;
import id.kopilet.app.moviefamous.R;
import id.kopilet.app.moviefamous.adapter.ReviewListAdapter;
import id.kopilet.app.moviefamous.adapter.TrailerListAdapter;
import id.kopilet.app.moviefamous.customview.NonScrollListView;
import id.kopilet.app.moviefamous.model.Movie;
import id.kopilet.app.moviefamous.model.ReviewItem;
import id.kopilet.app.moviefamous.model.TrailerItem;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String DETAIL_MOVIE = "MOVIE";

    private TrailerListAdapter mTrailerAdapter;
    private ReviewListAdapter mReviewAdapter;
    private ArrayList<TrailerItem> mTrailerList;
    private ArrayList<ReviewItem> mReviewList;

    private Movie movie;
    private int idMovie;


    private TextView tvTitle;
    private TextView tvRelease;
    private TextView tvRating;
    private TextView tvSynopsis;
    private ImageView ivPoster;
    private NonScrollListView lvTrailer;
    private NonScrollListView lvReview;

    public static DetailFragment newInstance(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(DetailFragment.DETAIL_MOVIE, movie);

        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(args);
        return detailFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        tvTitle = (TextView) rootView.findViewById(R.id.textTitle);
        tvRating = (TextView) rootView.findViewById(R.id.textRating);
        tvRelease = (TextView) rootView.findViewById(R.id.textRelease);
        tvSynopsis = (TextView) rootView.findViewById(R.id.textSynopsis);
        ivPoster = (ImageView) rootView.findViewById(R.id.poster_thumb);

        lvTrailer = (NonScrollListView) rootView.findViewById(R.id.listTrailer);
        lvReview = (NonScrollListView) rootView.findViewById(R.id.listReview);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            movie = arguments.getParcelable(DETAIL_MOVIE);
            mTrailerList = new ArrayList<>();
            mTrailerAdapter = new TrailerListAdapter(getActivity(), mTrailerList);

            mReviewList = new ArrayList<>();
            mReviewAdapter = new ReviewListAdapter(getActivity(), mReviewList);

            insertData();

            lvTrailer.setAdapter(mTrailerAdapter);
            lvReview.setAdapter(mReviewAdapter);

            // Youtube Intent
            lvTrailer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String videoID = mTrailerAdapter.getItem(position).getKey();
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoID));
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + videoID));
                        startActivity(intent);
                    }
                }
            });
        }
        super.onActivityCreated(savedInstanceState);
    }

    private void insertData() {
        tvTitle.setText(movie.getTittle());
        tvRating.setText(movie.getRating());
        tvRelease.setText(movie.getRelease());
        tvSynopsis.setText(movie.getSynopsis());

        Picasso.with(getContext()).load(movie.getPoster()).into(ivPoster);

        getTrailer(movie.getId());
        getReview(movie.getId());
    }

    private void getTrailer(int id) {
        Log.v(LOG_TAG, "get trailer");
        FetchTrailer fetchTrailer = new FetchTrailer();
        fetchTrailer.execute(id);
    }

    // Asyntack for get Trailer
    private class FetchTrailer extends AsyncTask<Integer, Void, TrailerItem[]> {

        private TrailerItem[] getTrailerDataFromJson(String trailerJsonStr) throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String TRAILER_LIST = "results";
            final String TRAILER_KEY_URL = "key";
            final String TRAILER_NAME = "name";

            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray(TRAILER_LIST);

            TrailerItem trailerItem[] = new TrailerItem[trailerArray.length()];
            for (int i = 0; i < trailerArray.length(); i++) {

                String key;
                String name;

                JSONObject trailerObj = trailerArray.getJSONObject(i);
                key = trailerObj.getString(TRAILER_KEY_URL);
                name = trailerObj.getString(TRAILER_NAME);

                trailerItem[i] = new TrailerItem(key, name);
            }

            return trailerItem;
        }

        @Override
        protected TrailerItem[] doInBackground(Integer... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailerJsonStr = null;

            try {
                // Construct the URL for the The Movie DB query
                // Possible parameters are available at The Movie DB's apiary page, at
                // http://docs.themoviedb.apiary.io/#

                final String MOVIEDB_TRAILER_URL = "http://api.themoviedb.org/3/movie/";
                final String MOVIE_ID = params[0].toString();
                final String VIDEO_PARAM = "videos";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIEDB_TRAILER_URL).buildUpon()
                        .appendPath(MOVIE_ID)
                        .appendPath(VIDEO_PARAM)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "VIDEOS MOVIE ID : " + MOVIE_ID);

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                // StringBuffer buffer = new StringBuffer();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    // buffer.append(line + "\n");
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                trailerJsonStr = buffer.toString();


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailerDataFromJson(trailerJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(TrailerItem[] trailerItems) {
            if (trailerItems != null) {
       /*         mTrailerAdapter.clear();
                for (TrailerItem t : trailerItems) {
                    mTrailerAdapter.add(t);

                }*/
                mTrailerList.clear();
                mTrailerList.addAll(Arrays.asList(trailerItems));
                mTrailerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void getReview(int id) {
        Log.v(LOG_TAG, "get review");
        FetchReview fetchTrailer = new FetchReview();
        fetchTrailer.execute(id);
    }

    // Asyntack for get Review
    private class FetchReview extends AsyncTask<Integer, Void, ReviewItem[]> {

        private ReviewItem[] getReviewDataFromJson(String reviewJsonStr) throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String REVIEW_LIST = "results";
            final String REVIEW_AUTHOR = "author";
            final String REVIEW_CONTENT = "content";
            final String REVIEW_URL = "url";

            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(REVIEW_LIST);

            ReviewItem reviewItems[] = new ReviewItem[reviewArray.length()];
            for (int i = 0; i < reviewArray.length(); i++) {

                String author;
                String content;
                String url;

                JSONObject reviewObj = reviewArray.getJSONObject(i);
                author = reviewObj.getString(REVIEW_AUTHOR);
                content = reviewObj.getString(REVIEW_CONTENT);
                url = reviewObj.getString(REVIEW_URL);

                reviewItems[i] = new ReviewItem(author, content, url);
            }

            return reviewItems;
        }

        @Override
        protected ReviewItem[] doInBackground(Integer... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String reviewJsonStr = null;

            try {
                // Construct the URL for the The Movie DB query
                // Possible parameters are available at The Movie DB's apiary page, at
                // http://docs.themoviedb.apiary.io/#

                final String MOVIEDB_TRAILER_URL = "http://api.themoviedb.org/3/movie/";
                final String MOVIE_ID = params[0].toString();
                final String REVIEW_PARAM = "reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIEDB_TRAILER_URL).buildUpon()
                        .appendPath(MOVIE_ID)
                        .appendPath(REVIEW_PARAM)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "REVIEW MOVIE ID : " + MOVIE_ID);

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                // StringBuffer buffer = new StringBuffer();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    // buffer.append(line + "\n");
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                reviewJsonStr = buffer.toString();


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewDataFromJson(reviewJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ReviewItem[] reviewItems) {
            if (reviewItems != null) {
              /*  mReviewAdapter.clear();
                for (ReviewItem r : reviewItems) {
                    mReviewAdapter.add(r);
                }*/
                mReviewList.clear();
                mReviewList.addAll(Arrays.asList(reviewItems));
                mReviewAdapter.notifyDataSetChanged();
            }
        }
    }
}
