package id.kopilet.app.moviefamous.fragment;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import id.kopilet.app.moviefamous.BuildConfig;
import id.kopilet.app.moviefamous.R;
import id.kopilet.app.moviefamous.Utility;
import id.kopilet.app.moviefamous.adapter.MovieGridAdapter;
import id.kopilet.app.moviefamous.model.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();

    private MovieGridAdapter mMovieGridAdapter;
    private ArrayList<Movie> mMovieArrayList;

    public interface Callback {
        void onItemSelected(Movie movie);
    }

    public MovieFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("flavors")) {
            mMovieArrayList = new ArrayList<>();
            mMovieGridAdapter = new MovieGridAdapter(getActivity(), new ArrayList<Movie>());
            Log.v(LOG_TAG, "instance state null");
        } else {
            mMovieArrayList = savedInstanceState.getParcelableArrayList("flavors");
            mMovieGridAdapter = new MovieGridAdapter(getActivity(), mMovieArrayList);
            Log.v(LOG_TAG, "using saved instance state");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        gridView.setAdapter(mMovieGridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movieClick = mMovieGridAdapter.getItem(position);

                ((Callback) getActivity()).onItemSelected(movieClick);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMovie();
    }

    private void updateMovie() {
        String SORT_BY = Utility.getSortedBy(getActivity());

        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute(SORT_BY);
        Log.i(LOG_TAG, "Update Movie method");
    }

    private class FetchMovieTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(String date) {
            // Because the API returns date in YYYY-MM-DD,
            // it must be converted to readable date.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat readableFormat = new SimpleDateFormat("MMMM dd, yyyy");
            try {
                Date mDate = dateFormat.parse(date);
                return readableFormat.format(mDate);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Error Date : " + e);
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Take the Array Movie representing the complete movie data in JSON Format and
         * pull out the data we need to construct the Movies needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private Movie[] getMovieDataFromJson(String movieJsonStr) throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIE_LIST = "results";
            final String MOVIE_ID = "id";
            final String MOVIE_POSTER = "poster_path";
            final String MOVIE_ORI_TITLE = "original_title";
            final String MOVIE_SYNOPSIS = "overview";
            final String MOVIE_RATING = "vote_average";
            final String MOVIE_RELEASE = "release_date";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);

            Movie[] mMovie = new Movie[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {

                int id;
                String poster_path;
                String title;
                String synopsis;
                String rating;
                String release;

                String urlPoster = "http://image.tmdb.org/t/p/w342/";

                JSONObject movieObj = movieArray.getJSONObject(i);

                id = movieObj.getInt(MOVIE_ID);
                poster_path = movieObj.getString(MOVIE_POSTER);
                title = movieObj.getString(MOVIE_ORI_TITLE);
                synopsis = movieObj.getString(MOVIE_SYNOPSIS);
                rating = String.valueOf(movieObj.getDouble(MOVIE_RATING));
                release = getReadableDateString(movieObj.getString(MOVIE_RELEASE));

                mMovie[i] = new Movie(id, urlPoster.concat(poster_path), title, synopsis, rating, release);
            }

            return mMovie;
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the The Movie DB query
                // Possible parameters are available at The Movie DB's apiary page, at
                // http://docs.themoviedb.apiary.io/#

//                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3/discover/movie/";
                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String SORT__PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";
/*

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT__PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();
*/
                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "DISCOVER : " + params[0]);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
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
//                    buffer.append(line + "\n");
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                movieJsonStr = buffer.toString();

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
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movieResults) {
            if (movieResults != null) {
                mMovieGridAdapter.clear();
                for (Movie m : movieResults) {
                    mMovieGridAdapter.add(m);
                }
                mMovieArrayList.clear();
                mMovieArrayList.addAll(Arrays.asList(movieResults));
                mMovieGridAdapter.notifyDataSetChanged();
                Log.v(LOG_TAG, "DOne Asyntask");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("flavors", mMovieArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
