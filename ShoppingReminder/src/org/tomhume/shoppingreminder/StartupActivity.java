package org.tomhume.shoppingreminder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.Worksheet;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import android.os.AsyncTask;
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
	private static final String PREF_ACCOUNT_NAME = "accountName";
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);

		final Button button_getstarted = (Button) findViewById(R.id.button_start); 
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
	        	
	        	new AddSpreadsheetTask().execute(thisActivity);

	        }
	    });
		
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

	private Account getGMailAccount(Account[] accounts) {
		for (Account a: accounts) {
			if (a.type.equals("com.google") && a.name.endsWith("gmail.com")) return a;
		}
		return null;
	}
	
	private SpreadsheetEntry getShoppingList(SpreadsheetService ss) throws IOException, ServiceException {
	    URL metafeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
	    SpreadsheetFeed feed = ss.getFeed(metafeedUrl, SpreadsheetFeed.class);

	    List<SpreadsheetEntry> spreadsheets = feed.getEntries();
	    for (SpreadsheetEntry sheet: spreadsheets) {
	    	if (sheet.getTitle().getPlainText().equalsIgnoreCase("shopping list")) return sheet;
	    }
	    return null;
	}
	
	private class AddSpreadsheetTask extends AsyncTask<Activity, Integer, Long> {
	     protected Long doInBackground(Activity... acts) {

	        	AccountManager amgr = AccountManager.get(getApplicationContext());
	        	Account gmail = getGMailAccount(amgr.getAccounts());
	        	if (gmail!=null) {
	        		AccountManagerFuture<Bundle> amf = amgr.getAuthToken(gmail, "wise", null, acts[0], null, null);
					try {
						
						Bundle authTokenBundle = amf.getResult();
						String authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
		        		SpreadsheetService ss = new SpreadsheetService("Spreadsheet");
		        	    ss.setProtocolVersion(SpreadsheetService.Versions.V3);
		        	    ss.setHeader("Authorization", "GoogleLogin auth=" + authToken);

		        	    SpreadsheetEntry theSheet = getShoppingList(ss);
		        	    WorksheetEntry ws = theSheet.getWorksheets().get(0);
		        	    

		        	    
		        	    URL listFeedUrl = ws.getListFeedUrl();
		        	    ListFeed listFeed = ss.getFeed(listFeedUrl, ListFeed.class);

		        	    // Create a local representation of the new row.
		        	    ListEntry row = new ListEntry();
		        	    row.getCustomElements().setValueLocal("item", "Joe");
		        	    // Send the new row to the API for insertion.
		        	    row = ss.insert(listFeedUrl, row);
		        	    
		        	    Log.d(TAG, "wrote row");
		        		
					} catch (OperationCanceledException e) {
						e.printStackTrace();
					} catch (AuthenticatorException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ServiceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	    	 
	         return 0L;
	     }

	     protected void onProgressUpdate(Integer... progress) {
	    	 Log.d(TAG, "onProgressUpdate " + progress[0]);
	     }

	     protected void onPostExecute(Long result) {
	    	 Log.d(TAG, "onPostExecute " + result);
	     }
	 }
	
}
