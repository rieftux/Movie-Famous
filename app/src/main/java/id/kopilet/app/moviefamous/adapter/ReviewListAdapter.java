package id.kopilet.app.moviefamous.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import id.kopilet.app.moviefamous.R;
import id.kopilet.app.moviefamous.model.ReviewItem;

/**
 * Created by rieftux on 07/05/16.
 */
public class ReviewListAdapter extends ArrayAdapter<ReviewItem>{

    public ReviewListAdapter(Context context, List<ReviewItem> reviewList) {
        super(context, 0, reviewList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ReviewHolder reviewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_review, parent, false);
            reviewHolder = new ReviewHolder(convertView);
            convertView.setTag(reviewHolder);
        } else {
            reviewHolder = (ReviewHolder) convertView.getTag();
        }

        ReviewItem reviewItem = getItem(position);
        reviewHolder.tvAuthor.setText(reviewItem.getAuthor());
        reviewHolder.tvContent.setText(reviewItem.getContent());

        return convertView;
    }

    private static class ReviewHolder {
        public final TextView tvAuthor;
        public final TextView tvContent;

        public ReviewHolder(View view) {
            this.tvAuthor = (TextView) view.findViewById(R.id.reviewAuthor);
            this.tvContent = (TextView) view.findViewById(R.id.reviewContent);
        }
    }
}
