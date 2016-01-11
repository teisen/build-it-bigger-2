package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.steelgirderdev.jokedisplaylibrary.DisplayActivity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private static final String LOGTAG = "MainActivity";
    String mJoke = null;
    Exception mError = null;
    CountDownLatch signal = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * called by the view to tell a joke.
     * Calls the endpoint async
     * @param view
     */
    public void tellJoke(View view){
        //Log.v(LOGTAG, "tellJoke...");
        signal = new CountDownLatch(1);

        try {
            EndpointsAsyncTask task = new EndpointsAsyncTask();
            task.setListener(new EndpointsAsyncTask.EndpointsAsyncTaskListener() {
                @Override
                public void onComplete(String str, Exception e) {
                    //Log.v(LOGTAG, "onComplete("+str+","+e+")");
                    mJoke = str;
                    mError = e;
                    signal.countDown();
                }
            }).execute(new Pair<Context, View>(null, null));
            //Log.v(LOGTAG, "before wait");
            signal.await(5, TimeUnit.SECONDS);
            //Log.v(LOGTAG, "after wait mJoke="+mJoke);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(LOGTAG, "Error:"+e.getLocalizedMessage());
        }

        if(mJoke==null) {
            Log.e(LOGTAG, "Error: No joke was retrieved.");
            mJoke = "Could not retrieve the Joke. Are you connected to the Joke Server?";
        }

        launchLibraryActivity(view, mJoke);
    }

    /**
     * Launched an Intent with the Text of a Joke to display.
     * Called by the async Task onPostExecute
     * @param view
     * @param joke
     */
    public void launchLibraryActivity(View view, String joke){
        Intent myIntent = new Intent(this, DisplayActivity.class);
        myIntent.putExtra(DisplayActivity.JOKE_KEY, joke);
        startActivity(myIntent);
    }




}
