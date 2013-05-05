package org.tomhume.shoppingreminder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

import com.google.gson.Gson;

public class EANDataResolver {

	private static final String TAG = "EANDataResolver";
	private static final String EANDATA_KEY = "7AD4C1255186A6B4";
	
	private String itemCode;
	private String itemName;
	
	public EANDataResolver(String i) {
		this.itemCode = i;
	}

	public void resolve() throws URISyntaxException, ClientProtocolException, IOException {
		Gson g = new Gson();
		
		HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 50000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 50000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);           
        HttpClient httpClient = new DefaultHttpClient(httpParameters);          

		URI url = new URI("http://eandata.com/feed.php?keycode="+ EANDATA_KEY + "&mode=json&find=" + itemCode);
		Log.d(TAG, "Hitting " + url.toString());
		HttpResponse response = httpClient.execute(new HttpGet(url));
        HttpEntity entity = response.getEntity();
        Reader reader = new InputStreamReader(entity.getContent());
        EANData data = g.fromJson(reader, EANData.class);
        if ((data!=null) && (data.product !=null)) this.itemName = data.product.product;
        else Log.d(TAG, "couldn't find any product in data returned");
	}
	
	class EANData {
		public EANDataStatus status;
		public EANDataProduct product;
		public EANDataCompany company;
	}
	
	class EANDataStatus {
		public String version;
		public String code;
		public String message;
		public String find;
	}

	class EANDataProduct {
		public String modified;
		public String ean13;
		public String upca;
		public String upce;
		public String isbn10;
		public String ASIN;
		public String SKU;
		public String PriceNew;
		public String PriceUsed;
		public String PriceData;
		public String product;
		public String description;
		public String category_no;
		public String category_text;
		public String url;
		public String has_long_desc;
		public String image;
		public String barcode;
	}
	
	class EANDataCompany {
		public String name;
		public String logo;
		public String url;
		public String address;
		public String phone;
		public String locked;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
}
