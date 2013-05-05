package org.tomhume.shoppingreminder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.widget.Toast;

public class StartupActivity extends Activity {
	
	private static final String TAG = "StartupActivity";
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);

		final Button button_getstarted = (Button) findViewById(R.id.button_start); 
		button_getstarted.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {                 
	        	triggerLookup();
	        }
	    });
		
		triggerLookup();

	}
	
	private void triggerLookup() {
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        startActivityForResult(intent, 0);
	}


	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    if (requestCode == 0) {
	        if (resultCode == RESULT_OK) {
	            String contents = intent.getStringExtra("SCAN_RESULT");
	            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	            Log.i(TAG, "format="+format+",contents="+contents);
	            
	        	new ResolverTask().execute(new EANDataResolver(contents));

	            // Handle successful scan
	        	
	        } else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel
	        }
	        triggerLookup();
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
	
	private class ResolverTask extends AsyncTask<EANDataResolver, Integer, Long> {
		
		protected Long doInBackground(EANDataResolver... data) {
        	try {
        		EANDataResolver edr = data[0];
	        	edr.resolve();
	        	Log.d(TAG, "Product="+edr.getItemName());
	        	
	        	if ((edr.getItemName()!=null) && (!edr.getItemName().equals(""))) {
	        		new AddSpreadsheetTask().execute(new ShoppingItem(edr.getItemCode(),edr.getItemName()));
	        	}
        	} catch (IOException e) {
        		e.printStackTrace();
        	} catch (URISyntaxException e) {
				e.printStackTrace();
			}
        	return 0L;
		}
	}
	
	private class AddSpreadsheetTask extends AsyncTask<ShoppingItem, Integer, Long> {
	     protected Long doInBackground(ShoppingItem... items) {

	        	AccountManager amgr = AccountManager.get(getApplicationContext());
	        	Account gmail = getGMailAccount(amgr.getAccounts());
	        	if (gmail!=null) {
	        		AccountManagerFuture<Bundle> amf = amgr.getAuthToken(gmail, "wise", null, null, null, null);
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
		        	    row.getCustomElements().setValueLocal("code", items[0].getItemCode());
		        	    row.getCustomElements().setValueLocal("name", items[0].getItemName());
		        	    // Send the new row to the API for insertion.
		        	    row = ss.insert(listFeedUrl, row);
		        	    
		        	    Log.d(TAG, "wrote row");
		        	    
			        	try {
			                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			                r.play();
			            } catch (Exception e) {}
			        	
		        		
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

	 }
	
}
