package id.kopilet.app.moviefamous;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import id.kopilet.app.moviefamous.fragment.DetailFragment;
import id.kopilet.app.moviefamous.model.Movie;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Movie m = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);
            DetailFragment detailFragment = DetailFragment.newInstance(m);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_detail, detailFragment)
                    .commit();
        }

    }

}
