package com.georgeshank.chirpful;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AuthActivity extends Activity {
	private CommonsHttpOAuthConsumer httpOauthConsumer;
    private OAuthProvider httpOauthprovider;
    public final static String consumerKey = Constants.CONSUMER_KEY;
    public final static String consumerSecret = Constants.CONSUMER_SECRET;
    private final String CALLBACKURL = Constants.CALLBACK_URL;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	
    	try {
    	    httpOauthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
    	    httpOauthprovider = new DefaultOAuthProvider("http://twitter.com/oauth/request_token",
    	                                            "http://twitter.com/oauth/access_token",
    	                                            "http://twitter.com/oauth/authorize");
    	    String authUrl = httpOauthprovider.retrieveRequestToken(httpOauthConsumer, CALLBACKURL);
    	    // Open the browser
    	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
    	} catch (Exception e) {
    	    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    	}
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    Log.d("OAUTH", "intent caught!");
	    Uri uri = intent.getData();

	    //Check if you got NewIntent event due to Twitter Call back only

	    if (uri != null && uri.toString().startsWith(CALLBACKURL)) {

	        String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);

	        try {
	            // this will populate token and token_secret in consumer

	            httpOauthprovider.retrieveAccessToken(httpOauthConsumer, verifier);
	            String userKey = httpOauthConsumer.getToken();
	            String userSecret = httpOauthConsumer.getTokenSecret();

	            // Save user_key and user_secret in user preferences and return

	            SharedPreferences settings = getBaseContext().getSharedPreferences("prefs", 0);
	            SharedPreferences.Editor editor = settings.edit();
	            editor.putString("user_key", userKey);
	            editor.putString("user_secret", userSecret);
	            editor.commit();
	            Log.d("OAuth", userKey + " " + userSecret);
	            this.pullCurrentFeed();
	        } catch(Exception e){
	        	Log.d("ERRORBRO", e.toString());
	        }
	    } else {
	        // Do something if the callback comes from elsewhere
	    }

	}
    
    public void pullCurrentFeed()
    {
    	HttpClient mClient = new DefaultHttpClient();
    	HttpGet get = new HttpGet("http://api.twitter.com/1/statuses/home_timeline.json");
    	HttpParams params = new BasicHttpParams();
    	HttpProtocolParams.setUseExpectContinue(params, false);
    	get.setParams(params);
    	// sign the request to authenticate
    	try{
	    	httpOauthConsumer.sign(get);
	    	Log.d("REQ", get.toString());
	    	String responsex = mClient.execute(get, new BasicResponseHandler());
	    	Toast.makeText(getApplicationContext(), responsex, Toast.LENGTH_LONG).show();
	    	Log.d("JSON", responsex);
	    	JSONArray array = new JSONArray(responsex);
    	}
    	catch(Exception e)
    	{
    		Toast.makeText(getApplicationContext(), "Error dude", Toast.LENGTH_SHORT).show();
    	}
    }
}
