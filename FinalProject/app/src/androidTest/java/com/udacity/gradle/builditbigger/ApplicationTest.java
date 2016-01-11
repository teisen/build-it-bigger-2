package com.udacity.gradle.builditbigger;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * How to test AsyncTask in Android
 * Source: http://marksunghunpark.blogspot.com/2015/05/how-to-test-asynctask-in-android.html
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    String mReturnString = null;
    Exception mError = null;
    CountDownLatch signal = null;

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        signal = new CountDownLatch(1);
    }

    @Override
    protected void tearDown() throws Exception {
        signal.countDown();
    }

    /**
     * Tests if the Async task returns a String
     * @throws InterruptedException
     */
    public void testGetJokeAsync() throws InterruptedException {

        EndpointsAsyncTask task = new EndpointsAsyncTask();
        task.setListener(new EndpointsAsyncTask.EndpointsAsyncTaskListener() {
            @Override
            public void onComplete(String str, Exception e) {
                mReturnString = str;
                mError = e;
                signal.countDown();
            }
        }).execute(new Pair<Context, View>(null, null));
        signal.await(5, TimeUnit.SECONDS);

        assertNull(mError);
        assertFalse(TextUtils.isEmpty(mReturnString));

    }

}