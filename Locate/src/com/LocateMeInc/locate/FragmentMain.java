package com.LocateMeInc.locate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentMain extends Fragment {
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Location loc1;
	private Location loc2;
	private static CurLocation curloc;
	private View view;
	
	private void updateCurLocInfo() {
		View v = this.getView();
		if (v == null) {
			if (this.view == null) { return; }
			v = this.view;
		}
		Location loc = curloc.getLocation();
		// Set latitude, longitude, altitude
		((TextView)v.findViewById(R.id.curlocinfo_longitude_val)).setText(Double.toString(loc.getLatitude()));
		((TextView)v.findViewById(R.id.curlocinfo_latitude_val)).setText(Double.toString(loc.getLongitude()));
		((TextView)v.findViewById(R.id.curlocinfo_altitude_val)).setText(Double.toString(loc.getAltitude()));
		
		// Set accuracy, provider, last fetched timestamp
		((TextView)v.findViewById(R.id.curlocinfo_accuracy_val)).setText(Double.toString(loc.getAccuracy()));
		((TextView)v.findViewById(R.id.curlocinfo_provider_val)).setText(loc.getProvider());
		((TextView)v.findViewById(R.id.curlocinfo_updated_val)).setText(
			new SimpleDateFormat(
				//loc.getTime()
				"d/LLL kk:mm:ss",
				Locale.ROOT
			).format(loc.getTime())
		);
		
		// Satellite info
		Bundle extras = loc.getExtras();
		int sat = (extras != null && extras.containsKey("satellites")) ? extras.getInt("satellites") : 0;
		
		((TextView)v.findViewById(R.id.curlocinfo_satellites_val)).setText(Integer.toString(sat));
	}
	
	private void updateTexts(Boolean ispos1, Location loc) {
		View v = this.getView();
		if (v == null) {
			if (this.view == null) { return; }
			v = this.view;
		}
		// Latitude, longitude, altitude
		((EditText)v.findViewById(ispos1 ? R.id.loc1lat_val : R.id.loc2lat_val)).setText(Double.toString(loc.getLatitude()));
		((EditText)v.findViewById(ispos1 ? R.id.loc1long_val : R.id.loc2long_val)).setText(Double.toString(loc.getLongitude()));
		((EditText)v.findViewById(ispos1 ? R.id.loc1alt_val : R.id.loc2alt_val)).setText(Double.toString(loc.getAltitude()));
		
		// Address
		try {
			if (Geocoder.isPresent()) {
				Geocoder geoCoder = new Geocoder(this.getActivity());
		        StringBuilder builder = new StringBuilder();
		        List<Address> addresslist = geoCoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
		        int maxLines = addresslist.get(0).getMaxAddressLineIndex();
		        for (int i = 0; i < maxLines; i++) {
		        	String addressStr = addresslist.get(0).getAddressLine(i);
		        	builder.append(addressStr);
		        	builder.append(" ");
		        }
		
		        ((EditText)v.findViewById(ispos1 ? R.id.loc1city_val : R.id.loc2city_val)).setText(builder.toString());
	        }
	        
	        else {
				Toast.makeText(
					this.getActivity().getApplicationContext(), 
					"Geocoder service is unavailable.", 
					Toast.LENGTH_SHORT
				).show();	        	
	        }
		}
		
		catch(Exception e) {
			Toast.makeText(
				this.getActivity().getApplicationContext(), 
				getResources().getString(R.string.toast_unable_to_determine_city), 
				Toast.LENGTH_SHORT
			).show();
		}
		
		// Update distance measurement
		if (this.loc1 != null && this.loc2 != null) {
			// Update measurement
			String distance = Float.toString(this.loc1.distanceTo(this.loc2));
			((EditText)v.findViewById(R.id.distance)).setText(distance);
		}
		
    	try {
	     	FileOutputStream filehandler = this.getActivity().openFileOutput("cachedLocations.dat", Context.MODE_PRIVATE);
	     	ObjectOutputStream filestream = new ObjectOutputStream(filehandler);
	     	filestream.writeObject(new SLocation(this.loc1));
	     	filestream.writeObject(new SLocation(this.loc2));
	     	filestream.close();
	     	
	     	Toast.makeText(this.getActivity(), "Saved cached locations!", Toast.LENGTH_SHORT).show();
    	}
    	
    	catch(Exception e) {
    		//e.printStackTrace();
    		Toast.makeText(this.getActivity(), "Unable to save cached locations", Toast.LENGTH_SHORT).show();
    	}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final Activity main = this.getActivity();
		View view = inflater.inflate(R.layout.main, container, false);
		
		FragmentMain.curloc = new CurLocation();
		
		// Update curlocinfo in a separate thread
	    Thread t = new Thread() {

	        @Override
	        public void run() {
	            try {
	                while (!isInterrupted()) {
	                    Thread.sleep(1000);
	                    main.runOnUiThread(new Runnable() {
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
	    this.mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
	    this.mDrawerToggle = new ActionBarDrawerToggle(main, this.mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                main.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                main.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        this.getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        this.mDrawerToggle.setDrawerIndicatorEnabled(true);
        this.mDrawerToggle.syncState();

        // Set the drawer toggle as the DrawerListener
        this.mDrawerLayout.setDrawerListener(this.mDrawerToggle);
        
        // Update fields
        this.view = view;
     	updateTexts(true, this.loc1);
     	updateTexts(false, this.loc2);

		return view;
	}
	
	@Override
	public void onAttach(Activity act) {
		super.onAttach(act);
		this.open();
	}
	
	public ActionBarDrawerToggle getToggle() {
		return this.mDrawerToggle;
	}
	
	public void close() {
		FragmentMain.curloc.close();
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
			this.getActivity(), 
			getResources().getString(R.string.toast_accuracy) + Float.toString(loc.getAccuracy()) + "m", 
			Toast.LENGTH_SHORT
		).show();
	}
	
	public void open() {
		try {
	     	FileInputStream filehandler = this.getActivity().openFileInput("cachedLocations.dat");
	     	ObjectInputStream filestream = new ObjectInputStream(filehandler);
	     	this.loc1 = ((SLocation)(filestream.readObject())).get();
	     	this.loc2 = ((SLocation)(filestream.readObject())).get();
	     	filestream.close();
	     	
	     	Toast.makeText(this.getActivity(), "Cached locations loaded!", Toast.LENGTH_SHORT).show();
    	}
    	
    	catch(Exception e) {
    		//e.printStackTrace();
    		Toast.makeText(this.getActivity(), "Unable to load cached locations", Toast.LENGTH_SHORT).show();
    	}
	}
}