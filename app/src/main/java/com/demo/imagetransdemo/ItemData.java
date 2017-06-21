package com.demo.imagetransdemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuting on 17/6/20.
 */

public class ItemData {
    public static final int VIEW_TYPE_SINGLE = 0;
    public static final int VIEW_TYPE_MUTIL = 1;
    public List<String> images = new ArrayList<>();
    public String text;

    public int getViewType() {
        if (images.size() == 1) {
            return VIEW_TYPE_SINGLE;
        } else {
            return VIEW_TYPE_MUTIL;
        }
    }
}
