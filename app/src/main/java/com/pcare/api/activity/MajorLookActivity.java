package com.pcare.api.activity;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pcare.api.R;
import com.pcare.api.base.SimpleBaseActivity;
import com.pcare.api.contract.MajorLookContarct;
import com.pcare.api.presenter.MajorLookPresenter;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * @Author: gl
 * @CreateDate: 2019/10/16
 * @Description:
 */
public class MajorLookActivity extends SimpleBaseActivity<MajorLookPresenter> implements MajorLookContarct.View {


    @BindView(R.id.look_start)
    TextView start;

    @BindView(R.id.look_container)
    TextureView textureView;

    private MajorLookPresenter presenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_major_look;
    }

    @Override
    protected MajorLookPresenter bindPresenter() {
        presenter = new MajorLookPresenter((MajorLookActivity) getSelfActivity());
        return presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCamera();
    }

    //点击人脸识别按钮的方法
    @OnClick(R.id.look_start)
    public void start(View view){


    }

    //开启摄像头
    @Override
    public void startCamera() {
        presenter.startCameraThread();
        if(!textureView.isAvailable()){
            textureView.setSurfaceTextureListener(presenter.getTextureListener());
        }else {
            presenter.startPreview();
        }
    }

    @Override
    public SurfaceTexture getSurfaceTexture(){
        return textureView.getSurfaceTexture();
    }


    @Override
    public void finishFace() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.closeSession();
    }
}
