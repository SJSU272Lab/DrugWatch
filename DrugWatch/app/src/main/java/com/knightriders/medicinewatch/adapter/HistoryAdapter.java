package com.knightriders.medicinewatch.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.knightriders.medicinewatch.HistoryActivity;
import com.knightriders.medicinewatch.R;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private List<History> historyList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView number, ndc, status;
        public ImageView image;
        ProgressBar pBar;

        MyViewHolder(View view) {
            super(view);
            ndc = (TextView) view.findViewById(R.id.scanNdc);
            number = (TextView) view.findViewById(R.id.scanNumber);
            status = (TextView) view.findViewById(R.id.scanStatus);
            image = (ImageView) view.findViewById(R.id.scanImage);
            pBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }


    public HistoryAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        History history = historyList.get(position);
        holder.ndc.setText("NDC: " + history.getNdc());
        holder.number.setText("Recall Number: " + history.getNumber());
        holder.status.setText("Recall Status: " + history.getStatus());

        Glide.with(context)
                .load(getResId(history.getImage(), R.drawable.class))
                .listener(new RequestListener<Integer, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.pBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
