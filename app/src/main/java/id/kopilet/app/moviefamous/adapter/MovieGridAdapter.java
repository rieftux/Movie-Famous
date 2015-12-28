package id.kopilet.app.moviefamous.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import id.kopilet.app.moviefamous.R;
import id.kopilet.app.moviefamous.model.Movie;

/**
 * Created by rieftux on 27/12/15.
 */
public class MovieGridAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = MovieGridAdapter.class.getSimpleName();

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context        The current context. Used to inflate the layout file.
     * @param movieList A List of AndroidFlavor objects to display in a list
     */
    public MovieGridAdapter(Context context, List<Movie> movieList) {
        super(context, 0, movieList);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the AndroidFlavor object from the ArrayAdapter at the appropriate position
        Movie movie = getItem(position);
        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_thumb_movie, parent, false);
        }

        ImageView thumb_poster = (ImageView) convertView.findViewById(R.id.thumb_movie);
        Picasso.with(getContext()).load(movie.getPoster()).into(thumb_poster);

        return convertView;
    }
}
