package com.georgeshank.chirpful;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;



public class ChirpfulActivity extends Activity implements OnClickListener{
	
	private static final String TAG = "Chirpful";
    /** Name to store the users access token */
    private static final String PREF_ACCESS_TOKEN = "accessToken";
    /** Name to store the users access token secret */
    private static final String PREF_ACCESS_TOKEN_SECRET = "accessTokenSecret";
    /** Consumer Key generated when you registered your app at https://dev.twitter.com/apps/ */
    private static final String CONSUMER_KEY = Constants.CONSUMER_KEY;
    /** Consumer Secret generated when you registered your app at https://dev.twitter.com/apps/  */
    private static final String CONSUMER_SECRET = Constants.CONSUMER_SECRET; // XXX Encode in your app
    /** The url that Twitter will redirect to after a user log's in - this will be picked up by your app manifest and redirected into this activity */
    private static final String CALLBACK_URL = Constants.CALLBACK_URL;
    /** Preferences to store a logged in users credentials */
    private SharedPreferences mPrefs;
    /** Twitter4j object */
    private Twitter mTwitter;
    /** The request token signifies the unique ID of the request you are sending to twitter  */
    private RequestToken mReqToken;

    private Button mLoginButton;
    private Button mTweetButton;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Window window = getWindow();
        // Eliminates color banding
        window.setFormat(PixelFormat.RGBA_8888);
        
        //Setting up the "sign in" button
        Button signInButton = (Button)findViewById(R.id.signInButton);
        signInButton.setOnClickListener(this);
        
        // Create a new shared preference object to remember if the user has
        // already given us permission
        mPrefs = getSharedPreferences("twitterPrefs", MODE_PRIVATE);
        Log.i(TAG, "Got Preferences");
       
        // Load the twitter4j helper
        mTwitter = new TwitterFactory().getInstance();
        Log.i(TAG, "Got Twitter4j");
       
        // Tell twitter4j that we want to use it with our app
        mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        Log.i(TAG, "Inflated Twitter4j");
    }

	@Override
	public void onClick(View v) {
		
		Log.i(TAG, "Login Pressed");
        if (mPrefs.contains(PREF_ACCESS_TOKEN)) {
                Log.i(TAG, "Repeat User");
                loginAuthorisedUser();
        } else {
                Log.i(TAG, "New User");
                loginNewUser();
        }
		
	}
	
	private void loginNewUser() {
        try {
                Log.i(TAG, "Request App Authentication");
                mReqToken = mTwitter.getOAuthRequestToken(CALLBACK_URL);

                Log.i(TAG, "Starting Webview to login to twitter");
               
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mReqToken.getAuthenticationURL())));
                WebView twitterSite = new WebView(this);
                twitterSite.loadUrl(mReqToken.getAuthenticationURL());
                setContentView(twitterSite);
                
        } catch (TwitterException e) {
                Toast.makeText(this, "Twitter Login error, try again later", Toast.LENGTH_SHORT).show();
        }
	}
	
	private void loginAuthorisedUser() {
        String token = mPrefs.getString(PREF_ACCESS_TOKEN, null);
        String secret = mPrefs.getString(PREF_ACCESS_TOKEN_SECRET, null);

        // Create the twitter access token from the credentials we got previously
        AccessToken at = new AccessToken(token, secret);

        mTwitter.setOAuthAccessToken(at);
       
        Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show();
       
        enableTweetButton();
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            Log.i(TAG, "New Intent Arrived");
            dealWithTwitterResponse(intent);
    }

//    @Override
//    protected void onResume() {
//            super.onResume();
//            Log.i(TAG, "Arrived at onResume");
//    }
	
    private void dealWithTwitterResponse(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(CALLBACK_URL)) { // If the user has just logged in
                String oauthVerifier = uri.getQueryParameter("oauth_verifier");

                authoriseNewUser(oauthVerifier);
        }
    }
    
    private void authoriseNewUser(String oauthVerifier) {
        try {
                AccessToken at = mTwitter.getOAuthAccessToken(mReqToken, oauthVerifier);
                mTwitter.setOAuthAccessToken(at);

                saveAccessToken(at);

                // Set the content view back after we changed to a webview
                setContentView(R.layout.main);
               
                //enableTweetButton();
        } catch (TwitterException e) {
                Toast.makeText(this, "Twitter auth error x01, try again later", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveAccessToken(AccessToken at) {
        String token = at.getToken();
        String secret = at.getTokenSecret();
        Editor editor = mPrefs.edit();
        editor.putString(PREF_ACCESS_TOKEN, token);
        editor.putString(PREF_ACCESS_TOKEN_SECRET, secret);
        editor.commit();
    }
    
    public void enableTweetButton()
    {
//	    	try {
//	            mTwitter.updateStatus("Chirp Chirp, testing @Twitter4j");
//	
//	            Toast.makeText(this, "Tweet Successful!", Toast.LENGTH_SHORT).show();
//	    } catch (TwitterException e) {
//	            Toast.makeText(this, "Tweet error, try again later", Toast.LENGTH_SHORT).show();
//	    }
    }
}