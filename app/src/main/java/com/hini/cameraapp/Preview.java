package com.hini.cameraapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by Myeongho on 2016-01-14.
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private Context mContext;
	Camera mCamera;
	SurfaceHolder mHolder;
	private int mCameraFacing;

	public Preview(Context context, int mCameraFacing) {
		super(context);

		this.mCameraFacing = mCameraFacing;

		this.mContext = context;
		this.mHolder = getHolder();

		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.addCallback(this);
	}

	public void zoomUP() {
		if (mCamera != null && mCamera.getParameters() != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			int maxZoom = parameters.getMaxZoom();
			int currZoom = parameters.getZoom();

			currZoom += 5;
			if (currZoom >= maxZoom) currZoom = maxZoom;
			parameters.setZoom(currZoom);
			mCamera.setParameters(parameters);
		}
	}

	public void zoomDown() {
		if (mCamera != null && mCamera.getParameters() != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			int minZoom = 0;
			int currZoom = parameters.getZoom();

			currZoom -= 5;
			if (currZoom <= minZoom) currZoom = minZoom;
			parameters.setZoom(currZoom);
			mCamera.setParameters(parameters);
		}
	}

	public int getMaxZoom() {
		int maxZoom = 0;
		if (mCamera != null && mCamera.getParameters() != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			maxZoom = parameters.getMaxZoom();
		}
		return maxZoom;
	}

	public int getCurZoom() {
		int curZoom = 0;
		if (mCamera != null && mCamera.getParameters() != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			curZoom = parameters.getZoom();
		}
		return curZoom;
	}

	public void setZoom(int zoom) {
		if (mCamera != null && mCamera.getParameters() != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setZoom(zoom);
			mCamera.setParameters(parameters);
		}
	}

	// 사진을 찍을때 호출되는 함수 (스냅샷)
	public void capture() {
		if (mCamera != null)
			mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
	}

	Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
		public void onShutter() {
		}
	};

	Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
		public void onPictureTaken(final byte[] data, final Camera camera) {
			mCamera.stopPreview();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// 사진데이타를 비트맵 객체로 저장
						Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						Matrix m = new Matrix();
						m.postRotate(mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK ? 90 : 270);
						Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

						bitmap.recycle();

						// bitmap 이미지를 이용해 앨범에 저장
						// 내용재공자를 통해서 앨범에 저장
						String outUriStr = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bm, "Captured Image", "Captured Image using Camera.");

						if (outUriStr == null) {
							Log.d("SampleCapture", "Image insert failed.");
							return;
						} else {
							Uri outUri = Uri.parse(outUriStr);
							mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outUri));
						}

						((Activity) mContext).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mContext, "카메라로 찍은 사진을 앨범에 저장했습니다.", Toast.LENGTH_LONG).show();
							}
						});

						// 다시 미리보기 화면 보여줌
						camera.startPreview();
					} catch (Exception e) {
						Log.e("SampleCapture", "Failed to insert image.", e);
					}
				}
			}).start();
		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
	                           int height) {
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera = Camera.open(mCameraFacing);
			mCamera.setDisplayOrientation(90);
			// 방향 설정
			Camera.Parameters parameters = mCamera.getParameters();
			// 포커스 설정
			parameters.setFocusMode(
					mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK ?
							Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : Camera.Parameters.FOCUS_MODE_FIXED);

			// 프리뷰 사이즈 설정
			List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
			Camera.Size previewSize = previewSizes.get(0);
			parameters.setPreviewSize(previewSize.width, previewSize.height);

			// 사진 설정
			parameters.setJpegQuality(70);
			parameters.setPictureFormat(ImageFormat.JPEG);
			parameters.setPictureSize(previewSize.width, previewSize.height);
			mCamera.setParameters(parameters);

			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
}