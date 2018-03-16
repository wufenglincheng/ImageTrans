package com.demo.imagetransdemo.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.demo.imagetransdemo.MyApplication;
import com.demo.imagetransdemo.R;
import com.demo.imagetransdemo.adapter.CustomTransform;
import com.demo.imagetransdemo.adapter.MyImageLoad;
import com.demo.imagetransdemo.view.SpaceDecoration;

import java.util.ArrayList;
import java.util.List;

import it.liuting.imagetrans.ImageTrans;
import it.liuting.imagetrans.ScaleType;
import it.liuting.imagetrans.listener.SourceImageViewGet;

/**
 * Created by liuting on 17/6/1.
 */

public class PhotoAlbumActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private PhotoAlbumAdapter adapter;
    private int itemSize;

    private ScaleType scaleType = ScaleType.CENTER_CROP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration(new SpaceDecoration(10));
        itemSize = (MyApplication.getScreenWidth() - 10 * 4) / 3;
        adapter = new PhotoAlbumAdapter(this);
        recyclerView.setAdapter(adapter);
        new ReadImagesTask(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scale_type, menu);
        menu.findItem(R.id.action_clear_cache).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_center_crop:
                scaleType = ScaleType.CENTER_CROP;
                break;
            case R.id.action_start_crop:
                scaleType = ScaleType.START_CROP;
                break;
            case R.id.action_end_crop:
                scaleType = ScaleType.END_CROP;
                break;
            case R.id.action_fit_xy:
                scaleType = ScaleType.FIT_XY;
                break;
        }
        adapter.notifyDataSetChanged();
        return true;
    }

    class PhotoAlbumAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

        private Context context;
        private List<String> images = new ArrayList<>();

        PhotoAlbumAdapter(Context context) {
            this.context = context;
        }

        public void setData(List<String> images) {
            this.images = images;
        }

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(itemSize, itemSize));
            return new PhotoViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(final PhotoViewHolder holder, final int position) {
            Glide.with(context).load(images.get(position))
                    .placeholder(R.drawable.place_holder)
                    .transform(new CustomTransform(context, scaleType))
                    .into((ImageView) holder.itemView);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageTrans.with(context)
                            .setImageList(images)
                            .setSourceImageView(new SourceImageViewGet() {
                                @Override
                                public ImageView getImageView(int pos) {
                                    int layoutPos = recyclerView.indexOfChild(holder.itemView);
                                    int viewPos = layoutPos + pos - position;
                                    View view = recyclerView.getChildAt(viewPos);
                                    if (view != null) return (ImageView) view;
                                    return null;
                                }
                            })
                            .setScaleType(scaleType)
                            .setImageLoad(new MyImageLoad())
                            .setNowIndex(position)
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return images.size();
        }
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        private SparseArray<View> views;

        public PhotoViewHolder(View itemView) {
            super(itemView);
        }

        public <E extends View> E get(int id) {
            View childView = views.get(id);
            if (null == childView) {
                childView = itemView.findViewById(id);
                views.put(id, childView);
            }
            return (E) childView;
        }
    }

    private class ReadImagesTask extends AsyncTask<Void, Integer, List<String>> {
        private Context context;

        private ReadImagesTask(Context context) {
            this.context = context;
        }

        @Override
        protected List<String> doInBackground(Void[] params) {
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] columns = new String[]{
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_MODIFIED
            };
            String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

            ContentResolver mContentResolver = context.getContentResolver();
            Cursor mCursor = mContentResolver.query(mImageUri, columns, null, null, sortOrder);
            if (mCursor == null) {
                return null;
            }

            List<String> imagePathList = new ArrayList<>();
            while (mCursor.moveToNext()) {
                //获取图片的路径
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                if (!path.endsWith(".gif") && !path.endsWith(".GIF"))
                    imagePathList.add("file:///" + path);
            }
            mCursor.close();
            return imagePathList;
        }

        @Override
        protected void onPostExecute(List<String> imageUriList) {
            adapter.setData(imageUriList);
            adapter.notifyDataSetChanged();
        }
    }
}
