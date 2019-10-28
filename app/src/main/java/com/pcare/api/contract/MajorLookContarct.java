package com.pcare.api.contract;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

import com.pcare.api.base.IView;

/**
 * @Author: gl
 * @CreateDate: 2019/10/23
 * @Description:
 */
public interface MajorLookContarct {
    interface Model{

    }
    interface Presenter{
        void openCamera();
        void startCameraThread();
        void startPreview();
        TextureView.SurfaceTextureListener getTextureListener();
        void closeSession();


    }
    interface View extends IView {
        void startCamera();//开启摄像头
        SurfaceTexture getSurfaceTexture();
        void finishFace();
    }
}
