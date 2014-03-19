package course.labs.contentproviderlab;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;
import course.labs.contentproviderlab.provider.PlaceBadgesContract;



public class PlaceViewActivity extends ListActivity implements
		LocationListener, LoaderCallbacks<Cursor> {
	private static final long FIVE_MINS = 5 * 60 * 1000;

	private static String TAG = "Lab-ContentProvider";

	// The last valid location reading
	private Location mLastLocationReading;

	// The ListView's adapter
	// private PlaceViewAdapter mAdapter;
	private PlaceViewAdapter mCursorAdapter;

	// default minimum time between new location readings
	private long mMinTime = 5000;

	// default minimum distance between old and new readings.
	private float mMinDistance = 1000.0f;

	// Reference to the LocationManager
	private LocationManager mLocationManager;

	// A fake location provider used for testing
	private MockLocationProvider mMockLocationProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // TODO - Set up the app's user interface
        // This class is a ListActivity, so it has its own ListView

		ListView listView = this.getListView();
		listView.setFooterDividersEnabled(true);

		mCursorAdapter = new PlaceViewAdapter(getApplicationContext(), null, 0);
		
        // TODO - add a footerView to the ListView
        // You can use footer_view.xml to define the footer
		
		View footerView = null;
		LayoutInflater inflater = this.getLayoutInflater();
		footerView = (View) inflater.inflate( R.layout.footer_view, listView, false);
		
		listView.addFooterView(footerView);
		listView.setAdapter(mCursorAdapter); //if you add this before adding the footer it does not show
		
        // TODO - When the footerView's onClick() method is called, it must issue the
        // following log call
        // log("Entered footerView.OnClickListener.onClick()");
		
		footerView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

		        log("Entered footerView.OnClickListener.onClick()");   
		        
		        // footerView must respond to user clicks.
		        // Must handle 3 cases:
		        
		        // 3) There is no current location - response is up to you. The best
		        // solution is to disable the footerView until you have a location.
		        // Issue the following log call:
		        // log("Location data is not available");
		        
		        if (mLastLocationReading == null) {
		        	
		        	log("Location data is not available");
		        	
		        	getListView().removeFooterView(v);
		        	return;
		        }

		        // 2) The current location has been seen before - issue Toast message.
		        // Issue the following log call:
		        // log("You already have this location badge");
		        
		        else if (mCursorAdapter.intersects(mLastLocationReading)) {
		        	
		        	log("You already have this location badge");

		        }
		        
		        // 1) The current location is new - download new Place Badge. Issue the
		        // following log call:
		        // log("Starting Place Download");
		        
		        else {
		        	
		        	log("Starting Place Download");
		        	
		        	PlaceDownloaderTask download = new PlaceDownloaderTask(PlaceViewActivity.this);
		        	download.execute(mLastLocationReading);
		        }
			}
		});  
		
		// TODO - Create and set empty PlaceViewAdapter
        // ListView's adapter should be a PlaceViewAdapter called mCursorAdapter
		Cursor cursor = this.getContentResolver().query(PlaceBadgesContract.CONTENT_URI, null, null, null, null);

		mCursorAdapter = new PlaceViewAdapter(this, cursor, 0);
		
		// TODO - Initialize a CursorLoader
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onPause() {

		mMockLocationProvider.shutdown();

		// TODO - unregister for location updates
		mLocationManager.removeUpdates(this);
		
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		
		log("Entered onResume()");

		super.onResume();

		mMockLocationProvider = new MockLocationProvider(
				LocationManager.NETWORK_PROVIDER, this);

        // TODO - Check NETWORK_PROVIDER for an existing location reading.
        // Only keep this last reading if it is fresh - less than 5 minutes old.
		
		if (null == (mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE)))
			finish();
		
		Location locationReading = bestLastKnownLocation(mMinTime);
		
		if (locationReading != null & locationReading.getTime() < FIVE_MINS) {
			mLastLocationReading = locationReading;
		} else {
			mLastLocationReading = null;
		}
		
        // TODO - register to receive location updates from NETWORK_PROVIDER
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, mMinTime, mMinDistance, this);
	}

	public void addNewPlace(PlaceRecord place) {

		log("Entered addNewPlace()");

		mCursorAdapter.add(place);

	}

	@Override
	public void onLocationChanged(Location currentLocation) {

        // TODO - Handle location updates
        // Cases to consider
        // 1) If there is no last location, keep the current location.
        // 2) If the current location is older than the last location, ignore
        // the current location
        // 3) If the current location is newer than the last locations, keep the
        // current location.

		if (mLastLocationReading == null ||
				currentLocation.getTime() < mLastLocationReading.getTime()) 
		{
			mLastLocationReading = currentLocation;
		} 
	}

	@Override
	public void onProviderDisabled(String provider) {
		// not implemented
	}

	@Override
	public void onProviderEnabled(String provider) {
		// not implemented
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// not implemented
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		log("Entered onCreateLoader()");
	
		// TODO - Create a new CursorLoader and return it
		CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), PlaceBadgesContract.CONTENT_URI, 
				null , null, null, null);
	
	    return cursorLoader;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> newLoader, Cursor newCursor) {

		// TODO - Swap in the newCursor
		if(mCursorAdapter!=null && newCursor!=null)
			mCursorAdapter.swapCursor(newCursor); 
		else
			Log.v(TAG,"OnLoadFinished: mAdapter is null");	
    }

	@Override
	public void onLoaderReset(Loader<Cursor> newLoader) {

		// TODO - Swap in a null Cursor
		if(mCursorAdapter!=null)
			mCursorAdapter.swapCursor(null);
		else
			Log.v(TAG,"OnLoadFinished: mAdapter is null");	
    }

	private long age(Location location) {
		return System.currentTimeMillis() - location.getTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.print_badges:
			ArrayList<PlaceRecord> currData = mCursorAdapter.getList();
			for (int i = 0; i < currData.size(); i++) {
				log(currData.get(i).toString());
			}
			return true;
		case R.id.delete_badges:
			mCursorAdapter.removeAllViews();
			return true;
		case R.id.place_one:
			mMockLocationProvider.pushLocation(37.422, -122.084);
			return true;
		case R.id.place_invalid:
			mMockLocationProvider.pushLocation(0, 0);
			return true;
		case R.id.place_two:
			mMockLocationProvider.pushLocation(38.996667, -76.9275);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static void log(String msg) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, msg);
	}
	
	private Location bestLastKnownLocation(long minTime) {
		
		log("Entered bestLastKnownLocation()");

		Location bestResult = null;
		long bestTime = Long.MIN_VALUE;

		String provider = LocationManager.NETWORK_PROVIDER; 
		Location location = mLocationManager.getLastKnownLocation(provider);

		if (location != null) {

			long time = location.getTime();

			bestResult = location;
			bestTime = time;
		}

		// Return best reading or null
		if (bestTime < minTime) {
			return null;
		} else {
			return bestResult;
		}
	}
}
