package com.example.xiamin.musicplayer.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.xiamin.musicplayer.MyView.PlayerBar;
import com.example.xiamin.musicplayer.R;
import com.example.xiamin.musicplayer.Service.MusicPlayService;
import com.example.xiamin.musicplayer.utils.Actions;

/**
 * Created by Xiamin on 2016/10/22.
 * 监听耳机拔插事件  MusicPlayService中初始化
 */

public class PhoneComingReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
     Intent serviceIntent = new Intent(context, MusicPlayService.class);
     serviceIntent.setAction(Actions.ACTION_MEDIA_PAUSE);
     context.startService(serviceIntent);
     Intent sendIntent1= new Intent(Actions.ACTION_MEDIA_PAUSE);
        // 发送广播，将被Activity组件中的BroadcastReceiver接收到
        context.sendBroadcast(sendIntent1);
    }
}
