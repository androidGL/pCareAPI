package com.pcare.api.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.core.app.ActivityCompat;


import com.pcare.api.base.BasePresenter;
import com.pcare.api.contract.MajorLookContarct;
import com.pcare.api.utils.PermissionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author: gl
 * @CreateDate: 2019/10/23
 * @Description:
 */
public class MajorLookPresenter extends BasePresenter<MajorLookContarct.View> implements MajorLookContarct.Presenter {

    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private Activity lookActivity;

    private String mCameraId;
    private Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private SurfaceTexture mSurfaceTexture;

    public MajorLookPresenter(MajorLookContarct.View view) {
        super(view);
        lookActivity = (Activity) getView();
    }


    //开始摄像的线程
    @Override
    public void startCameraThread() {
        PermissionHelper.requestCameraPermission(lookActivity, true);
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };
    @Override
    public void openCamera() {
        CameraManager manager = (CameraManager) lookActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(lookActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startPreview() {
        mSurfaceTexture = getView().getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void closeSession() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    public TextureView.SurfaceTextureListener getTextureListener() {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //当SurefaceTexture可用的时候，设置相机参数并打开相机
                setupCamera(width, height);
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };
    }

    //设置相机
    private void setupCamera(int width, int height) {
        CameraManager manager = (CameraManager) lookActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            //遍历所有的摄像头
            for(String cameraId : manager.getCameraIdList()){
                //获取到每个相机的参数对象，包含前后摄像头，分辨率等
                CameraCharacteristics  cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                //摄像头的方向
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                //此处默认打开前置置摄像头
                if (facing != null && facing != CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                //根据TextureView的尺寸设置预览尺寸
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                mCameraId = cameraId;
                break;
            }
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }



    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

}
