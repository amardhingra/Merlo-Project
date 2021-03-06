package com.example.merloclient;

import com.example.merloclient.CameraPreview;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.widget.FrameLayout;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

public class ScannerActivity extends Activity {

	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;

	ImageScanner scanner;

	static {
		System.loadLibrary("iconv");
	}

	boolean running = true;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_scanner);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();

		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);

		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		FrameLayout preview = (FrameLayout) findViewById(R.id.cameraFrame);
		preview.addView(mPreview);
	}

	public void onPause() {
		super.onPause();
		releaseCamera();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {

			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				running = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					Intent returnIntent = new Intent();
					returnIntent.putExtra("USERID", sym.getData());
					setResult(RESULT_OK, returnIntent);
					finish();
				}

			}
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (running)
				mCamera.autoFocus(autoFocusCB);
		}
	};

}
