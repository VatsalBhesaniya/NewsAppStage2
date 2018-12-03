package com.example.android.newsappstage2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private Context mContext;
    private List<News> mNewsSectionsList;

    public NewsAdapter(Context mContext, List<News> mNewsSectionsList) {
        this.mContext = mContext;
        this.mNewsSectionsList = mNewsSectionsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mNewsSectionsList.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final News currentPlace = mNewsSectionsList.get(position);

        PreferenceManager.setDefaultValues(mContext, R.xml.settings_main, false);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean thumbnail = sharedPrefs.getBoolean(mContext.getString(R.string.settings_thumbnails_key), mContext.getResources().getBoolean(R.bool.settings_thumbnails_checked));
        String url = currentPlace.getImageUrl();
        if (thumbnail & !TextUtils.isEmpty(url)) {
            new DownloadImageTask(holder.image).execute(url);
        } else {
            holder.image.setVisibility(View.GONE);
            holder.newsTitle.setTextColor(mContext.getResources().getColor(R.color.colorBlueGrey));
        }
        holder.sectionName.setText(currentPlace.getSectionName());
        holder.newsTitle.setText(currentPlace.getNewsTitle());
        holder.newsText.setText(currentPlace.getNewsText());
        holder.authorName.setText(currentPlace.getAuthorName());
        // Converting UTC timezone date string into local timezine date string.
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateInString = currentPlace.getDatePublished();
        Date date = null;
        try {
            date = formatter.parse(dateInString.replaceAll("Z$", "+0000"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, LLL dd, yyyy");
        dateFormat.setTimeZone(TimeZone.getDefault());
        String datePublished = dateFormat.format(date);
        holder.datePublished.setText(datePublished);
        // OnClickListener to open full article in web browser on list item click.
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String webUrl = currentPlace.getmWebUrl();
                Intent newsIntent = new Intent(Intent.ACTION_VIEW);
                newsIntent.setData(Uri.parse(webUrl));
                mContext.startActivity(newsIntent);
            }
        });
    }

    public List<News> updateData(List<News> news) {
        this.mNewsSectionsList = news;
        if (news != null) {
            this.notifyDataSetChanged();
        }
        return news;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView image;
        private TextView sectionName;
        private TextView newsTitle;
        private TextView newsText;
        private TextView authorName;
        private TextView datePublished;

        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            image = view.findViewById(R.id.image);
            sectionName = view.findViewById(R.id.section_name);
            newsTitle = view.findViewById(R.id.news_title);
            newsText = view.findViewById(R.id.news_text);
            authorName = view.findViewById(R.id.author_name);
            datePublished = view.findViewById(R.id.date_published);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}

