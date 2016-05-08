package id.kopilet.app.moviefamous.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import id.kopilet.app.moviefamous.R;
import id.kopilet.app.moviefamous.model.TrailerItem;

/**
 * Created by rieftux on 07/05/16.
 */
public class TrailerListAdapter extends ArrayAdapter<TrailerItem>{

    public TrailerListAdapter(Context context, List<TrailerItem> trailerList) {
        super(context, 0, trailerList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TrailerHolder trailerHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_trailer, parent, false);
            trailerHolder = new TrailerHolder(convertView);
            convertView.setTag(trailerHolder);
        } else {
            trailerHolder = (TrailerHolder) convertView.getTag();
        }

        TrailerItem trailerItem = getItem(position);
        trailerHolder.tvTrailer.setText(trailerItem.getName());



        return convertView;
    }

    private static class TrailerHolder {
        public final TextView tvTrailer;

        public TrailerHolder(View view) {
            this.tvTrailer = (TextView) view.findViewById(R.id.trailerTitle);
        }
    }
}
