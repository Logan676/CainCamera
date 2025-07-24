package com.cgfay.picker;


import android.content.Intent;
import androidx.fragment.app.FragmentActivity;

import com.cgfay.picker.selector.OnMediaSelector;
import com.cgfay.picker.loader.MediaLoader;

public final class MediaPickerBuilder {

    private OnMediaSelector mMediaSelector;
    private MediaPicker mMediaPicker;
    private MediaPickerParam mPickerParam;

    public MediaPickerBuilder(MediaPicker engine) {
        mMediaPicker = engine;
        mPickerParam = new MediaPickerParam();
        MediaPickerManager.getInstance().reset();
    }

    /**
     * 是否显示拍照item列表
     * @param show
     * @return
     */
    public MediaPickerBuilder showCapture(boolean show) {
        mPickerParam.setShowCapture(show);
        return this;
    }

    /**
     * 是否显示视频
     * @param show
     * @return
     */
    public MediaPickerBuilder showVideo(boolean show) {
        mPickerParam.setShowVideo(show);
        return this;
    }

    /**
     * 是否显示图片
     * @param show
     * @return
     */
    public MediaPickerBuilder showImage(boolean show) {
        mPickerParam.setShowImage(show);
        return this;
    }

    /**
     * 一行的item数目
     * @param spanCount
     * @return
     */
    public MediaPickerBuilder spanCount(int spanCount) {
        if (spanCount < 0) {
            throw new IllegalArgumentException("spanCount cannot be less than zero");
        }
        mPickerParam.setSpanCount(spanCount);
        return this;
    }

    /**
     * 分割线大小
     * @param spaceSize
     * @return
     */
    public MediaPickerBuilder spaceSize(int spaceSize) {
        if (spaceSize < 0) {
            throw new IllegalArgumentException("spaceSize cannot be less than zero");
        }
        mPickerParam.setSpaceSize(spaceSize);
        return this;
    }

    /**
     * 设置是否存在边沿分割线
     * @param hasEdge
     * @return
     */
    public MediaPickerBuilder setItemHasEdge(boolean hasEdge) {
        mPickerParam.setItemHasEdge(hasEdge);
        return this;
    }

    public MediaPickerBuilder setAutoDismiss(boolean autoDismiss) {
        mPickerParam.setAutoDismiss(autoDismiss);
        return this;
    }

    /**
     * 图片加载器
     * @param loader
     * @return
     */
    public MediaPickerBuilder ImageLoader(MediaLoader loader) {
        MediaPickerManager.getInstance().setMediaLoader(loader);
        return this;
    }

    /**
     * 设置媒体选择监听器
     * @param selector
     */
    public MediaPickerBuilder setMediaSelector(OnMediaSelector selector) {
        mMediaSelector = selector;
        return this;
    }

    /**
     * 打开基于Compose的媒体选择页面
     */
    public void show() {
        FragmentActivity activity = mMediaPicker.getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, com.cgfay.picker.compose.PickerComposeActivity.class);
        intent.putExtra(MediaPicker.PICKER_PARAMS, mPickerParam);
        activity.startActivity(intent);
    }
}
