package com.demo.imagetransdemo.activity;

import android.content.Context;
import android.os.Bundle;
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
import com.demo.imagetransdemo.adapter.MyImageTransAdapter;
import com.demo.imagetransdemo.view.RingLoadingView;
import com.demo.imagetransdemo.view.SpaceDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.liuting.imagetrans.ImageTrans;
import it.liuting.imagetrans.ScaleType;
import it.liuting.imagetrans.listener.SourceImageViewGet;

/**
 * Created by liuting on 17/6/1.
 */

public class NetImageActivity extends AppCompatActivity {

    public static String[] netImages = {
            "http://wx1.sinaimg.cn/bmiddle/9672f95cly1fgcl0xc7hmj20gt5fwhdt.jpg",
            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=4210120443,3922685164&fm=27&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=2966021298,3341101515&fm=23&gp=0.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496402134202&di=6c7f4a6afa5bdf02000c788f7a51e9c0&imgtype=0&src=http%3A%2F%2Fcdnq.duitang.com%2Fuploads%2Fitem%2F201506%2F23%2F20150623183946_iZtFs.jpeg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496996892&di=ea1e213c8ddd4427c55f073db9bf91b7&imgtype=jpg&er=1&src=http%3A%2F%2Fpic27.nipic.com%2F20130323%2F9483785_182530048000_2.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496996959&di=13c094ba73675a24df2ad1d2c730c02c&imgtype=jpg&er=1&src=http%3A%2F%2Fdasouji.com%2Fwp-content%2Fuploads%2F2015%2F07%2F%25E9%2595%25BF%25E8%258A%25B1%25E5%259B%25BE-6.jpg"
    };

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
        adapter.setData(Arrays.asList(netImages));
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scale_type, menu);
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
                                    View view = recyclerView.getChildAt(layoutPos + pos - position);
                                    if (view != null) return (ImageView) view;
                                    return (ImageView) holder.itemView;
                                }
                            })
                            .setImageLoad(new MyImageLoad())
                            .setNowIndex(position)
                            .setProgressBar(RingLoadingView.class, MyApplication.dpToPx(50), MyApplication.dpToPx(50))
                            .setAdapter(new MyImageTransAdapter())
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
}
