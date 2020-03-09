package com.example.xiamin.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xiamin.musicplayer.Bean.MusicInfoBean;
import com.example.xiamin.musicplayer.R;
import com.example.xiamin.musicplayer.Service.MusicPlayService;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Xiamin on 2016/9/16.
 */
public class LocalMusicAdapter extends BaseAdapter {


    @Override
    public int getCount() {
        return MusicPlayService.getMusicList().size();
    }

    @Override
    public Object getItem(int i) {
        return MusicPlayService.getMusicList().get(i);
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
        final MusicInfoBean music = MusicPlayService.getMusicList().get(i);
        holder.tvTitle.setText(music.getTitle());
        holder.tvArtist.setText(music.getArtist());
        holder.vDivider.setVisibility(isShowDivider(i) ? View.VISIBLE : View.GONE);
        /**
         * Glide加载图片，就是这么简单。。
         */
        Glide.with(viewGroup.getContext())
                .load(music.getCoverUri())
                .error(R.drawable.default_cover)
                .into(holder.ivCover);
        return view;
    }

    private boolean isShowDivider(int position) {
        return position != MusicPlayService.getMusicList().size() - 1;
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
