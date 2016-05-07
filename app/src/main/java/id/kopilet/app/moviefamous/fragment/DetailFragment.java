package id.kopilet.app.moviefamous.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
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
import id.kopilet.app.moviefamous.adapter.TrailerListAdapter;
import id.kopilet.app.moviefamous.model.Movie;
import id.kopilet.app.moviefamous.model.TrailerItem;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    protected final String LOG_TAG = DetailFragment.class.getSimpleName();

    private TrailerListAdapter mTrailerAdapter;
    private ArrayList<TrailerItem> mTrailerList;

    Movie movie;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || savedInstanceState.containsKey("trailers")) {
            mTrailerList = new ArrayList<>();
            mTrailerAdapter = new TrailerListAdapter(getActivity(), new ArrayList<TrailerItem>());
            Log.v(LOG_TAG, "instance state null");
        } else {
            mTrailerList = savedInstanceState.getParcelableArrayList("trailers");
            mTrailerAdapter = new TrailerListAdapter(getActivity(), mTrailerList);
            Log.v(LOG_TAG, "using saved instance state");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("trailers", mTrailerList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movie = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            ImageView poster = (ImageView) rootView.findViewById(R.id.poster_thumb);
            Picasso.with(getContext()).load(movie.getPoster()).into(poster);

            ((TextView) rootView.findViewById(R.id.textTitle)).setText(movie.getTittle());
            ((TextView) rootView.findViewById(R.id.textRating)).setText(movie.getRating());
            ((TextView) rootView.findViewById(R.id.textRelease)).setText(movie.getRelease());
            ((TextView) rootView.findViewById(R.id.textSynopsis)).setText(movie.getSynopsis());

            ListView listView = (ListView) rootView.findViewById(R.id.listTrailer);
            listView.setAdapter(mTrailerAdapter);

            getTrailer(movie.getId());

        }

        return rootView;
    }

    protected void getTrailer(int idFilm) {
        FetchTrailerTask trailerTask = new FetchTrailerTask();
        trailerTask.execute(idFilm);
        Log.i(LOG_TAG, "Get trailer");
    }

    protected class FetchTrailerTask extends AsyncTask<Integer, Void, TrailerItem[]> {

        protected TrailerItem[] getTrailerDataFromJson(String trailerJsonStr) throws JSONException {
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

                String youtubeUrl;

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

                Log.v(LOG_TAG, "URL : " + url);

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
                mTrailerAdapter.clear();
                for (TrailerItem t : trailerItems) {
                    mTrailerAdapter.add(t);
                }
                mTrailerList.clear();
                mTrailerList.addAll(Arrays.asList(trailerItems));
            }
        }
    }
}
