package com.LocateMeInc.locate;

import java.io.Serializable;

import android.location.Location;
import android.os.Parcel;

public class SLocation implements Serializable {
	private static final long serialVersionUID = 0L;
	
	private byte[] serialized;
	
	public SLocation() {}
	public SLocation(Location loc) {
		Parcel pLoc = Parcel.obtain();
		loc.writeToParcel(pLoc, 0);
		this.serialized = pLoc.marshall();
		pLoc.recycle(); // not sure if needed or a good idea
	}
	public SLocation(byte[] bytes) {
		this.serialized = bytes;
	}
	
	public Location get() {
		Parcel pLoc = Parcel.obtain();
		pLoc.unmarshall(this.serialized, 0, this.serialized.length);
		pLoc.setDataPosition(0);
		return Location.CREATOR.createFromParcel(pLoc);
	}
}