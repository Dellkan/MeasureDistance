package com.LocateMeInc.locate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	
	private static Context context;
	private static CurLocation curloc;
	private Location loc1;
	private Location loc2;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	
	private void updateCurLocInfo() {
		Location loc = curloc.getLocation();
		// Set latitude, longitude, altitude
		((TextView)findViewById(R.id.curlocinfo_longitude_val)).setText(Double.toString(loc.getLatitude()));
		((TextView)findViewById(R.id.curlocinfo_latitude_val)).setText(Double.toString(loc.getLongitude()));
		((TextView)findViewById(R.id.curlocinfo_altitude_val)).setText(Double.toString(loc.getAltitude()));
		
		// Set accuracy, provider, last fetched timestamp
		((TextView)findViewById(R.id.curlocinfo_accuracy_val)).setText(Double.toString(loc.getAccuracy()));
		((TextView)findViewById(R.id.curlocinfo_provider_val)).setText(loc.getProvider());
		((TextView)findViewById(R.id.curlocinfo_updated_val)).setText(
			new SimpleDateFormat(
				//loc.getTime()
				"d/LLL kk:mm:ss",
				Locale.ROOT
			).format(loc.getTime())
		);
		
		// Satellite info
		Bundle extras = loc.getExtras();
		int sat = (extras != null && extras.containsKey("satellites")) ? extras.getInt("satellites") : 0;
		
		((TextView)findViewById(R.id.curlocinfo_satellites_val)).setText(Integer.toString(sat));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_main);
		
		// Set up a couple of static variables
		MainActivity.context = getApplicationContext();
		MainActivity.curloc = new CurLocation();
		
		// Update curlocinfo in a separate thread
	    Thread t = new Thread() {

	        @Override
	        public void run() {
	            try {
	                while (!isInterrupted()) {
	                    Thread.sleep(1000);
	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                            updateCurLocInfo();
	                        }
	                    });
	                }
	            } catch (InterruptedException e) {
	            }
	        }
	    };
	    t.start();
	    
	    // Set up drawer toggle button
	    this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	    this.mDrawerToggle = new ActionBarDrawerToggle(
	    	this, 
	    	this.mDrawerLayout,
	    	R.drawable.ic_drawer, 
	    	R.string.drawer_open, 
	    	R.string.drawer_close
	    ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        this.mDrawerLayout.setDrawerListener(this.mDrawerToggle);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
    	super.onPause();

    	try {
	     	FileOutputStream filehandler = context.openFileOutput("cachedLocations.dat", Context.MODE_PRIVATE);
	     	ObjectOutputStream filestream = new ObjectOutputStream(filehandler);
	     	filestream.writeObject(new SLocation(this.loc1));
	     	filestream.writeObject(new SLocation(this.loc2));
	     	filestream.close();
	     	
	     	Toast.makeText(getApplicationContext(), "Saved cached locations!", Toast.LENGTH_SHORT).show();
    	}
    	
    	catch(Exception e) {
    		//e.printStackTrace();
    		Toast.makeText(getApplicationContext(), "Unable to save cached locations", Toast.LENGTH_SHORT).show();
    	}
    }
	
    @Override
    public void onResume() {
    	super.onResume();
     
    	try {
	     	FileInputStream filehandler = context.openFileInput("cachedLocations.dat");
	     	ObjectInputStream filestream = new ObjectInputStream(filehandler);
	     	this.loc1 = ((SLocation)(filestream.readObject())).get();
	     	this.loc2 = ((SLocation)(filestream.readObject())).get();
	     	filestream.close();
	     	
	     	updateTexts(true, this.loc1);
	     	updateTexts(false, this.loc2);
	     	
	     	Toast.makeText(getApplicationContext(), "Cached locations loaded!", Toast.LENGTH_SHORT).show();
    	}
    	
    	catch(Exception e) {
    		//e.printStackTrace();
    		Toast.makeText(getApplicationContext(), "Unable to load cached locations", Toast.LENGTH_SHORT).show();
    	}
    }
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Tell the Location managers that we don't need location updates anymore.
		curloc.close();
	}
	
	private void updateTexts(Boolean ispos1, Location loc) {
		// Latitude, longitude, altitude
		((EditText)findViewById(ispos1 ? R.id.loc1lat_val : R.id.loc2lat_val)).setText(Double.toString(loc.getLatitude()));
		((EditText)findViewById(ispos1 ? R.id.loc1long_val : R.id.loc2long_val)).setText(Double.toString(loc.getLongitude()));
		((EditText)findViewById(ispos1 ? R.id.loc1alt_val : R.id.loc2alt_val)).setText(Double.toString(loc.getAltitude()));
		
		// Address
		try {
			if (Geocoder.isPresent()) {
				Geocoder geoCoder = new Geocoder(this);
		        StringBuilder builder = new StringBuilder();
		        List<Address> addresslist = geoCoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
		        int maxLines = addresslist.get(0).getMaxAddressLineIndex();
		        for (int i = 0; i < maxLines; i++) {
		        	String addressStr = addresslist.get(0).getAddressLine(i);
		        	builder.append(addressStr);
		        	builder.append(" ");
		        }
		
		        ((EditText)findViewById(ispos1 ? R.id.loc1city_val : R.id.loc2city_val)).setText(builder.toString());
	        }
	        
	        else {
				Toast.makeText(
					getApplicationContext(), 
					"Geocoder service is unavailable.", 
					Toast.LENGTH_SHORT
				).show();	        	
	        }
		}
		
		catch(Exception e) {
			Toast.makeText(getContext(), getResources().getString(R.string.toast_unable_to_determine_city), Toast.LENGTH_SHORT).show();
		}
		
		// Update distance measurement
		if (this.loc1 != null && this.loc2 != null) {
			// Update measurement
			String distance = Float.toString(this.loc1.distanceTo(this.loc2));
			((EditText)findViewById(R.id.distance)).setText(distance);
		}
	}
	
	public void fetchLocation(View v) {
		// Update fields
		Location loc = new Location(curloc.getLocation());
		if (v.getId() == R.id.loc1get) {
			this.loc1 = loc;
		}
		
		else if (v.getId() == R.id.loc2get) {
			this.loc2 = loc;
		}
		updateTexts(v.getId() == R.id.loc1get, loc);
		
		// Notify user of accuracy
		Toast.makeText(
			getApplicationContext(), 
			getResources().getString(R.string.toast_accuracy) + Float.toString(loc.getAccuracy()) + "m", 
			Toast.LENGTH_SHORT
		).show();
	}

	public static Context getContext() {
		return context;
	}
}
