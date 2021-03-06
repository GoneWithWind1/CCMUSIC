package com.example.xiamin.musicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xiamin.musicplayer.Bean.OnlineMuiscBean;
import com.example.xiamin.musicplayer.R;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Xiamin on 2016/9/22.
 */
public class OnlineMusicListAdapter extends BaseAdapter{
    private List<OnlineMuiscBean> mData;
    private Context mContext;
    private String mType;

    public OnlineMusicListAdapter(Context context, List<OnlineMuiscBean> data)
    {
        this.mData = data;
        this.mContext = context;
    }

    public OnlineMusicListAdapter(Context context, String type)
    {
        this.mType = type;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_music, viewGroup, false);
            holder = new ViewHolder(view);
            holder.vPlaying=(View)view.findViewById(R.id.v_playing);
            holder.ivCover=(ImageView) view.findViewById(R.id.iv_cover);
            holder.tvTitle=(TextView)view.findViewById(R.id.tv_title);
            holder.tvArtist=(TextView)view.findViewById(R.id.tv_artist);
            holder.ivMore=(ImageView)view.findViewById(R.id.iv_more);
            holder.vDivider=(View)view.findViewById(R.id.v_divider);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        OnlineMuiscBean jOnlineMusic = mData.get(i);
        Log.i("iii",jOnlineMusic.getTitle() + jOnlineMusic.getArtist_name());
        Glide.with(mContext)
                .load(jOnlineMusic.getPic_small())
                .error(R.drawable.default_cover)
                .into(holder.ivCover);
        holder.tvTitle.setText(jOnlineMusic.getTitle());
        holder.tvArtist.setText(jOnlineMusic.getArtist_name());

        return view;
    }


    class ViewHolder {
        public View vPlaying;
        public ImageView ivCover;
        public TextView tvTitle;
        public TextView tvArtist;
        public ImageView ivMore;
        public View vDivider;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
