package com.pcare.api.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import butterknife.ButterKnife;

public abstract class SimpleBaseActivity<P extends IPresenter> extends Activity implements IView{
    protected P presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        presenter = bindPresenter();
    }
    public abstract int getLayoutId();
    // 绑定Presenter
    protected abstract P bindPresenter();


    @Override
    public Activity getSelfActivity() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(presenter != null)
            presenter.detachView();
    }

}
