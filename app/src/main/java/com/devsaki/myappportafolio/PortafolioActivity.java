package com.devsaki.myappportafolio;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.Properties;

public class PortafolioActivity extends AppCompatActivity {

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portafolio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toast = Toast.makeText(this, R.string.btn_project_1, Toast.LENGTH_SHORT);
        try {
            Properties prop = new Properties();
            prop.load(getBaseContext().getAssets().open("app.properties"));
            String apiKey = prop.getProperty("themoviedb.api.key");
            Log.d(PortafolioActivity.class.getCanonicalName(), apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_portafolio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToProject1(View view) {
        toast.setText(R.string.btn_project_1);
        toast.show();

        Intent popularMovie = new Intent(this, PopularMovieActivity.class);
        startActivity(popularMovie);
    }

    public void goToProject2(View view) {
        toast.setText(R.string.btn_project_2);
        toast.show();
    }

    public void goToProject3(View view) {
        toast.setText(R.string.btn_project_3);
        toast.show();
    }

    public void goToProject4(View view) {
        toast.setText(R.string.btn_project_4);
        toast.show();
    }

    public void goToProject5(View view) {
        toast.setText(R.string.btn_project_5);
        toast.show();
    }

    public void goToProject6(View view) {
        toast.setText(R.string.btn_project_6);
        toast.show();
    }
}
