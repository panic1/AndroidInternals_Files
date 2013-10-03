package com.marakana.android.fibonaccicommon;

import android.os.Parcel;
import android.os.Parcelable;

public class FibonacciRequest implements Parcelable {

	public static enum Type {
		RECURSIVE_JAVA, ITERATIVE_JAVA, RECURSIVE_NATIVE, ITERATIVE_NATIVE;
	}
	
	public static final Parcelable.Creator<FibonacciRequest> CREATOR
		= new Parcelable.Creator<FibonacciRequest>() {

			@Override
			public FibonacciRequest createFromParcel(Parcel parcel) {
				return new FibonacciRequest(parcel);
			}

			@Override
			public FibonacciRequest[] newArray(int len) {
				return new FibonacciRequest[len];
			}
	};
	
	private final long n;
	
	private final Type type;
	
	public FibonacciRequest(long n, Type type) {
		this.n = n;
		this.type = type;
	}

	public FibonacciRequest(Parcel parcel) {
		this(parcel.readLong(), Type.values()[parcel.readInt()]);
	}

	public long getN() { return n; }

	public Type getType() { return type; }

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeLong(n);
		parcel.writeInt(type.ordinal());
	}
}
