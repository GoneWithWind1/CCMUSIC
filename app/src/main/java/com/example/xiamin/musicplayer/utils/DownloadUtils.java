package com.example.xiamin.musicplayer.utils;

import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.example.xiamin.musicplayer.Bean.MusicDownLoadBean;
import com.example.xiamin.musicplayer.Bean.MusicInfoBean;
import com.example.xiamin.musicplayer.Bean.OnlineMuiscBean;
import com.example.xiamin.musicplayer.utils.JsonCallBack.JsonCallBack;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by cqb on 2018/5/31.
 */

public class DownloadUtils {
    private static final int SUCCESS_LRC = 1;//下载歌词成功
    private static final int FAILED_LRC = 2;//下载歌词失败
    private static final int SUCCESS_MP3 = 3;//下载歌曲成功
    private static final int FAILED_MP3 = 4;//下载歌曲失败
    private static final int GET_MP3_URL = 5;//获取音乐下载地址成功
    private static final int GET_FAILED_MP3_URL = 6;//获取音乐下载地址失败
    private static final int MUSIC_EXISTS = 7;//下载时,音乐已存在
    private static final int SUCCESS_PIC = 8;//下载歌词成功
    private static final int FAILED_PIC = 9;//下载歌词成功


    private static DownloadUtils sInstance;
    private OnDownloadListener mListener;

    private ExecutorService mThreadPool;



    public DownloadUtils setListener(OnDownloadListener mListener){
        this.mListener = mListener;
        return this;
    }

    //获取下载工具的实例
    public synchronized static DownloadUtils getsInstance(){
        if (sInstance == null){
            try {
                sInstance = new DownloadUtils();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return  sInstance;
    }

    /**
     * 下载的具体业务方法
     * @throws
     */
    private DownloadUtils() throws ParserConfigurationException{
        mThreadPool = Executors.newSingleThreadExecutor();
    }
    public void download(final OnlineMuiscBean searchResult){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case SUCCESS_PIC:
                        if (mListener != null) mListener.onDowload("图片下载成功");
                        break;
                    case FAILED_PIC:
                        if (mListener != null) mListener.onDowload("图片下载失败");
                        break;
                    case SUCCESS_LRC:
                        if (mListener != null) mListener.onDowload("歌词下载成功");
                        break;
                    case FAILED_LRC:
                        if (mListener != null) mListener.onFailed("歌词下载失败");
                        break;
                    case GET_MP3_URL:
                        System.out.println("GET_MP3_URL:"+msg.obj);
                        downloadMusic(searchResult,(String)msg.obj,this);
                        break;
                    case GET_FAILED_MP3_URL:
                        if (mListener != null) mListener.onFailed("下载失败,该歌曲为收费或VIP类型");
                        break;
                    case SUCCESS_MP3:
                        if (mListener != null) mListener.onDowload(searchResult.getTitle()+"已经下载");
                        String url =searchResult.getLrclink();
                       System.out.println("download lrc:"+url);
                       downloadLRC(url,searchResult.getTitle(),searchResult.getArtist_name(),this);
                       downloadMusicCover(searchResult,this);
                        break;
                    case FAILED_MP3:
                        if (mListener != null) mListener.onFailed(searchResult.getTitle()+"下载失败");
                        break;
                    case MUSIC_EXISTS:
                        if (mListener != null) mListener.onFailed("音乐已存在");
                        break;
                }
            }
        };
        getDownloadMusicURL(searchResult,handler);
    }

    private void downloadLRC(final String lrcPath, final String musicName,final  String musicAritst,final Handler handler){
        mThreadPool.execute(new Runnable() {
            @Override
                public void run() {
                    try {
                        File lrcDirFile = new File(Environment.getExternalStorageDirectory() + "/CCMUSIC");
                        if (!lrcDirFile.exists()){
                            lrcDirFile.mkdirs();
                        }

                        String target = lrcDirFile + "/" + musicAritst+"-"+musicName + ".lrc";
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(lrcPath).build();
                        try {
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()){
                                PrintStream ps = new PrintStream(new File(target));
                                byte[] bytes = response.body().bytes();
                                ps.write(bytes,0,bytes.length);
                                ps.close();
                                handler.obtainMessage(SUCCESS_LRC,target).sendToTarget();
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                            handler.obtainMessage(FAILED_LRC).sendToTarget();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    private void getDownloadMusicURL(final OnlineMuiscBean searchResult, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {   OkHttpUtils.get().url(Constants.BASE_URL)

                        .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .addHeader("Accept-Encoding","gzip, deflate, sdch")
                        .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4")
                        .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.110 Safari/537.36")
                        .addHeader("Host", "tingapi.ting.baidu.com")
                        .addParams(Constants.PARAM_METHOD, Constants.METHOD_DOWNLOAD_MUSIC)
                        .addParams(Constants.PARAM_SONG_ID, searchResult.getSong_id())
                        .build()
                        .execute(new JsonCallBack<MusicDownLoadBean>(MusicDownLoadBean.class) {
                            @Override
                            public void onError(Call call, Exception e) {

                            }
                            @Override
                            public void onResponse(MusicDownLoadBean response) {
                                if (response == null) {

                                    return;
                                }
                                Message msg = handler.obtainMessage(GET_MP3_URL,response.getBitrate().getFile_link());
                                msg.sendToTarget();
                            }
                        });
                } catch (Exception e) {
                      e.printStackTrace();
                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                }
            }
        });
    }
//下载MP3
    private void downloadMusic(final OnlineMuiscBean searchResult,final String url,final Handler handler){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = new File(Environment.getExternalStorageDirectory()+"/CCMUSIC");
                if (!musicDirFile.exists()){
                    musicDirFile.mkdirs();
                }
                String mp3url = url;
                String target = musicDirFile + "/" + searchResult.getArtist_name()+"-"+searchResult.getTitle() + ".mp3";
                System.out.println(mp3url);
                System.out.println(target);
                File fileTarget = new File(target);
                if (fileTarget.exists()){
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                }else {
                    //使用OkHttpClient组件
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(mp3url).build();
                    System.out.println(request);
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()){
                            PrintStream ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes,0,bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3).sendToTarget();
                    }
                }
            }
        });
    }

    //下载专辑图片
    private void downloadMusicCover(final OnlineMuiscBean searchResult,final Handler handler){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = new File(Environment.getExternalStorageDirectory()+"/CCMUSIC");
                if (!musicDirFile.exists()){
                    musicDirFile.mkdirs();
                }
                String coverurl = searchResult.getPic_big();
                String target = musicDirFile + "/" + searchResult.getArtist_name()+"-"+searchResult.getTitle() + ".jpg";

                File fileTarget = new File(target);
                if (fileTarget.exists()){
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                   return;
                }else {
                    //使用OkHttpClient组件
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(coverurl).build();
                    System.out.println(request);
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()){
                            PrintStream ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes,0,bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_PIC).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_PIC).sendToTarget();
                    }
                }

            }
        });
    }






    //自定义下载事件监听器
    public interface OnDownloadListener {
        public void onDowload(String mp3Url);
        public void onFailed(String error);
    }
}
