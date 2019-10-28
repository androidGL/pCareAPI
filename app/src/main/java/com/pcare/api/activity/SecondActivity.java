package com.pcare.api.activity;

import android.os.Bundle;

import com.pcare.api.R;
import com.pcare.api.base.IPresenter;
import com.pcare.api.base.SimpleBaseActivity;

/**
 * @Author: gl
 * @CreateDate: 2019/10/28
 * @Description:
 */
public class SecondActivity extends SimpleBaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_second;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api();
    }

    private void api(){


    }

    @Override
    protected IPresenter bindPresenter() {
        return null;
    }
}
