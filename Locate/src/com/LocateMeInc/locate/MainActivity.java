package com.LocateMeInc.locate;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends FragmentActivity {
	
	private static Context context;
	private FragmentMain fragmentmain;
	private FragmentPagerAdapter fragmentpager;
	private ViewPager viewpager;
	private List<Fragment> fragments = new ArrayList<Fragment>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_main);
		
		// Set up a couple of static variables
		MainActivity.context = getApplicationContext();
        
        // Create swiper stuff
        this.fragmentmain = new FragmentMain();
        fragments.add(0, this.fragmentmain);
        fragments.add(1, new FragmentMap());
        FragmentManager fm = getSupportFragmentManager(); 
        fragmentpager = new FragmentPagerAdapter(fm) {
            @Override
            public int getCount() {
              return 2;
            }
            @Override
            public Fragment getItem(final int position) {
              return fragments.get(position);
            }
            @Override
            public CharSequence getPageTitle(final int position) {
            	switch (position) {
                case 0:
                	return "Set locations";
                case 1:
                	return "Map";
                default:
                	return null;
            	}
            } 
        };
        viewpager = (ViewPager) findViewById(R.id.pager);
        viewpager.setAdapter(fragmentpager);
        viewpager.setCurrentItem(0);
        
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.fragmentmain.getToggle() != null) {
        	this.fragmentmain.getToggle().onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
    	if (this.fragmentmain.getToggle() != null) {
	        if (this.fragmentmain.getToggle().onOptionsItemSelected(item)) {
	          return true;
	        }
    	}

        return super.onOptionsItemSelected(item);
    }
    
	public void fetchLocation(View v) {
		// Update fields
		this.fragmentmain.fetchLocation(v);
	}
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Tell the Location managers that we don't need location updates anymore.
		fragmentmain.close();
	}

	public static Context getContext() {
		return context;
	}
}
