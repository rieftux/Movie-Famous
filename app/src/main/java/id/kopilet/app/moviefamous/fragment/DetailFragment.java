package id.kopilet.app.moviefamous.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import id.kopilet.app.moviefamous.R;
import id.kopilet.app.moviefamous.model.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    Movie movie;

    public DetailFragment() {
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
        }

        return rootView;
    }
}
