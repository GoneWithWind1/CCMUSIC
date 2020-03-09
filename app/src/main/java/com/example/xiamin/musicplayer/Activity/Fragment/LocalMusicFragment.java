package com.example.xiamin.musicplayer.Activity.Fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiamin.musicplayer.Activity.MusicActivity;
import com.example.xiamin.musicplayer.Bean.MusicInfoBean;
import com.example.xiamin.musicplayer.R;
import com.example.xiamin.musicplayer.Service.MusicPlayService;
import com.example.xiamin.musicplayer.adapter.LocalMusicAdapter;
import com.example.xiamin.musicplayer.utils.Actions;
import com.example.xiamin.musicplayer.utils.MusicScanUntils;
import com.example.xiamin.musicplayer.utils.PermissionsChecker;

import java.io.File;



/**
 * Created by Xiamin on 2016/9/15.
 * 本地音乐fragment，位于主界面的viewpager中
 */
public class LocalMusicFragment extends BaseFragment implements AdapterView.OnItemClickListener
        ,AdapterView.OnItemLongClickListener{

    private  ListView mListLocalMusic;
 //   private  TextView mTvEmpty;
    private LocalMusicAdapter adapter;
    protected View mView;

    private  MusicActivity musicActivity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.musicActivity = (MusicActivity) context;
    }
    public static LocalMusicFragment newInstance() {
        LocalMusicFragment my = new LocalMusicFragment();
        return my;
    }

    @Override
    public  void initView() {
        adapter = new LocalMusicAdapter();
        mListLocalMusic.setAdapter(adapter);
        mListLocalMusic.setOnItemClickListener(this);
        mListLocalMusic.setOnItemLongClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView=inflater.inflate(R.layout.fragment_local_music,container,false);
        mListLocalMusic=(ListView)mView.findViewById(R.id.lv_local_music);
      //  mTvEmpty=(TextView)mView.findViewById(R.id.tv_empty);
        return mView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        /**
         * 让service播放该音乐
         * 改变activity上的playbar信息
         */
        getPlayService().play(i);
        ((MusicActivity)getActivity()).setPlayBar(MusicPlayService.getMusicList().get(i));
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l)
    {
        final String items[] = {"删除", "设为铃声","详细信息"};
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                // .setIcon(R.mipmap.icon)//设置标题的图片
                .setTitle("歌曲选项")//设置对话框的标题
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                delect(i);
                                break;
                            case 1:
                                setMyRingtone(MusicPlayService.getMusicList().get(i).getUri());
                                break;
                            case 2:
                                showDialog(i);
                                break;
                        }


                    }
                }).create();
        dialog.show();
        return true;
    }

    public   void refreshView()
    {
       MusicScanUntils.scanMusic(getContext(),MusicPlayService.getMusicList());
        adapter = new LocalMusicAdapter();
        mListLocalMusic.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

//删除本地音乐
    public void delect(final int i)
    {
        final String url = (String)MusicPlayService.getMusicList().get(i).getUri();
        String na = (String)MusicPlayService.getMusicList().get(i).getTitle();
        final AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle("是否删除该歌曲 ==>"+na);
        ab.setMessage("删除该歌曲，");
        ab.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File mf = new File(url);
                File  lrcmf=new File(url.replace(".mp3",".lrc"));
                if(mf.exists()){
                    mf.delete();
                    getContext().getContentResolver().delete(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Audio.Media.DATA+ " = '" + url + "'", null);
                    Toast.makeText(getActivity(), "OK成功删除", Toast.LENGTH_LONG).show();

                    if(lrcmf.exists())
                        lrcmf.delete();
                }else {
                    Toast.makeText(getActivity(), "该文件不存在", Toast.LENGTH_LONG).show();
                }
                MusicPlayService.getMusicList().remove(i);
                initView();
                adapter.notifyDataSetChanged();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        ab.create();
        ab.show();
    }

    //设置铃声
    public void setMyRingtone(String path)
    {

        ContentValues cv = new ContentValues();
        Uri newUri = null;
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);

        // 查询音乐文件在媒体库是否存在
        Cursor cursor = getContext().getContentResolver().query(uri, null, MediaStore.MediaColumns.DATA + "=?", new String[] { path },null);
        if (cursor.moveToFirst() && cursor.getCount() > 0)
        {
            String _id = cursor.getString(0);
            cv.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            cv.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            cv.put(MediaStore.Audio.Media.IS_ALARM, false);
            cv.put(MediaStore.Audio.Media.IS_MUSIC, false);

            // 把需要设为铃声的歌曲更新铃声库
           getContext().getContentResolver().update(uri, cv, MediaStore.MediaColumns.DATA + "=?",new String[] { path });
            newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            // 以下为关键代码：
            CheckPermission();
            RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, newUri);
            Toast.makeText(getActivity(), "成功设置为铃声", Toast.LENGTH_LONG).show();
        }
    }
//检查权限
    private void CheckPermission() {
        if (!Settings.System.canWrite(getActivity())) {
            //ToastUtil.longTips("请在该设置页面勾选，才可以使用路况提醒功能");
            Uri selfPackageUri = Uri.parse("package:"
                    + getActivity().getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    selfPackageUri);
            startActivity(intent);
        }
    }


    private   void showDialog(int i)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        MusicInfoBean mMusicInfoBean=MusicPlayService.getMusicList().get(i);
        View view = View.inflate(getActivity(), R.layout.custom_dialog, null);
        builder.setView(view);
        builder.setCancelable(true);

        TextView  title= (TextView) view.findViewById(R.id.title);//歌曲名称
        TextView  artist= (TextView) view.findViewById(R.id.aritist);//歌手
        TextView  duration= (TextView) view.findViewById(R.id.duration);//时长
        TextView  url= (TextView) view.findViewById(R.id.url);//歌曲路径
        TextView  file_name= (TextView) view.findViewById(R.id.file_name);//文件名称
        TextView  cover_url= (TextView) view.findViewById(R.id.cover_url);//专辑封面路径
        TextView  file_size= (TextView) view.findViewById(R.id.file_size);// 文件大小
        TextView  year= (TextView) view.findViewById(R.id.year);//发行年份
        TextView   lrc_url=(TextView)view.findViewById(R.id.lrc_url);//歌词路径
        Button btn_cancel=(Button)view
                .findViewById(R.id.btn_cancel);//取消按钮

        //取消或确定按钮监听事件处理
        final AlertDialog dialog = builder.create();

        title.setText(mMusicInfoBean.getTitle());
        artist.setText(mMusicInfoBean.getArtist());
        duration.setText(PlayFragment.formatTime(mMusicInfoBean.getDuration()));
        url.setText(mMusicInfoBean.getUri());
        file_name.setText(mMusicInfoBean.getFileName());
        cover_url.setText(mMusicInfoBean.getCoverUri());
        file_size.setText(fortmatesize(mMusicInfoBean.getFileSize()));
         year.setText(mMusicInfoBean.getYear());
         lrc_url.setText(mMusicInfoBean.getLrcLink());


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    public String fortmatesize(long size)
    {
        String M=size/(1024*1024)+"";
        String k=size%(1024*1024)+"";
        Double K=Double.parseDouble(k)/(1024*1024);


        return M+"."+K.toString().trim().substring(2,4)+"M";
    }
}

