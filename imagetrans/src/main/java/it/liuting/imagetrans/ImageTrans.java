package it.liuting.imagetrans;

import android.app.Activity;
import android.content.Intent;

import java.util.List;

import it.liuting.imagetrans.listener.ImageLoad;
import it.liuting.imagetrans.listener.SourceImageViewParam;

/**
 * Created by liuting on 17/5/27.
 */

public class ImageTrans {
    private Activity context;
    private int nowIndex;
    private List<String> imageList;
    private ImageLoad imageLoad;
    private SourceImageViewParam imageViewParam;

    public static ImageTrans with(Activity context) {
        return new ImageTrans(context);
    }

    private ImageTrans(Activity context) {
        this.context = context;
    }


    public ImageTrans setNowIndex(int index) {
        this.nowIndex = index;
        return this;
    }

    public ImageTrans setImageList(List<String> imageList) {
        this.imageList = imageList;
        return this;
    }

    public ImageTrans setImageLoad(ImageLoad imageLoad) {
        this.imageLoad = imageLoad;
        return this;
    }
    public ImageTrans setSourceImageViewParam(SourceImageViewParam imageViewParam) {
        this.imageViewParam = imageViewParam;
        return this;
    }

    public void show() {
        StaticParam.imageList = imageList;
        StaticParam.imageLoad = imageLoad;
        StaticParam.clickIndex = nowIndex;
        StaticParam.nowIndex = nowIndex;
        StaticParam.sourceImageViewParam = imageViewParam;
        StaticParam.screenWidth = Util.getScreenWidth(context);
        StaticParam.screenHeight = Util.getScreenHeight(context);
        StaticParam.init();
        Intent intent = new Intent(context, ImageShowActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
        context.overridePendingTransition(0, 0);
    }

}
