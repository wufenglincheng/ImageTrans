package it.liuting.imagetrans;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import java.util.List;

import it.liuting.imagetrans.listener.ImageLoad;
import it.liuting.imagetrans.listener.ImageTransAdapter;
import it.liuting.imagetrans.listener.SourceImageViewParam;

/**
 * Created by liuting on 17/5/27.
 */

public class ImageTrans implements TransImageView.OnCloseListener, DialogInterface.OnShowListener, DialogInterface.OnKeyListener, DialogInterface.OnDismissListener, DialogInterface {
    protected Context mContext;
    protected AlertDialog mDialog;
    protected DialogRootLayout mDialogRootLayout;
    protected ImageTransParam mImageTransParam;
    private boolean isShow = false;

    public static ImageTrans with(Context context) {
        return new ImageTrans(context);
    }

    private ImageTrans(Context context) {
        this.mContext = context;
        mImageTransParam = new ImageTransParam(context);
    }


    public ImageTrans setNowIndex(int index) {
        mImageTransParam.clickIndex = index;
        mImageTransParam.nowIndex = index;
        return this;
    }

    public ImageTrans setImageList(List<String> imageList) {
        mImageTransParam.imageList = imageList;
        return this;
    }

    public ImageTrans setImageLoad(ImageLoad imageLoad) {
        mImageTransParam.imageLoad = imageLoad;
        return this;
    }

    public ImageTrans setSourceImageViewParam(SourceImageViewParam imageViewParam) {
        mImageTransParam.sourceImageViewParam = imageViewParam;
        return this;
    }

    public ImageTrans setAdapter(ImageTransAdapter adapter) {
        mImageTransParam.imageTransAdapter = adapter;
        return this;
    }

    private void createView() {
        mDialogRootLayout = new DialogRootLayout(mContext, mImageTransParam, this);
        mDialogRootLayout.setOnCloseListener(this);
    }

    public void show() {
        mImageTransParam.checkParam();
        createView();
        mDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle)
                .setView(mDialogRootLayout)
                .create();
        mDialog.setOnDismissListener(this);
        mDialog.setOnShowListener(this);
        mDialog.setOnKeyListener(this);
        mDialog.show();
    }

    @Override
    public void close() {
        mDialog.cancel();
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (isShow && keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP &&
                !event.isCanceled()) {
            isShow = false;
            mDialogRootLayout.runClose();
        }
        return true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    @Override
    public void onShow(DialogInterface dialog) {
        isShow = true;
    }

    @Override
    public void cancel() {
        mDialogRootLayout.runClose();
    }

    @Override
    public void dismiss() {
        mDialogRootLayout.runClose();
    }
}
