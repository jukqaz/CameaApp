package com.hini.cameraapp;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.tisquare.cameraapp.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private Context mContext;
	private SeekBar mSbZoom;
	private Button mBtPicture, mBtChangeMode;
	private Preview mPreview;
	private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		initPreview();
	}

	private void initPreview() {
		mPreview = new Preview(mContext, mCameraFacing);
		setContentView(mPreview);
		addContentView(LayoutInflater.from(this).inflate(R.layout.activity_main, null),
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		mSbZoom = (SeekBar) findViewById(R.id.sb_zoom);
		mSbZoom.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mSbZoom.setProgress(mPreview.getCurZoom());
				mSbZoom.setMax(mPreview.getMaxZoom());
			}
		}, 200);

		mBtPicture = (Button) findViewById(R.id.bt_take_picture);
		mBtChangeMode = (Button) findViewById(R.id.bt_change_mode);

		mBtPicture.setOnClickListener(this);
		mBtChangeMode.setOnClickListener(this);
	}

	private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			mSbZoom.setProgress(progress);
			mPreview.setZoom(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				mPreview.zoomUP();
				mSbZoom.setProgress(mPreview.getCurZoom());
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				mPreview.zoomDown();
				mSbZoom.setProgress(mPreview.getCurZoom());
				return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// 화면 전화
			case R.id.bt_change_mode:
				mCameraFacing = (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK)
						? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
				initPreview();
				break;
			// 사진찍기
			case R.id.bt_take_picture:
				mPreview.capture();
				break;
		}
	}

	private double mClickTime;

	private float mPinchOriX;
	private float mPinchOriY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				// Double Tab
				if (System.currentTimeMillis() - mClickTime < 200) {
					mPreview.zoomUP();
					mSbZoom.setProgress(mPreview.getCurZoom());
					return true;
				}
				mClickTime = System.currentTimeMillis();
				//
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				// Pinch Zoom
				if (event.getPointerCount() == 2) {

				}
				break;
		}
		return false;
	}
}
