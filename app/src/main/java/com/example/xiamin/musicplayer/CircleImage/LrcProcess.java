package com.example.xiamin.musicplayer.CircleImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cqb on 2018/5/21.
 */
public class LrcProcess {
    private List<LrcContent> lrcList; //List集合存放歌词内容对象
    private LrcContent mLrcContent;     //声明一个歌词内容对象
    /**
     * 无参构造函数用来实例化对象
     */
    public LrcProcess() {
        mLrcContent = new LrcContent();
        lrcList = new ArrayList<LrcContent>();
    }

    /**
     * 读取歌词
     * @param path
     * @return
     */
    public String readLRC(String path) {
        //定义一个StringBuilder对象，用来存放歌词内容
        StringBuilder stringBuilder = new StringBuilder();
        File f = new File(path.replace(".mp3", ".lrc"));

        try {
            //创建一个文件输入流对象
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String s = "";
            while((s = br.readLine()) != null) {
                //替换字符
                s = s.replace("[", "");
                s = s.replace("]", "@");

                //分离“@”字符
                String splitLrcData[] = s.trim().split("@");
                if (splitLrcData.length > 1&&splitLrcData.length <3) {
                    mLrcContent.setLrcStr(splitLrcData[1]);

                    //处理歌词取得歌曲的时间
                    int lrcTime = time2Str(splitLrcData[0]);

                    mLrcContent.setLrcTime(lrcTime);

                    //添加进列表数组
                    lrcList.add(mLrcContent);

                    //新创建歌词内容对象
                }
                else if(splitLrcData.length >2)
                {
                    mLrcContent.setLrcStr(splitLrcData[splitLrcData.length-1]);
                    int lrcTime1= time2Str(splitLrcData[0]);
                    mLrcContent.setLrcTime(lrcTime1);
                    lrcList.add(mLrcContent);
                }
                mLrcContent = new LrcContent();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            stringBuilder.append("木有歌词文件，赶紧去下载!");
        } catch (IOException e) {
            e.printStackTrace();
            stringBuilder.append("木有读取到歌词哦！");
        }
        return stringBuilder.toString();
    }



    public String  getURLLrc (String path)
    {
            StringBuilder stringBuilder = new StringBuilder();
               try {
                   URL url = new URL(path);
                   HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                   urlConn.setConnectTimeout(5 * 1000);
                  urlConn.connect();
                   // 判断请求是否成功
                   if (urlConn.getResponseCode() == 200) {
                       // 获取响应的输入流对象
                       InputStream is = urlConn.getInputStream();
                       InputStreamReader isr = new InputStreamReader(is, "utf-8");
                       BufferedReader br = new BufferedReader(isr);
                       String s = "";
                       while ((s = br.readLine()) != null) {
                           //替换字符
                           s = s.replace("[", "");
                           s = s.replace("]", "@");

                           //分离“@”字符
                           String splitLrcData[] = s.trim().split("@");
                           if (splitLrcData.length > 1&&splitLrcData.length<3) {
                               mLrcContent.setLrcStr(splitLrcData[1]);

                               //处理歌词取得歌曲的时间
                               int lrcTime = time2Str(splitLrcData[0]);

                               mLrcContent.setLrcTime(lrcTime);

                               //添加进列表数组
                               lrcList.add(mLrcContent);

                               //新创建歌词内容对象
                           }
                           else if(splitLrcData.length >2)
                           {
                               mLrcContent.setLrcStr(splitLrcData[splitLrcData.length-1]);
                               int lrcTime1= time2Str(splitLrcData[0]);
                               mLrcContent.setLrcTime(lrcTime1);
                               lrcList.add(mLrcContent);
                           }
                           mLrcContent = new LrcContent();
                       }
                   }
                   urlConn.disconnect();
               }
               catch (FileNotFoundException e)
               {
                   e.printStackTrace();
                   stringBuilder.append("链接失败！");
               }
               catch (IOException e) {
                   e.printStackTrace();
                   stringBuilder.append("暂无歌词！");
               }

        return stringBuilder.toString();
    }





    /**
     * 解析歌词时间
     * 歌词内容格式如下：
     * [00:02.32]陈奕迅
     * [00:03.43]好久不见
     * [00:05.22]歌词制作  王涛
     * @param timeStr
     * @return
     */
    public int time2Str(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");

        String timeData[] = timeStr.split("@"); //将时间分隔成字符串数组
        //分离出分、秒并转换为整型
            int minute = Integer.parseInt(timeData[0]);
            int second = Integer.parseInt(timeData[1]);
            int millisecond = Integer.parseInt(timeData[2]);
            //计算上一行与下一行的时间转换为毫秒数
            int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
            return currentTime;
    }
    public List<LrcContent> getLrcList() {
        return lrcList;
    }
}
