package id.kopilet.app.moviefamous.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rieftux on 27/12/15.
 */
public class Movie implements Parcelable{

    String poster;
    String tittle;
    String synopsis;
    String rating;
    String release;

    public Movie(String poster, String tittle, String synopsis, String rating, String release) {
        this.poster = poster;
        this.tittle = tittle;
        this.synopsis = synopsis;
        this.rating = rating;
        this.release = release;
    }

    public String getPoster() {
        return poster;
    }

    public String getTittle() {
        return tittle;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getRating() {
        return rating;
    }

    public String getRelease() {
        return release;
    }

    protected Movie(Parcel in) {
        poster = in.readString();
        tittle = in.readString();
        synopsis = in.readString();
        rating = in.readString();
        release = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(poster);
        dest.writeString(tittle);
        dest.writeString(synopsis);
        dest.writeString(rating);
        dest.writeString(release);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
