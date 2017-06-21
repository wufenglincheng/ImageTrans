package com.demo.imagetransdemo.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.demo.imagetransdemo.ItemData;
import com.demo.imagetransdemo.MyApplication;
import com.demo.imagetransdemo.R;
import com.demo.imagetransdemo.view.ItemDecorationAlbumColumns;

import java.util.List;

import it.liuting.imagetrans.ImageTrans;
import it.liuting.imagetrans.ScaleType;
import it.liuting.imagetrans.listener.SourceImageViewParam;

/**
 * Created by liuting on 17/6/20.
 */

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineAdapter.ViewHolder> {

    private List<ItemData> mDatas;
    private Context context;

    public TimeLineAdapter(List<ItemData> datas, Context context) {
        mDatas = datas;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case ItemData.VIEW_TYPE_SINGLE:
                view = inflater.inflate(R.layout.item_single_image, parent, false);
                break;
            case ItemData.VIEW_TYPE_MUTIL:
                view = inflater.inflate(R.layout.item_single_images, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.item_single_image, parent, false);
                break;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ItemData data = mDatas.get(position);
        TextView content = holder.get(R.id.item_content);
        content.setText(data.text);
        int imageParentWidth = (MyApplication.getScreenWidth() - MyApplication.dpToPx(90));
        switch (getItemViewType(position)) {
            case ItemData.VIEW_TYPE_SINGLE:
                buildSingleImage(data, holder, imageParentWidth);
                break;
            case ItemData.VIEW_TYPE_MUTIL:
                buildMutilImages(data, holder, imageParentWidth);
                break;
        }
    }

    private void buildMutilImages(final ItemData data, ViewHolder holder, int imageParentWidth) {
        final RecyclerView recyclerView = holder.get(R.id.item_image);
        int column = data.images.size() == 4 ? 2 : 3;
        recyclerView.setLayoutManager(new GridLayoutManager(context, column));
        int space = MyApplication.dpToPx(3);
        ItemDecorationAlbumColumns itemDecoration = new ItemDecorationAlbumColumns(space, column);
        final int itemWidth = (imageParentWidth - space * (column - 1)) / column;
        final int itemHeight = column == 2 ? (int) (itemWidth * 9f / 16) : itemWidth;
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setFocusable(false);
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                ImageView image = new ImageView(parent.getContext());
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(itemWidth, itemHeight);
                image.setLayoutParams(layoutParams);
                return new ViewHolder(image);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
                ImageView image = (ImageView) holder.itemView;
                Glide.with(context)
                        .load(data.images.get(position))
                        .placeholder(R.drawable.place_holder)
                        .transform(new CustomTransform(context, ScaleType.CENTER_CROP))
                        .into(image);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageTrans.with(context)
                                .setImageList(data.images)
                                .setSourceImageViewParam(new SourceImageViewParam() {
                                    @Override
                                    public View getSourceView(int pos) {
                                        int layoutPos = recyclerView.indexOfChild(holder.itemView);
                                        View view = recyclerView.getChildAt(layoutPos + pos - position);
                                        if (view != null) return view;
                                        return holder.itemView;
                                    }

                                    @Override
                                    public ScaleType getScaleType(int position) {
                                        return ScaleType.CENTER_CROP;
                                    }
                                })
                                .setImageLoad(new MyImageLoad())
                                .setNowIndex(position)
                                .setAdapter(new MyImageTransAdapter())
                                .show();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return data.images.size();
            }
        });
    }

    private void buildSingleImage(final ItemData data, ViewHolder holder, int imageParentWidth) {
        final ImageView imageView = holder.get(R.id.item_image);
        imageView.getLayoutParams().height = (int) (imageParentWidth * 9f / 16);
        imageView.setLayoutParams(imageView.getLayoutParams());
        Glide.with(context)
                .load(data.images.get(0))
                .placeholder(R.drawable.place_holder)
                .transform(new CustomTransform(context, ScaleType.CENTER_CROP))
                .into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageTrans.with(context)
                        .setImageList(data.images)
                        .setSourceImageViewParam(new SourceImageViewParam() {
                            @Override
                            public View getSourceView(int pos) {
                                return imageView;
                            }

                            @Override
                            public ScaleType getScaleType(int position) {
                                return ScaleType.CENTER_CROP;
                            }
                        })
                        .setImageLoad(new MyImageLoad())
                        .setNowIndex(0)
                        .setAdapter(new MyImageTransAdapter())
                        .show();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return mDatas.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        <T extends View> T get(int id) {
            return (T) itemView.findViewById(id);
        }
    }
}
