package com.example.gcmpush;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MainActivity extends Activity {

	SharedPreferences registration_prefs;
	final String SENDER_ID = "564127530812";
	String gcm_regid;
	ProgressBar p;
	GoogleCloudMessaging gcm;
	List<NameValuePair> nameValuePairs;
	String http_response = "404";
	Boolean idGenerated;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		nameValuePairs= new ArrayList<NameValuePair>();
		gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
		p = (ProgressBar)findViewById(R.id.progressBar1);
		registration_prefs = getPreferences(MODE_PRIVATE);
		
		if(registration_prefs == null|| registration_prefs.getBoolean("PROPERTY_IS_REGISTERED", false)==false)   
			gcmRegister();
			
	}
	

	public void gcmRegister() {
		
		new AsyncTask<String, Void, Boolean>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				
                p.setIndeterminate(true);
                p.setVisibility(View.VISIBLE);
			}

			@Override
			protected Boolean doInBackground(String... params) {
				try {
					gcm_regid = gcm.register(SENDER_ID);
					Log.i("debug", "registration id is: "+ gcm_regid + "length is: " + gcm_regid.length());
					if(gcm_regid!=null)
					   {
					   registration_prefs = getPreferences(MODE_PRIVATE);
					   Editor e = registration_prefs.edit();
					   e.putBoolean("PROPERTY_IS_REGISTERED", true);
					   e.putString("PROPERTY_REG_ID", gcm_regid);
					   e.commit();
					   }
					   if(registration_prefs.getBoolean("PROPERTY_IS_REGISTERED", false)==true)
						   return true;					   
					}
				 catch (IOException e) {
					Log.i("debug","registration failed");
					e.printStackTrace();
				}
				return false;
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				p.setVisibility(View.INVISIBLE);
            
			    if(result == true)
				postToServer();
			    else
			    {
		     	AlertDialog.Builder b= new AlertDialog.Builder(MainActivity.this);	
			    b.setMessage("Error registering device to server.");
		    	b.setTitle("Cannot reach Google servers.");
			    b.setPositiveButton("OK", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				   dialog.dismiss();
				}
			});
			b.create().show();
			}
		}
		}.execute(null,null,null);
		
				
	}
	
	
	
	public void postToServer() {
		new AsyncTask<String, Void, Void>(){

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				
                p.setIndeterminate(true);
                p.setVisibility(View.VISIBLE);
			}
			
			@Override
			protected Void doInBackground(String... params) {
				String email = AccountManager.get(MainActivity.this).getAccountsByType("com.google")[0].name;
				Log.i("debug","email: "+email);
                String url = "http://pauldmps.hol.es/store.php";
				nameValuePairs.add(new BasicNameValuePair("mail_id",email));
				Log.i("debug","prefs_regid: "+registration_prefs.getString("PROPERTY_REG_ID", null));
				nameValuePairs.add(new BasicNameValuePair("reg_id",registration_prefs.getString("PROPERTY_REG_ID", null)));
				HttpClient http_client = new DefaultHttpClient();
				HttpPost http_post = new HttpPost(url);
				try{
					http_post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					http_response = http_client.execute(http_post,new BasicResponseHandler()); 
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				p.setVisibility(View.INVISIBLE);
				Log.i("debug","response: " +http_response);
			try{
				if (Integer.parseInt(http_response)!=0)
				{
					Log.i("debug","error code: "+ http_response);
					AlertDialog.Builder b= new AlertDialog.Builder(MainActivity.this);	
					b.setMessage("Cannot reach application server");
					b.setTitle("Unknown Error Occured");
					b.setPositiveButton("OK", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						   dialog.dismiss();
						}
					});
					b.create().show();
				}
			}
				catch(NumberFormatException e)
				{
					Log.i("PHP Error",http_response);
					e.printStackTrace();
				}
			} 
			
			
	}.execute(null,null,null);
	

  }


}

