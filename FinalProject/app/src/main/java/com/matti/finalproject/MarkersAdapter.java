package com.matti.finalproject;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MarkersAdapter extends RecyclerView.Adapter<MarkersAdapter.ViewHolder> {

    // Member variables.
    private ArrayList<Marker> mMarkersData;
    private Context mContext;
    private int mDataNumber;

    MarkersAdapter(Context context, ArrayList<Marker> markersData, int DataNumber) {
        this.mMarkersData = markersData;
        this.mContext = context;
        this.mDataNumber = DataNumber;
    }

    @Override
    public MarkersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_item, parent, false);
        MarkersAdapter.ViewHolder ViewHolder = new MarkersAdapter.ViewHolder(listItem);
        return ViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Marker currentMarker = mMarkersData.get(position);
        holder.bindTo(currentMarker);
    }

    @Override
    public int getItemCount() {
        return mMarkersData.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        // Member Variables for the TextViews
        public TextView titleView;
        public TextView snippetView;
        public TextView longitudeView;
        public TextView latitudeView;
        public TextView modeView;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         *
         * @param itemView The rootview of the list_item.xml layout file.
         */
        ViewHolder(View itemView) {
            super(itemView);
            // Initialize the views.
            this.titleView = (TextView) itemView.findViewById(R.id.titleListItem);
            this.snippetView = (TextView) itemView.findViewById(R.id.snippetListItem);
            this.longitudeView = (TextView) itemView.findViewById(R.id.longitudeListItem);
            this.latitudeView = (TextView) itemView.findViewById(R.id.latitudeListItem);
            this.modeView = (TextView) itemView.findViewById(R.id.modeListItem);

            // Set the OnClickListener to the entire view.
            itemView.setOnClickListener(this);
        }

        void bindTo(Marker currentMarker){
            // Populate the textviews with data.
            titleView.setText(currentMarker.getTitle());
            snippetView.setText(currentMarker.getSnippet());
            longitudeView.setText(currentMarker.getLongitude());
            latitudeView.setText(currentMarker.getLatitude());
            modeView.setText(currentMarker.getMode());
        }

        @Override
        public void onClick(View view) {
            if (mDataNumber != 0) {
                Marker currentMarker = mMarkersData.get(getAdapterPosition());
                Intent intent = new Intent(mContext, EditMarkers.class);
                intent.putExtra("title", currentMarker.getTitle());
                mContext.startActivity(intent);
            }
        }
    }
}