package com.example.xiamin.musicplayer.Activity.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Xml;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xiamin.musicplayer.Activity.MusicActivity;
import com.example.xiamin.musicplayer.Bean.MusicInfoBean;
import com.example.xiamin.musicplayer.CircleImage.CircleImageView;
import com.example.xiamin.musicplayer.CircleImage.LrcContent;
import com.example.xiamin.musicplayer.CircleImage.LrcProcess;
import com.example.xiamin.musicplayer.CircleImage.LrcView;
import com.example.xiamin.musicplayer.MVP.IPlayBar;
import com.example.xiamin.musicplayer.R;
import com.example.xiamin.musicplayer.Service.MusicPlayService;
import com.example.xiamin.musicplayer.utils.Actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Xiamin on 2016/9/15.
 * 播放列表界面
 */
public class PlayFragment extends BaseFragment implements
        View.OnClickListener, GestureDetector.OnGestureListener {
    private ImageView mBackHome;    //返回
    private TextView mArtistText;    //歌手
    private TextView mTitleText;    //歌曲名称
    private ImageView mBackGround;       //背景
    private TextView m_CurrentTime;        //当前时间
    private SeekBar mProgress;      //进度条
    private TextView mTotalTime;      //总时间
    private ImageView mPlayButton;     //播放/暂停
    private ImageView mNextButton;     //下一首
    private ImageView mPrevButton;       //上一首
    private ImageView mLrcSetting;      //歌词设置
    private ImageView mModel;         //播模式设置

    CircleImageView mPlayImageView;       //歌词/专辑图片视图
    MusicInfoBean mMusicBean;             //当前播放音乐类
    MusicActivity.MyOnTouchListener onTouchListener;


    public LrcView lrcView;         //歌词
    private PlayerReceiver playerReceiver;      //广播接收器

    public static final String UPDATE_ACTION = "com.cqb.action.UPDATE_ACTION";  //更新动作

    @Override
    public void initView() {


        mBackHome.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mLrcSetting.setOnClickListener(this);
        mModel.setOnClickListener(this);
        mProgress.setOnSeekBarChangeListener(new SeekBarChangeListener());
        mPlayImageView = (CircleImageView) getView().findViewById(R.id.fragment_play_circle_image);
        mPlayImageView.setOnClickListener(this);
        mMusicBean = getPlayService().getPlayingMusic();
        initUI(mMusicBean);
        initLrc();
        onTouchListener = new MusicActivity.MyOnTouchListener() {
            @Override
            public boolean onTouch(MotionEvent ev) {
                gestureDetector.onTouchEvent(ev);
                return false;
            }
        };
        ((MusicActivity) getActivity()).registerMyOnTouchListener(onTouchListener);
    }
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        log("onCreateView");

        registerReceiver();//注册广播

        view = inflater.inflate(R.layout.fragment_play, container, false);
        mBackHome = (ImageView) view.findViewById(R.id.iv_back);
        mArtistText = (TextView) view.findViewById(R.id.tv_artist);
        mTitleText = (TextView) view.findViewById(R.id.tv_title);
        mBackGround = (ImageView) view.findViewById(R.id.iv_play_page_bg);
        m_CurrentTime = (TextView) view.findViewById(R.id.tv_current_time);
        mProgress = (SeekBar) view.findViewById(R.id.sb_progress);
        mTotalTime = (TextView) view.findViewById(R.id.tv_total_time);
        mPlayButton = (ImageView) view.findViewById(R.id.iv_play);
        mNextButton = (ImageView) view.findViewById(R.id.iv_next);
        mPrevButton = (ImageView) view.findViewById(R.id.iv_prev);
        lrcView = (LrcView) view.findViewById(R.id.lrcShowView);
        mLrcSetting = (ImageView) view.findViewById(R.id.iv_lrcsettig);
        mModel = (ImageView) view.findViewById(R.id.iv_mode);
        return view;
    }

    private int state = 0;     //播放模式 初始列表播放

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back: {
                log("onClick R.id.bar_iv_menu");
                hideThis();
                break;
            }
            case R.id.iv_mode: {
                state = (state + 1) % 3;
                if (state == 0) Toast.makeText(getContext(), "列表播放", Toast.LENGTH_SHORT).show();
                else if (state == 1)
                    Toast.makeText(getContext(), "随机播放", Toast.LENGTH_SHORT).show();
                else Toast.makeText(getContext(), "单曲循环", Toast.LENGTH_SHORT).show();
                mModel.setImageResource(R.drawable.play_mode_level_list);
                mModel.setImageLevel(state);
                getPlayService().setState(state);
                break;
            }

            case R.id.iv_lrcsettig: {
                final String items[] = {"搜索歌词", "调整歌词大小", "下载歌词"};
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        // .setIcon(R.mipmap.icon)//设置标题的图片
                        .setTitle("歌词选项")//设置对话框的标题
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
                                        if (!getLrcByLrcId())
                                            Toast.makeText(getContext(), "未收索到匹配歌词", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        showInputDialog();
                                        break;
                                    case 2:
                                        new Thread() {
                                            public void run() {
                                              wrtieContentFromUrl(get_lrc_url, getLrcPath(mMusicBean.getTitle(), mMusicBean.getArtist()));
                                            };
                                        }.start();
                                        Toast.makeText(getContext(), "下载成功", Toast.LENGTH_SHORT).show();
                                        break;
                                }


                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
                break;
            }
            case R.id.iv_next: {
                getPlayService().next();
                initLrc();
                mMusicBean = getPlayService().getPlayingMusic();
                initUI(mMusicBean);
                ((IPlayBar) getActivity()).setPlayBar(mMusicBean);
                break;
            }
            case R.id.iv_prev: {
                getPlayService().preMusic();
                mMusicBean = getPlayService().getPlayingMusic();
                initUI(mMusicBean);
                initLrc();
                ((IPlayBar) getActivity()).setPlayBar(mMusicBean);
                break;
            }
            case R.id.iv_play: {
                play_pressed();
                break;
            }
        }
    }

    private void showInputDialog() {
    /*@setView 装入一个EditView
     */
        final EditText editText = new EditText(getActivity());
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(getActivity());
        inputDialog.setTitle("输入歌词大小（最大100）").setView(editText);
        inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float size = Float.parseFloat(editText.getText().toString());
                if (size > 100) size = 100;
                lrcView.setTextsize(size);
                Toast.makeText(getActivity(), editText.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        }).show();
    }


    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                audioTrackChange(progress); //用户控制进度的改变
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    public void audioTrackChange(int progress) {

        getPlayService().ChangeBar(progress);
    }

    public static String formatTime(long time) {
        // TODO Auto-generated method stub
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }


    public void initUI(MusicInfoBean musicBean) {
        Glide.with(this).load(musicBean.getCoverUri()).
                error(R.drawable.default_cover).
                into(mPlayImageView);
        Glide.with(this).
                load(musicBean.getCoverUri()).
                error(R.drawable.default_cover).
                into(mBackGround);
        mBackGround.setAlpha(70);

        if (musicBean.getDuration() > 10000) {
            mTotalTime.setText(formatTime(musicBean.getDuration()));
            mProgress.setMax((int) musicBean.getDuration());
        } else {

            mTotalTime.setText(formatTime(musicBean.getDuration() * 1000));
            mProgress.setMax((int) (musicBean.getDuration() * 1000));
        }
        mModel.setImageResource(R.drawable.play_mode_level_list);
        state = getPlayService().getState();
        mModel.setImageLevel(state);

        m_CurrentTime.setText(formatTime(MusicPlayService.getCurrentTime()));
        mProgress.setProgress((int) MusicPlayService.getCurrentTime());

        handler.postDelayed(runnable, 1000); //开启定时器

        mArtistText.setText(musicBean.getArtist());
        mTitleText.setText(musicBean.getTitle());

        //根据状态 改变按钮的样式 并且设置转轮情况
        if (MusicPlayService.getPlayingState()) {
            mPlayButton.setSelected(true);
            mPlayImageView.StartRotation();
        } else {
            mPlayButton.setSelected(false);
            mPlayImageView.StopRotation();
        }
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            m_CurrentTime.setText(formatTime(MusicPlayService.getCurrentTime()));
            mProgress.setProgress((int) MusicPlayService.getCurrentTime());
            handler.postDelayed(this, 1000);
        }
    };

    /**
     * 当play按钮
     */
    private void play_pressed() {
        //获取服务 触发暂停或者播放
        getPlayService().playPause();
        //根据状态 改变按钮的样式 并且设置转轮情况
        if (MusicPlayService.getPlayingState()) {
            mPlayButton.setSelected(true);
            mPlayImageView.StartRotation();
        } else {
            mPlayButton.setSelected(false);
            mPlayImageView.StopRotation();
        }
        //再跟新activity中的按钮样式
        mMusicBean = getPlayService().getPlayingMusic();
        ((IPlayBar) getActivity()).setPlayBar(mMusicBean);
    }

    private void hideThis() {
        /**
         * 使用hide 完美解决销毁问题
         */
        if (getFragmentManager() != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(0, R.anim.fragment_slide_down).hide(this).commit();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        log("onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unregisterReceiver(playerReceiver);
        log("onDestroy");
    }

    @Override
    public void onPause() {
        super.onPause();
        log("onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        log("onStop");
    }

    GestureDetector gestureDetector = new GestureDetector(getActivity(), this);


    @Override
    public boolean onDown(MotionEvent e) {
        //       Log.i("iii", "GestureDetector: " + e.getX() + "-" + e.getY());
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e2.getY() - e1.getY() > 150) {
            Log.i("iii", "GestureDetector: " + e2.getY() + "-" + e1.getY());
            Log.i("iii", "ondown");


            hideThis();
            /**
             * 若不unregister 则第二次出现空指针异常 因为每次都是新fragment
             */
            ((MusicActivity) getActivity()).unregisterMyOnTouchListener(onTouchListener);
        }
        return false;
    }

    private void registerReceiver() {
        playerReceiver = new PlayerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION);
        filter.addAction(Actions.ACTION_MEDIA_PAUSE);
        getActivity().getApplicationContext().registerReceiver(playerReceiver, filter);
    }

    public class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UPDATE_ACTION)) {
                mMusicBean = getPlayService().getPlayingMusic();
                initUI(mMusicBean);
                initLrc();
                ((IPlayBar) getActivity()).setPlayBar(mMusicBean);
            }
            if (action.equals(Actions.ACTION_MEDIA_PAUSE)) {
                if (MusicPlayService.getPlayingState()) {
                    mPlayButton.setSelected(true);
                    mPlayImageView.StartRotation();
                } else {
                    mPlayButton.setSelected(false);
                    mPlayImageView.StopRotation();
                }
                //再跟新activity中的按钮样式
                mMusicBean = getPlayService().getPlayingMusic();
                ((IPlayBar) getActivity()).setPlayBar(mMusicBean);
            }
        }

    }

    private LrcProcess mLrcProcess; //歌词处理
    private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象
    private int index = 0;          //歌词检索值


    public void initLrc() {
        mLrcProcess = new LrcProcess();
        //读取歌词文件
        mMusicBean = getPlayService().getPlayingMusic();
        if (mMusicBean.getType().equals(MusicInfoBean.Type.ONLINE))
            mLrcProcess.getURLLrc(mMusicBean.getLrcLink());
        else mLrcProcess.readLRC(mMusicBean.getUri());
        //传回处理后的歌词文件
        lrcList = mLrcProcess.getLrcList();
        lrcView.setmLrcList(lrcList);
        //切换带动画显示歌词
        if (lrcList.size() != 0) handler.post(mRunnable);
    }

    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            lrcView.setIndex(lrcIndex());
            lrcView.invalidate();
            handler.postDelayed(mRunnable, 100);
        }
    };

    private long duration;
    private long currentTime;

    public int lrcIndex() {
        if (MusicPlayService.getPlayingState()) {
            currentTime = MusicPlayService.getCurrentTime();
            duration = MusicPlayService.getPlayDuratin();
        }
        if (currentTime < duration) {
            for (int i = 0; i < lrcList.size(); i++) {
                if (i < lrcList.size() - 1) {
                    if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
                        index = i;
                    }
                    if (currentTime > lrcList.get(i).getLrcTime() && currentTime < lrcList.get(i + 1).getLrcTime()) {
                        index = i;
                    }
                }
                if (i == lrcList.size() - 1 && currentTime > lrcList.get(i).getLrcTime()) {
                    index = i;
                }
            }
        }
        return index;
    }


    private String get_id_url;
    private String baseUrl;
    private String get_lrc_url;


    private boolean getLrcByLrcId() {
        get_id_url = "http://geci.me/api/lyric/";
        get_lrc_url = "";
        get_lrc_url = getLrcURL(mMusicBean.getTitle(), mMusicBean.getArtist());
        // 开始获取 lrc
        mLrcProcess.getURLLrc(get_lrc_url);
        //传回处理后的歌词文件
        lrcList = mLrcProcess.getLrcList();
        lrcView.setmLrcList(lrcList);
        //切换带动画显示歌词

        if (lrcList.size() == 0) return false;
        else {
            handler.post(mRunnable);
            return true;
        }
    }

    public String Encode(String str) {

        try {
            return URLEncoder.encode(str.trim(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return str;
    }

    public String getQueryLrcURL(String title, String artist) {
        return get_id_url + Encode(title) + "/" + Encode(artist);
    }

    public String getLrcPath(String title, String artist) {
        File f = new File(mMusicBean.getUri());
        baseUrl = f.getParentFile().getAbsolutePath() + "/";
        return baseUrl + artist + " - " + title + ".lrc";
    }


    //获取歌词下载地址
    public String getLrcURL(String title, String artist) {
        String queryLrcURLStr = getQueryLrcURL(title, artist);
        try {
            URL url = new URL(queryLrcURLStr);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            StringBuffer sb = new StringBuffer();

            String temp;
            while ((temp = in.readLine()) != null) {
                sb.append(temp);
            }

            JSONObject jObject = new JSONObject(sb.toString());
            int count = jObject.getInt("count");
            // int index = count == 0 ? 0 : new Random().nextInt() % count;
            if (count != 0) {
                int index = new Random().nextInt(count);
                JSONArray jArray = jObject.getJSONArray("result");
                JSONObject obj = jArray.getJSONObject(index);
                return obj.getString("lrc");
            } else {
                return null;
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    //歌词下载
    public void wrtieContentFromUrl(String urlPath, String lrcPath) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(urlPath).build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    PrintStream ps = new PrintStream(new File(lrcPath));
                    byte[] bytes = response.body().bytes();
                    ps.write(bytes, 0, bytes.length);
                    ps.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}