package com.xyp.tiange.pathdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * User: xyp
 * Date: 2017/7/14
 * Time: 16:12
 */
public class MainActivity extends AppCompatActivity {
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchView = (SearchView) findViewById(R.id.searchView);
    }

    public void start(View view) {
        searchView.startSearch();
    }

    public void stop(View view) {
        searchView.stopSearch();
    }
}
