package com.matti.finalproject;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MarkersAdapter extends RecyclerView.Adapter<MarkersAdapter.ViewHolder> {

    // Member variables.
    private ArrayList<Marker> mMarkersData;
    private Context mContext;

    MarkersAdapter(Context context, ArrayList<Marker> markersData) {
        this.mMarkersData = markersData;
        this.mContext = context;
    }



    @Override
    public MarkersAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_item, parent, false);
        MarkersAdapter.ViewHolder ViewHolder = new MarkersAdapter.ViewHolder(listItem);
        return ViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Marker marker = mMarkersData.get(position);
        holder.titleView.setText(mMarkersData.get(position).getTitle());
        holder.snippetView.setText(mMarkersData.get(position).getSnippet());
        holder.longitudeView.setText("Longitude : " + mMarkersData.get(position).getLongitude());
        holder.latitudeView.setText("Latitude : " + mMarkersData.get(position).getLatitude());
    }


    @Override
    public int getItemCount() {
        return mMarkersData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView;
        public TextView snippetView;
        public TextView longitudeView;
        public TextView latitudeView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.titleView = (TextView) itemView.findViewById(R.id.titleListItem);
            this.snippetView = (TextView) itemView.findViewById(R.id.snippetListItem);
            this.longitudeView = (TextView) itemView.findViewById(R.id.longitudeListItem);
            this.latitudeView = (TextView) itemView.findViewById(R.id.latitudeListItem);
        }
    }


//    class ViewHolder extends RecyclerView.ViewHolder
//            implements View.OnClickListener{
//
//        // Member Variables for the TextViews
//        private TextView mTitleText;
//        private TextView mInfoText;
//        private ImageView mSportsImage;
//
//        /**
//         * Constructor for the ViewHolder, used in onCreateViewHolder().
//         *
//         * @param itemView The rootview of the list_item.xml layout file.
//         */
//        ViewHolder(View itemView) {
//            super(itemView);
//
//            // Initialize the views.
//            mTitleText = itemView.findViewById(R.id.title);
//            mInfoText = itemView.findViewById(R.id.subTitle);
//            mSportsImage = itemView.findViewById(R.id.sportsImage);
//
//            // Set the OnClickListener to the entire view.
//            itemView.setOnClickListener(this);
//        }
//
//        void bindTo(Sport currentSport){
//            // Populate the textviews with data.
//            mTitleText.setText(currentSport.getTitle());
//            mInfoText.setText(currentSport.getInfo());
//
//            // Load the images into the ImageView using the Glide library.
//            Glide.with(mContext).load(
//                    currentSport.getImageResource()).into(mSportsImage);
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onClick(View view) {
//            Sport currentSport = mMarkersData.get(getAdapterPosition());
//            Intent detailIntent = new Intent(mContext, DetailActivity.class);
//            detailIntent.putExtra("title", currentSport.getTitle());
//            detailIntent.putExtra("image_resource",
//                    currentSport.getImageResource());
//
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            {
//                ActivityOptions options = ActivityOptions
//                        .makeSceneTransitionAnimation((Activity) mContext, mSportsImage, "SportsImage");
//
//                mContext.startActivity(detailIntent, options.toBundle());
//            }
//            else
//            {
//                mContext.startActivity(detailIntent);
//            }
//        }
//    }

}