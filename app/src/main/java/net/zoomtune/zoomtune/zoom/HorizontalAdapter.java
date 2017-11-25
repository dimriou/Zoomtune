package net.zoomtune.zoomtune.zoom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.zoomtune.zoomtune.R;

import java.util.List;


public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {
    private List<Sound> horizontalList;
    private Context ctx;
    private int pos = -1;


    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;

        MyViewHolder(View view) {
            super(view);
            imgView = view.findViewById(R.id.imgView);
        }
    }


    HorizontalAdapter(List<Sound> horizontalList, Context ctx) {
        this.horizontalList = horizontalList;
        this.ctx = ctx;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.horizontal_adapter_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Drawable icon = ResourcesCompat.getDrawable(ctx.getResources(),
                horizontalList.get(position).id, null);
        holder.imgView.setImageDrawable(icon);
        holder.imgView.setBackgroundResource(R.drawable.ripple);
        holder.imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ZoomActivity)ctx).setTitle(horizontalList.get(position).name);
                if (pos != position) {
                    stopPlaying();
                    int id = ctx.getResources().getIdentifier(horizontalList.get(position).name.toLowerCase()
                            , "raw", ctx.getPackageName());
                    ZoomActivity.mPlayer = null;
                    ZoomActivity.mPlayer = MediaPlayer.create(ctx, id);
                    ZoomActivity.mPlayer.start();
                    pos = position;
                }
                else {
                    if (ZoomActivity.mPlayer.isPlaying()){
                        stopPlaying();
                    }
                    else {
                        int id = ctx.getResources().getIdentifier(horizontalList.get(position).name.toLowerCase()
                                , "raw", ctx.getPackageName());
                        ZoomActivity.mPlayer = null;
                        ZoomActivity.mPlayer = MediaPlayer.create(ctx, id);
                        ZoomActivity.mPlayer.start();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return horizontalList.size();
    }

    private void stopPlaying() {
        if (ZoomActivity.mPlayer != null) {
            ZoomActivity.mPlayer.stop();
        }
    }
}
