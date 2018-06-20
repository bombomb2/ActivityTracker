package com.dasom.activitytracker;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
    ArrayList<StatItem> mItems;

    public RecyclerAdapter(ArrayList<StatItem> items){
        mItems = items;
    }


    // 새로운 뷰 홀더 생성
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view,parent,false);
        return new ItemViewHolder(view);
    }


    // View 의 내용을 해당 포지션의 데이터로 바꿉니다.
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.tv_startTime.setText(mItems.get(position).getStartTime());
        holder.tv_endTime.setText(mItems.get(position).getEndTime());
        holder.tv_stats.setText(mItems.get(position).getStats());
        holder.tv_location.setText(mItems.get(position).getLocation());
        if (mItems.get(position).isStay()) { //체류
            holder.img_circle.setColorFilter(Color.parseColor("#ffc0cb"));
            holder.view.setBackgroundColor(Color.parseColor("#80ffffff"));

        }
        else {

            holder.img_circle.setColorFilter(Color.parseColor("#FFFCAB53"));
            holder.view.setBackgroundColor(Color.parseColor("#FF50D2C2"));
        }
        }


    // 데이터 셋의 크기를 리턴해줍니다.
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    // 커스텀 뷰홀더
    // item layout 에 존재하는 위젯들을 바인딩합니다.
    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_startTime;
        private TextView tv_endTime;
        private TextView tv_stats;
        private TextView tv_location;
        private ImageView img_circle;
        private View view;
        public ItemViewHolder(View itemView) {
            super(itemView);
            tv_startTime  = (TextView) itemView.findViewById(R.id.tv_startTime);
            tv_endTime = (TextView) itemView.findViewById(R.id.tv_endTime);
            tv_stats  = (TextView)itemView.findViewById(R.id.tv_stats);
            tv_location  =(TextView)itemView.findViewById(R.id.tv_location);
            img_circle = (ImageView)itemView.findViewById(R.id.img_circle);
            view = (View) itemView.findViewById(R.id.view2);
        }
    }
}