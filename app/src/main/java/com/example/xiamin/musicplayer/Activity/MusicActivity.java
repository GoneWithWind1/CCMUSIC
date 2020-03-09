package com.example.xiamin.musicplayer.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiamin.musicplayer.Activity.Fragment.LocalMusicFragment;
import com.example.xiamin.musicplayer.Activity.Fragment.PlayFragment;
import com.example.xiamin.musicplayer.Activity.Fragment.SearchFragment;
import com.example.xiamin.musicplayer.Activity.Fragment.SongListFragment;
import com.example.xiamin.musicplayer.Bean.MusicInfoBean;
import com.example.xiamin.musicplayer.MVP.IPlayBar;
import com.example.xiamin.musicplayer.MyView.PlayerBar;
import com.example.xiamin.musicplayer.R;
import com.example.xiamin.musicplayer.Service.MusicPlayService;
import com.example.xiamin.musicplayer.adapter.FragmentAdapter;
import com.example.xiamin.musicplayer.utils.Actions;
import com.example.xiamin.musicplayer.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Xiamin on 2016/9/15.
 */
public class MusicActivity extends BaseActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener
        , IPlayBar, PlayerBar.ShowPlayingFragmentListener
        , NavigationView.OnNavigationItemSelectedListener {

   private    DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;
    private  NavigationView searchView;

    private ImageView mvMenu;
    private ImageView mIvSearch;
    private TextView mTvLocalMusic;
    private TextView mTvOnlineMusic;
    private  ViewPager mViewPager;
    private PlayerBar mPlayBar;

    private View vNavigationHeader;
    private LocalMusicFragment mLocalMusicFragment;
    private SongListFragment mSongListFragment;
    private PlayFragment mPlayFragment;
    private SearchFragment mSearchFragment;

    private static final String TAG = "MusicActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        mNavigationView=(NavigationView)findViewById(R.id.navigation_view);
        searchView=(NavigationView)findViewById(R.id.search_view);
        mvMenu=(ImageView)findViewById(R.id.iv_menu);
        mIvSearch=(ImageView)findViewById(R.id.iv_search);
        mTvLocalMusic=(TextView)findViewById(R.id.tv_local_music);
        mTvOnlineMusic=(TextView)findViewById(R.id.tv_online_music);
        mViewPager=(ViewPager)findViewById(R.id.viewpager);
        initView();
        bindService();
        registerReceiver();
    }

    /**
     * 初始化view
     * 主界面的初始化，侧边栏的设置
     */
    private void initView() {
        mPlayBar = (PlayerBar) findViewById(R.id.fl_play_bar);

        mPlayBar.setShowPlayingFragmentListener(this);

        mViewPager.setOnPageChangeListener(this);
        mDrawerLayout.setOnClickListener(this);
        mIvSearch.setOnClickListener(this);
        mTvLocalMusic.setOnClickListener(this);
        mTvOnlineMusic.setOnClickListener(this);
        mvMenu.setOnClickListener(this);

        mNavigationView.setNavigationItemSelectedListener(this);
        searchView.setNavigationItemSelectedListener(this);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.MATCH_PARENT
                , ScreenUtils.dp2px(200f)));
        imageView.setImageResource(R.drawable.jay);
        mNavigationView.addHeaderView(imageView);

        ImageView imageView1 = new ImageView(this);
        imageView1.setLayoutParams(new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.MATCH_PARENT
                , ScreenUtils.dp2px(200f)));
        imageView1.setImageResource(R.drawable.jay);
        searchView.addHeaderView(imageView1);

        mLocalMusicFragment = new LocalMusicFragment();
        mSongListFragment = new SongListFragment();
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(mLocalMusicFragment);
        adapter.addFragment(mSongListFragment);
        mViewPager.setAdapter(adapter);
    }

    private MusicPlayService servicebinder;
    private ServiceConnection connet = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("TAG", "onServiceConnected");
            servicebinder = ((MusicPlayService.Mybinder) iBinder).getservice();
            //    servicebinder.initPlayer();
        }

        //当启动源和service连接意外丢失时会调用
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("TAG", "onServiceDisconnected");
        }
    };

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, MusicPlayService.class);
        bindService(intent, connet, Context.BIND_AUTO_CREATE);
    }

    public MusicPlayService getMusicService() {
        return servicebinder;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_menu: {
                Log.i("TAG", "点击侧滑按钮");
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            }
            case R.id.iv_search: {
                Log.i("TAG", "点击搜索按钮");
                 serchFragment();
                break;
            }
            case R.id.tv_local_music: {
                Log.i("TAG", "点击本地音乐");
                mViewPager.setCurrentItem(0);
                break;
            }
            case R.id.tv_online_music: {
                Log.i("TAG", "点击在线音乐");
                mViewPager.setCurrentItem(1);
                break;
            }
        }
    }

    private boolean mIsSearchFragment;

    private  void serchFragment()
    {
        mSearchFragment = new SearchFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_up, 0)
                .replace(android.R.id.content, mSearchFragment)
                .show(mSearchFragment)
                .commit();
        mIsSearchFragment=true;
    }

    private  void hideSearchFragment()
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mSearchFragment);
        ft.commit();
        mIsSearchFragment = false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            mTvLocalMusic.setSelected(true);
            mTvOnlineMusic.setSelected(false);
        } else if (position == 1) {
            mTvLocalMusic.setSelected(false);
            mTvOnlineMusic.setSelected(true);
        }
    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(playerReceiver);
        unbindService(connet);
    }

    @Override
    public void setPlayBar(MusicInfoBean musicInfoBean) {
        mPlayBar.setInfo(musicInfoBean);
    }

    @Override
    public void onBackPressed() {
        if (mIsPlayingFragment == true && mPlayFragment != null) {
            hidePlayingFragment();
            return;
        }
        if(mIsSearchFragment==true&&mSearchFragment!=null)
        {
            hideSearchFragment();
            return ;
        }



        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        if(mDrawerLayout.isDrawerOpen(GravityCompat.END))
        {   mDrawerLayout.closeDrawers();
            return;
        }
        moveTaskToBack(false);
        //    super.onBackPressed();
    }


    private boolean mIsPlayingFragment;

    @Override
    public void ShowPlayingFragment(MusicInfoBean mMusicInfoBean) {
        //每次点击都得刷新fragment，而hide和show不走生命周期
        mPlayFragment = new PlayFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_up, 0)
                .replace(android.R.id.content, mPlayFragment)
                .show(mPlayFragment)
                .commit();
        mIsPlayingFragment = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commit();
        mIsPlayingFragment = false;
    }

    /**
     * 侧边栏被点击的选项
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        mDrawerLayout.closeDrawers();
        /*对于NavigationItem 当被按下后会呈现暗色，我们需要手动将其置为可按的状态*/
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);
        switch (item.getItemId()) {
            case R.id.action_night:
                Toast.makeText(this, "action_night", Toast.LENGTH_SHORT).show();
              dark();
                break;
            case R.id.action_refresh:
                mLocalMusicFragment.refreshView();
                break;

            case R.id.action_timer:
                 timer_exit();
                return true;
            case R.id.action_exit:
                getMusicService().stopPlayer();
                for (Activity k : MusicPlayService.getActivityStack()) {
                    k.finish();
                }
                return true;
            case R.id.action_about:
               showDialog();
                return true;
        }
        return false;
    }

   //关于我们
      private   void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MusicActivity.this);
        View view = View.inflate(MusicActivity.this, R.layout.about_us, null);
        builder.setView(view);
        builder.setCancelable(true);

        Button btn_cancel=(Button)view.findViewById(R.id.btn_cancel);//取消按钮

        //取消或确定按钮监听事件处理
        final AlertDialog dialog = builder.create();
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //定时停止播放
    private void timer_exit()
    {    Resources resources =getResources();
        final String items[] = resources.getStringArray(R.array.timer_text);;
        AlertDialog dialog = new AlertDialog.Builder(MusicActivity.this)
                // .setIcon(R.mipmap.icon)//设置标题的图片
                .setTitle("定时退出")//设置对话框的标题
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                dialog.dismiss();
                                Toast.makeText(MusicActivity.this, "软件定时退出已取消", Toast.LENGTH_SHORT).show();
                                stopTimer();
                                break;
                            case 1:
                                 timer(10*60000);
                                Toast.makeText(MusicActivity.this, "软件将于10分钟后退出", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                timer(20*60000);
                                Toast.makeText(MusicActivity.this, "软件将于20分钟后退出", Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                timer(30*60000);
                                Toast.makeText(MusicActivity.this, "软件将于30分钟后退出", Toast.LENGTH_SHORT).show();
                                break;
                            case 4:
                                timer(45*60000);
                                Toast.makeText(MusicActivity.this, "软件将于45分钟后退出", Toast.LENGTH_SHORT).show();
                                break;
                            case 5:
                                timer(60*60000);
                                Toast.makeText(MusicActivity.this, "软件将于60分钟后退出", Toast.LENGTH_SHORT).show();
                                break;
                            case 6:
                                timer(90*60000);
                                Toast.makeText(MusicActivity.this, "软件将于90分钟后退出", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }).create();
        dialog.show();

    }

    public Timer timer;    //定时退出的定时器
    private void timer(long time) {
        if (timer != null)
            stopTimer();
        timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getMusicService().stopPlayer();
                    for (Activity k : MusicPlayService.getActivityStack()) {
                        k.finish();

                    }
                }
            }, time, time);
    }
    private void stopTimer(){
        if(timer != null){
          timer.cancel();
            // 一定设置为null，否则定时器不会被回收
            timer = null;
        }
    }



    /**
     * fragment触摸事件分发
     * 由于fragment没有触摸事件，而我的播放页需要监听手势，因此需要监听触摸
     */
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(
            10);

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyOnTouchListener listener : onTouchListeners) {
            if (listener != null) {
                listener.onTouch(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void registerMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.add(myOnTouchListener);
    }

    public void unregisterMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        if (onTouchListeners.contains(myOnTouchListener)) {
            onTouchListeners.remove(myOnTouchListener);
        }
    }

    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
    }


    private PlayerReceiver playerReceiver;

    private void registerReceiver() {
        playerReceiver = new PlayerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACTION_MEDIA_PAUSE);
        filter.addAction(MusicPlayService.UPDATE_ACTION);
         getApplicationContext().registerReceiver(playerReceiver, filter);
    }

    public class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(MusicPlayService.UPDATE_ACTION))
            {
                setPlayBar(getMusicService().getPlayingMusic());

            }
            if(action.equals(Actions.ACTION_MEDIA_PAUSE))
            {
                setPlayBar(getMusicService().getPlayingMusic());
            }
        }
    }
}
