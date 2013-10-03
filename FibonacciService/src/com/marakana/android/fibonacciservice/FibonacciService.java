package com.marakana.android.fibonacciservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FibonacciService extends Service {

	private IFibonacciServiceImpl svc;
	
	@Override
	public void onCreate() {
		super.onCreate();
		svc = new IFibonacciServiceImpl();
	}

	@Override
	public IBinder onBind(Intent i) {
		return svc;
	}
}
