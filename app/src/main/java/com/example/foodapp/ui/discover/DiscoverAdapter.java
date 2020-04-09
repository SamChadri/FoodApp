package com.example.foodapp.ui.discover;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.foodapp.utils.SampleDiscover;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import static com.example.foodapp.MainActivity.TAG;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.DiscoverViewHolder> {

    public ArrayList<SampleDiscover> data;

    public static class DiscoverViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView description;
        public ImageView image;

        public DiscoverViewHolder(View v){
            super(v);
            title = v.findViewById(R.id.discoverCardTitle);
            description = v.findViewById(R.id.discoverCardDescription);
            image = v.findViewById(R.id.discoverCardImageView);

        }
    }

    public DiscoverAdapter(ArrayList<SampleDiscover> data){
        this.data = data;
    }

    @Override
    public DiscoverViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discover_card_layout, parent, false);
        return new DiscoverViewHolder(view);

    }

    @Override
    public void onBindViewHolder(DiscoverViewHolder viewHolder, int position){
        if(data.get(position).imageUrl != null ){
            new DownloadImageTask(viewHolder.image).execute(data.get(position).imageUrl);
        }

        viewHolder.title.setText(data.get(position).title);
        //viewHolder.description.setText(data.get(position).description);
    }

    @Override
    public int getItemCount(){
        return data.size();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public ImageView view;
        public DownloadImageTask(ImageView view){
            this.view = view;
        }

        @Override
        protected Bitmap doInBackground(String... args){
            String url = args[0];
            Bitmap image = null;
            try{
                InputStream stream = new URL(url).openStream();
                image = BitmapFactory.decodeStream(stream);

            }catch (IOException e){
                Log.d(TAG,"DownloadImageTask() - Error occurred: " + e);
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            int displayWidth = DeviceDimensionsHelper.getDisplayWidth(view.getContext());
            int imageWidth = (int)DeviceDimensionsHelper.convertDpToPixel(350, view.getContext());
            int imageHeight = (int)DeviceDimensionsHelper.convertDpToPixel(250, view.getContext());
            Bitmap scaledResult = Bitmap.createScaledBitmap(result,imageWidth, imageHeight, true);
            this.view.setImageBitmap(scaledResult);
        }

        private Bitmap scaleImage(Bitmap bm, int width, int height){
            float widthRatio = bm.getWidth() / (float) width;
            float heightRatio = bm.getHeight() / (float) height;
            return Bitmap.createScaledBitmap(bm, (int)(bm.getWidth() *widthRatio), (int)(bm.getHeight() * heightRatio), true);
        }
    }
}
