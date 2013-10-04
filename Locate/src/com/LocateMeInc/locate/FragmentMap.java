package com.LocateMeInc.locate;

import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class FragmentMap extends Fragment {
	private HashMap<String, Marker> mMarkers = new HashMap<String, Marker>();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.map, null);
		return view;
	}
	
	public void updateMap() {
		try {
			final MainActivity main = (MainActivity)this.getActivity();
			FragmentManager fragmentManager = main.getSupportFragmentManager();  
			SupportMapFragment mapFragment = (SupportMapFragment)fragmentManager.findFragmentById(R.id.map);  
			GoogleMap map = mapFragment.getMap();
			
			Location loc1 = MainActivity.fetchMainFragment().getLoc1();
			Location loc2 = MainActivity.fetchMainFragment().getLoc2();
			Location curloc = MainActivity.fetchMainFragment().getCurLoc();
			
			Builder builder = new LatLngBounds.Builder();
			
			 
		    if (loc1 != null) {
		    	LatLng pos = new LatLng(loc1.getLatitude(), loc1.getLongitude());
		    	builder.include(pos);
		    	if (mMarkers.get("loc1") == null) {
		    		mMarkers.put("loc1", map.addMarker(new MarkerOptions().position(pos).title(getResources().getString(R.string.title_first_location))));
		    	}
		    	
		    	else {
		    		((Marker)mMarkers.get("loc1")).setPosition(pos);
		    	}
		    }
		    if (loc2 != null) {
		    	LatLng pos = new LatLng(loc2.getLatitude(), loc2.getLongitude());
		    	builder.include(pos);
		    	if (mMarkers.get("loc2") == null) {
		    		mMarkers.put("loc2", map.addMarker(new MarkerOptions().position(pos).title(getResources().getString(R.string.title_second_location))));
		    	}
		    	
		    	else {
		    		((Marker)mMarkers.get("loc2")).setPosition(pos);
		    	}
		    }
		    if (curloc != null) {
		    	LatLng pos = new LatLng(curloc.getLatitude(), curloc.getLongitude());
		    	builder.include(pos);
		    	if (mMarkers.get("curloc") == null) {
		    		mMarkers.put("curloc", map.addMarker(new MarkerOptions().position(pos).title("Current position")));
		    	}
		    	
		    	else {
		    		((Marker)mMarkers.get("curloc")).setPosition(pos);
		    	}
		    }
		    LatLngBounds boundsLatLng = builder.build();
	
		    map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsLatLng,50));
		    
			Toast.makeText(
				MainActivity.getContext(), 
				"Map updated!", 
				Toast.LENGTH_SHORT
			).show();
		}
		
		catch(Exception e) {
			Toast.makeText(
				MainActivity.getContext(), 
				"Couldn't update map", 
				Toast.LENGTH_SHORT
			).show();
			return;
		}
	}
}