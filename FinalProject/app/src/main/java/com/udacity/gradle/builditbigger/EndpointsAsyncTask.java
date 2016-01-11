package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.steelgirderdev.builditbigger.jokebackend.myApi.MyApi;

import java.io.IOException;

/**
 * Inner class that performs the async task to get the Joke from the GCE
 * Source/Tutorial: https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
 * Source 2: http://marksunghunpark.blogspot.com/2015/05/how-to-test-asynctask-in-android.html
 */
public class EndpointsAsyncTask extends AsyncTask<Pair<Context, View>, Void, String> {
    private static final String LOGTAG = "EndpointsAsyncTask";
    private MyApi myApiService = null;

    private EndpointsAsyncTaskListener mListener = null;
    private Exception mError = null;

    @Override
    protected String doInBackground(Pair<Context, View>... params) {
        //Log.v(LOGTAG,"doInBackground...");
        String content = null;

        if(myApiService == null) {  // Only do this once
            //Log.v(LOGTAG,"myApiService == null");
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                    .setApplicationName("EndpointsAsyncTask")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end options for devappserver

            myApiService = builder.build();
        }

        try {
            //Log.v(LOGTAG,"Calling the API");
            content = myApiService.sayHi("tellMeAJoke").execute().getData();
            //Log.v(LOGTAG,"API returned:"+content);
        } catch (IOException e) {
            mError = e;
            Log.e(LOGTAG,"API error:"+e.getLocalizedMessage());
        }
        
        if (this.mListener != null) {
            this.mListener.onComplete(content, mError);
        }

        return content;
    }

    public EndpointsAsyncTask setListener(EndpointsAsyncTaskListener listener) {
        this.mListener = listener;
        Log.e(LOGTAG,"setListener"+listener);
        return this;
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.v(LOGTAG,"onPostExecute this.mListener="+this.mListener);
        if (this.mListener != null) {
            this.mListener.onComplete(s, mError);
        }
    }

    @Override
    protected void onCancelled() {
        //Log.v(LOGTAG,"onCancelled this.mListener="+this.mListener);
        if (this.mListener != null) {
            mError = new InterruptedException("AsyncTask cancelled");
            this.mListener.onComplete(null, mError);
        }
    }


    public static interface EndpointsAsyncTaskListener {
        public void onComplete(String jsonString, Exception e);
    }
}
