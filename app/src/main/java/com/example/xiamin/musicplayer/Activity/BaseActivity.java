package com.example.xiamin.musicplayer.Activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.xiamin.musicplayer.R;
import com.example.xiamin.musicplayer.Service.MusicPlayService;

import butterknife.ButterKnife;

/**
 * Created by Xiamin on 2016/9/15.
 * 基类activity，主要负责检查toolbar是否存在，以及将activity保存的作用
 */
public class BaseActivity extends AppCompatActivity {
    private   Toolbar mToolbar;
    protected Handler mHandler;


    private int theme = R.style.AppTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            theme = savedInstanceState.getInt("theme");
            setTheme(theme);
        }
        setContentView(R.layout.activity_music);

        mToolbar=(Toolbar)findViewById(R.id.toolbar);
        mHandler = new Handler();
        setSystemBarTransparent();
        MusicPlayService.addToStack(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void dark() {
        theme = (theme == R.style.AppTheme) ? R.style.NightAppTheme : R.style.AppTheme;
        BaseActivity.this.recreate();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);
}

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        theme = savedInstanceState.getInt("theme");
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initView();
    }

    @Override
    protected void onDestroy() {
        MusicPlayService.removeFromStack(this);
        super.onDestroy();
    }

    private void initView() {
        ButterKnife.bind(this);
        /**
         * 此处用来检查xml文件是否带了toolbar
         */
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id 'toolbar'");
        }
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            Log.i("iii","getSupportActionBar is not null");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setSystemBarTransparent() {
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public void showSoftKeyboard(final EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 200L);
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
