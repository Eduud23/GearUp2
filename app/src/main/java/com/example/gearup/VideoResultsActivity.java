package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VideoResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_results);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String youtubeUrl = getIntent().getStringExtra("youtubeUrl");


        if (youtubeUrl != null) {
            new FetchVideoDataTask(recyclerView).execute(youtubeUrl);
        } else {
            Toast.makeText(this, "No URL provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class FetchVideoDataTask extends AsyncTask<String, Void, String> {
        private RecyclerView recyclerView;

        public FetchVideoDataTask(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String videoData) {
            if (videoData != null) {
                List<VideoItem> videoList = parseVideoData(videoData);
                VideoAdapter adapter = new VideoAdapter(videoList, video -> {
                    String youtubeUrl = "https://www.youtube.com/watch?v=" + video.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(VideoResultsActivity.this, "Failed to load videos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<VideoItem> parseVideoData(String videoData) {
        List<VideoItem> videoList = new ArrayList<>();
        if (videoData == null || videoData.isEmpty()) return videoList;

        try {
            JSONObject jsonObject = new JSONObject(videoData);
            JSONArray videos = jsonObject.getJSONArray("videos");
            for (int i = 0; i < videos.length(); i++) {
                JSONObject video = videos.getJSONObject(i);
                String title = video.getString("title");
                String thumbnail = video.getString("thumbnail");
                String videoId = video.getString("url").split("v=")[1];
                String channelTitle = video.getString("channelTitle"); // New field
                videoList.add(new VideoItem(title, thumbnail, videoId, channelTitle));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videoList;
    }

}
