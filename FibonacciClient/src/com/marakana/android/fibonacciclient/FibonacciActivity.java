package com.marakana.android.fibonacciclient;

import java.util.Locale;

import com.marakana.android.fibonaccicommon.FibonacciRequest;
import com.marakana.android.fibonaccicommon.FibonacciResponse;
import com.marakana.android.fibonaccicommon.IFibonacciService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class FibonacciActivity extends Activity implements OnClickListener, ServiceConnection {
	private static final String TAG = "FIBACT";

	private IFibonacciService service;
	private EditText input;
	private RadioGroup type;
	private TextView output;
	private Button button;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.input = (EditText) super.findViewById(R.id.input);
		this.type = (RadioGroup) super.findViewById(R.id.type);
		this.output = (TextView) super.findViewById(R.id.output);
		button = (Button) super.findViewById(R.id.button);
		button.setOnClickListener(this);
		button.setEnabled(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindService(new Intent(IFibonacciService.class.getName()), this, BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(this);
		button.setEnabled(false);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder svc) {
		service = IFibonacciService.Stub.asInterface(svc);
		button.setEnabled(true);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Toast.makeText(this, "Service disconnected!", Toast.LENGTH_LONG).show();
		button.setEnabled(true);
	}

	public void onClick(View view) {
		String s = this.input.getText().toString();
		if (TextUtils.isEmpty(s)) { return; }
		
		final long n;
		try { n = Long.parseLong(s); }
		catch (NumberFormatException e) {
			this.input.setError(super.getText(R.string.input_error));
			return;
		}

		// build the request object
		final FibonacciRequest.Type type;
		switch (FibonacciActivity.this.type.getCheckedRadioButtonId()) {
		case R.id.type_fib_jr:
			type = FibonacciRequest.Type.RECURSIVE_JAVA;
			break;
		case R.id.type_fib_ji:
			type = FibonacciRequest.Type.ITERATIVE_JAVA;
			break;
		case R.id.type_fib_nr:
			type = FibonacciRequest.Type.RECURSIVE_NATIVE;
			break;
		case R.id.type_fib_ni:
			type = FibonacciRequest.Type.ITERATIVE_NATIVE;
			break;
		default:
			return;
		}
		final FibonacciRequest request = new FibonacciRequest(n, type);

		// showing the user that the calculation is in progress
		final ProgressDialog dialog = ProgressDialog.show(
				this,
				"",
				getText(R.string.progress_text), true);

		// since the calculation can take a long time, we do it in a separate
		// thread to avoid blocking the UI
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				// this method runs in a background thread
				try {
					long totalTime = SystemClock.uptimeMillis();
					FibonacciResponse response
						= FibonacciActivity.this.service.fib(request);
					totalTime = SystemClock.uptimeMillis() - totalTime;
					// generate the result
					return String.format(
							"fibonacci(%d)=%d\nin %d ms\n(+ %d ms)",
							n,
							response.getResult(),
							response.getTimeInMillis(),
							totalTime - response.getTimeInMillis());
				}
				catch (RemoteException e) {
					Log.wtf(TAG, "Failed to communicate with the service", e);
					return null;
				}
			}

			@Override
			protected void onPostExecute(String result) {
				// get rid of the dialog
				dialog.dismiss();
				if (result != null) {
					// show the result to the user
					FibonacciActivity.this.output.setText(result);
				}
				else {
					// handle error
					Toast.makeText(
							FibonacciActivity.this,
							R.string.fib_error,
							Toast.LENGTH_SHORT)
							.show();
				}
			}
		}.execute(); // run our AsyncTask
	}
}
