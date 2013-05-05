package org.tomhume.shoppingreminder;

import java.io.IOException;

import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class StartupActivity extends Activity {
	
	private static final String TAG = "StartupActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);

		final Button button_getstarted = (Button) findViewById(R.id.button_start); 
		Log.d(TAG, "Button="+button_getstarted);
		
		button_getstarted.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {                 
	        	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
	            startActivityForResult(intent, 0);
	        }
	    });

		final Activity thisActivity = this;
		
		final Button button_append = (Button) findViewById(R.id.button_append);
		button_append.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {                 
	        	
	        	AccountManager amgr = AccountManager.get(getApplicationContext());
	        	Account gmail = getGMailAccount(amgr.getAccounts());
	        	if (gmail!=null) {
	        		AccountManagerFuture<Bundle> amf = amgr.getAuthToken(gmail, "wise", null, thisActivity, null, null);
					try {
						Bundle authTokenBundle = amf.getResult();
						String authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
		        		Log.d(TAG, "authToken="+authToken);
					} catch (OperationCanceledException e) {
						e.printStackTrace();
					} catch (AuthenticatorException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	        }
	    });
		
	}
	
	private Account getGMailAccount(Account[] accounts) {
		for (Account a: accounts) {
			if (a.type.equals("com.google") && a.name.endsWith("gmail.com")) return a;
		}
		return null;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    if (requestCode == 0) {
	        if (resultCode == RESULT_OK) {
	            String contents = intent.getStringExtra("SCAN_RESULT");
	            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	            Log.i(TAG, "format="+format+",contents="+contents);
	            // Handle successful scan
	        } else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel
	        }
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}

}
