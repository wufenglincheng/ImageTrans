package it.liuting.imagetrans;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.liuting.imagetrans.listener.SourceImageViewGet;

/**
 * Created by liuting on 17/5/27.
 */

public class ImageTrans extends DialogFragment implements DialogInterface.OnKeyListener, DialogInterface {
    private AlertDialog mDialog;
    private ImageTransBuild build;
    private DialogViewInflater dialogViewInflater;
    private Context mContext;

    public static ImageTrans with(Context context) {
        ImageTrans imageTrans = new ImageTrans();
        imageTrans.mContext = context;
        imageTrans.build = new ImageTransBuild();
        return imageTrans;
    }

    public ImageTrans setNowIndex(int index) {
        build.clickIndex = index;
        build.nowIndex = index;
        return this;
    }

    public ImageTrans setImageList(List<String> imageList) {
        build.imageList = imageList;
        return this;
    }

    public ImageTrans setSourceImageView(SourceImageViewGet sourceImageView) {
        build.sourceImageViewGet = sourceImageView;
        return this;
    }

    public ImageTrans setAdapter(ImageTransAdapter adapter) {
        build.imageTransAdapter = adapter;
        return this;
    }

    public ImageTrans setImageLoad(ImageLoad imageLoad) {
        build.imageLoad = imageLoad;
        return this;
    }

    public ImageTrans setScaleType(ScaleType scaleType) {
        build.scaleType = scaleType;
        return this;
    }

    public ImageTrans setConfig(ITConfig itConfig) {
        build.itConfig = itConfig;
        return this;
    }

    public ImageTrans setProgressBar(Class c,int width,int height){
        build.progressClass = c;
        build.progressWidth = width;
        build.progressHeight = height;
        return this;
    }

    public void show() {
        FragmentManager fragmentManager = getSupportFragmentManager(mContext);
        if (fragmentManager == null)
            throw new NullPointerException("fragmentManager is null,the dialog must be call on FragmentActivity");
        show(fragmentManager, "ImageTrans");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        build.checkParam();
        build.dialogInterface = this;
        getDialog().setOnKeyListener(this);
        dialogViewInflater = new DialogViewInflater(build, getChildFragmentManager());
        return dialogViewInflater.createView(inflater);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialogViewInflater.init(view);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.MyDialogStyle);
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP &&
                !event.isCanceled()) {
            if (dialogViewInflater != null) dialogViewInflater.runClose();
        }
        return true;
    }


    private FragmentManager getSupportFragmentManager(Context source) {
        Context finalContext = source;
        while (finalContext instanceof ContextWrapper) {
            if (finalContext == null) {
                break;
            }
            if (finalContext instanceof FragmentActivity) {
                return ((FragmentActivity) finalContext).getSupportFragmentManager();
            }
            Context tempContext = ((ContextWrapper) finalContext).getBaseContext();
            if (finalContext != tempContext) {
                finalContext = tempContext;
            } else {
                finalContext = null;
            }
        }
        return null;
    }

    @Override
    public void cancel() {
        if (dialogViewInflater != null) dialogViewInflater.runClose();
        else dismiss();
    }
}
