package com.example.xiamin.musicplayer.Activity.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.xiamin.musicplayer.Activity.MusicActivity;
import com.example.xiamin.musicplayer.Bean.MusicDownLoadBean;
import com.example.xiamin.musicplayer.Bean.MusicInfoBean;
import com.example.xiamin.musicplayer.Bean.OnlineMuiscBean;
import com.example.xiamin.musicplayer.Bean.SongListInfo;
import com.example.xiamin.musicplayer.R;
import com.example.xiamin.musicplayer.adapter.OnlineMusicListAdapter;
import com.example.xiamin.musicplayer.utils.Constants;
import com.example.xiamin.musicplayer.utils.DownloadUtils;
import com.example.xiamin.musicplayer.utils.JsonCallBack.JsonCallBack;
import com.example.xiamin.musicplayer.utils.JsonCallBack.JsonOnlineMusicList;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static android.content.ContentValues.TAG;


public class SearchFragment extends BaseFragment implements View.OnClickListener,
        AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{

     private LinearLayout mLinearLayout;
     private EditText  editText;
     private Button   bt_back;
     private  Button  bt_search;
     private ListView lvSearch;




    private View vHeader;
    private View mView;
    private SongListInfo mListInfo;
    private List<OnlineMuiscBean> mSearchMusicList;
    private JsonOnlineMusicList mJsonList;



    private OnlineMusicListAdapter mAdapter;


    @Override
    public void initView() {

        bt_back.setOnClickListener(this);
        bt_search.setOnClickListener(this);

        mSearchMusicList = new ArrayList<OnlineMuiscBean>();
        mAdapter = new OnlineMusicListAdapter(getContext(), mSearchMusicList);
        lvSearch.setAdapter(mAdapter);

        lvSearch.setOnItemClickListener(this);
        lvSearch.setOnItemLongClickListener(this);

    }
    View view;



    @Override
    public void onResume() {
        super.onResume();
        log("onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        log("onCreateView");

        view = inflater.inflate(R.layout.fragment_search, container, false);
        bt_back = (Button) view.findViewById(R.id.back);
        bt_search = (Button) view.findViewById(R.id.bt_search);
        editText=(EditText)view.findViewById(R.id.et_seach);
        mLinearLayout=(LinearLayout)view.findViewById(R.id.music_list);
        lvSearch=(ListView) view.findViewById(R.id.lv_search_music_list);
        return view;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back: {
                log("onClick R.id.bar_iv_menu");
                hideThis();
                break;
            }
            case R.id.bt_search:{
                searchMusic();
                //book();
                mAdapter.notifyDataSetChanged();
                lvSearch.setAdapter(mAdapter);
                break;
            }

        }
    }

    private  String searchResponse=null;
    private  void searchMusic()
    {
         String key=editText.getText().toString();
         if(key.equals(""))
         {
             Toast.makeText(getContext(), "输入不能为空", Toast.LENGTH_SHORT).show();
         }
         else
         {
           String url="http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.6.5.0&method=baidu.ting.search.catalogSug&format=json&query="+Encode(key);
             //测试地址
             try {
                 HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
                 conn.setConnectTimeout(5000);
                 //使用缓存提高处理效率
                 BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                 String line = null;
                 StringBuilder sb = new StringBuilder();
                 while ((line = br.readLine()) != null) {
                     sb.append(line);
                 }
                 //网络响应赋值给成员变量searchResponse
                   searchResponse = sb.toString();
                 parseResponse();
                Log.d(TAG, "searchResponse = " + searchResponse);
             } catch (IOException e) {
                 e.printStackTrace();
             }

         }
    }
    private void parseResponse() {
        try {
            JSONObject response = new JSONObject(searchResponse);
          // JSONObject result = response.getJSONObject("song");
            JSONArray songs = response.getJSONArray("song");
            if (mSearchMusicList.size() > 0)
                mSearchMusicList.clear();
            for (int i = 0; i < songs.length(); i++) {
                JSONObject song = songs.getJSONObject(i);
                //获取歌曲名字
                String title = song.getString("songname");
                //获取歌词演唱者
                String artist = song.getString("artistname");


                //获取歌曲专辑图片的url
                String songid = song.getString("songid");
                String big_pic=null;
                String small_pic=null;
                String ablum_title=null;
                String lrclink=null;
                String response_result=connection(songid);
                try {
                      JSONObject responseResult = new JSONObject(response_result);
                      JSONObject result = responseResult.getJSONObject("songinfo");
                       big_pic=result.getString("pic_big");
                       small_pic=result.getString("pic_small");
                      ablum_title=result.getString("album_title");
                       lrclink=result.getString("lrclink");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //保存音乐信息的OnlineMusicBean
                OnlineMuiscBean onlineMuiscBean=new OnlineMuiscBean();
                onlineMuiscBean.setTitle(title);
                onlineMuiscBean.setArtist_name(artist);
                onlineMuiscBean.setSong_id(songid);
                onlineMuiscBean.setPic_big(big_pic);
                onlineMuiscBean.setPic_small(small_pic);
                onlineMuiscBean.setAlbum_title(ablum_title);
                onlineMuiscBean.setLrclink(lrclink);
                //onlineMuiscBean.set
                //将一条歌曲信息存入list中
                mSearchMusicList.add(onlineMuiscBean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

      private  String connection(String  songid)
     {
          String url="http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid="+songid;
        //测试地址
      try {
            HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
           conn.setConnectTimeout(5000);
              //使用缓存提高处理效率
             BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
           while ((line = br.readLine()) != null) {
               sb.append(line);
           }
        //网络响应赋值给成员变量searchResponse
           Log.d(TAG, "searchResponse = " + searchResponse);
            return sb.toString();

      } catch (IOException e) {
             e.printStackTrace();
      }
       return null;
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        log("" + i + " info:" + mSearchMusicList.get(i).getTitle());
        playMusic(mSearchMusicList.get(i));
    }

    private void playMusic(final OnlineMuiscBean onlineMuiscBean) {


        OkHttpUtils.get().url(Constants.BASE_URL)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("Accept-Encoding","gzip, deflate, sdch")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4")
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.110 Safari/537.36")
                .addHeader("Host", "tingapi.ting.baidu.com")
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_DOWNLOAD_MUSIC)
                .addParams(Constants.PARAM_SONG_ID, onlineMuiscBean.getSong_id())
                .build()
                .execute(new JsonCallBack<MusicDownLoadBean>(MusicDownLoadBean.class) {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(MusicDownLoadBean response) {
                        if (response == null) {
                            log("response == null");
                            return;
                        }
                        log("开始播放在线音乐");

                        /*从response中获取信息传入PlayService 并更新playbar*/
                        MusicInfoBean musicInfoBean = new MusicInfoBean();
                        musicInfoBean.setUri(response.getBitrate().getFile_link());
                        musicInfoBean.setDuration(response.getBitrate().getFile_duration());
                        musicInfoBean.setAlbum(onlineMuiscBean.getAlbum_title());
                        musicInfoBean.setTitle(onlineMuiscBean.getTitle());
                        musicInfoBean.setArtist(onlineMuiscBean.getArtist_name());
                        musicInfoBean.setCoverUri(onlineMuiscBean.getPic_big());
                        musicInfoBean.setLrcLink(onlineMuiscBean.getLrclink());
                        musicInfoBean.setType(MusicInfoBean.Type.ONLINE);
                        getPlayService().play(musicInfoBean);
                        ((MusicActivity) getActivity()).setPlayBar(musicInfoBean);
                    }

                });
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l)
    {
        final String items[] = {"下载"};
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                // .setIcon(R.mipmap.icon)//设置标题的图片
                .setTitle("歌曲选项")//设置对话框的标题
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                downloadMusic(mSearchMusicList.get(i));
                                break;

                        }
                    }
                }).create();
        dialog.show();
        return true;
    }

    private void downloadMusic(final OnlineMuiscBean onlineMuiscBean ) {
        DownloadUtils.getsInstance().setListener(new DownloadUtils.OnDownloadListener() {
            @Override
            public void onDowload(String mp3Url) { //下载成功
                Toast.makeText(getContext(), onlineMuiscBean.getTitle()+"下载成功", Toast.LENGTH_SHORT).show();
                String path= Environment.getExternalStorageDirectory()+"/CCMUSIC/"+onlineMuiscBean.getArtist_name()+"-"+onlineMuiscBean.getTitle() + ".mp3";
                MediaScannerConnection.scanFile(getContext(), new String[] {path}, null,null);
            }

            @Override
            public void onFailed(String error) { //下载失败
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();

            }
        }).download(onlineMuiscBean);
    }

    int [] blackid = new int [1000];       //blackid数组
     int length=0;            //blackid数组长度

    private void  book() {
        String url = "http://qfc.qunar.com/homework/id_list.txt";
          getblackid();                   //获取blackid数组
        //测试地址
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            //使用缓存提高处理效率
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
              if(!isblackid(line))
                   jsonresult(line);
            }
            //网络响应赋值给成员变量searchResponse

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  void getblackid()             //获取blackid数组
    {
        String blackurl = "http://qfc.qunar.com/homework/id_blacklist.txt";
        //测试地址
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(blackurl).openConnection();
            conn.setConnectTimeout(5000);
            //使用缓存提高处理效率
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
               blackid[length++]=Integer.valueOf(line).intValue();    //转为整形可以使用二分查找
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean isblackid(String bookid)     //使用二分查找
    {
        int id=Integer.valueOf(bookid).intValue();
        int low=0;
        int high=length-1;
        while(low<=high)
        {
            int mid =(low+high)/2;
            if(blackid[mid]==id)
                return true;
            if(blackid[mid]<id)
            {
               low=mid+1;
            }
            else
            {
                high=mid-1;
            }
        }
        return false;
    }


    private void jsonresult(String bookid) {
            String url="https://api.douban.com/v2/book/"+bookid;
            //测试地址
            try {
                HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
                conn.setConnectTimeout(5000);
                //使用缓存提高处理效率
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                searchResponse=sb.toString();
                //网络响应赋值给成员变量searchResponse
                Log.d(TAG, "searchResponse = " + searchResponse);
                getresult();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    private void getresult() {
        try {
               JSONObject response = new JSONObject(searchResponse);
                //获取歌曲名字
            String title="";
            String author="";
            String summary="";
            String publish="";
            try {
                 title = response.getString("title");
                //获取歌词演唱者
                author = response.getString("author");
                 summary = response.getString("summary");
                publish = response.getString("publisher");
            }
            catch (Exception e)
            {

            }

            try {
                File lrcDirFile = new File(Environment.getExternalStorageDirectory() + "/CCMUSIC");
                if (!lrcDirFile.exists()){
                    lrcDirFile.mkdirs();
                }
                String target = lrcDirFile + "/" +  "result.txt";
                File tempfile = new File(target);
                if(!tempfile.exists())
                {
                    tempfile.createNewFile();
                }

                try {
                    String filein = "书名："+title+"\r\n"+"作者："+author+"\r\n"+"简介："+summary+"\r\n"+"出版社："+publish+"\r\n";//新写入的行，换行
                    String temp  = "";

                    FileInputStream fis = null;
                    InputStreamReader isr = null;
                    BufferedReader br = null;
                    FileOutputStream fos  = null;
                    PrintWriter pw = null;
                    try {

                        File file = new File(target);//文件路径(包括文件名称)

                        //将文件读入输入流
                        fis = new FileInputStream(file);
                        isr = new InputStreamReader(fis);
                        br = new BufferedReader(isr);
                        StringBuffer buffer = new StringBuffer();

                        //文件原有内容
                        for(int i=0;(temp =br.readLine())!=null;i++){
                            buffer.append(temp);
                            // 行与行之间的分隔符 相当于“\n”
                            buffer = buffer.append(System.getProperty("line.separator"));
                        }
                        buffer.append(filein);

                        fos = new FileOutputStream(file);
                        pw = new PrintWriter(fos);
                        pw.write(buffer.toString().toCharArray());
                        pw.flush();

                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }finally {
                        //不要忘记关闭
                        if (pw != null) {
                            pw.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                        if (br != null) {
                            br.close();
                        }
                        if (isr != null) {
                            isr.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                    }

                }catch (IOException e){
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


