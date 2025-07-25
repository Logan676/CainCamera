package com.cgfay.camera;

import android.app.Activity;

import com.cgfay.camera.model.AspectRatio;

import java.lang.ref.WeakReference;

/**
 * 相机预览引擎
 */
public final class PreviewEngine {

    private WeakReference<Activity> mWeakActivity;

    private PreviewEngine(Activity activity) {
        mWeakActivity = new WeakReference<>(activity);
    }

    public static PreviewEngine from(Activity activity) {
        return new PreviewEngine(activity);
    }


    /**
     * 设置长宽比
     * @param ratio
     * @return
     */
    public PreviewBuilder setCameraRatio(AspectRatio ratio) {
        return new PreviewBuilder(this, ratio);
    }

    public Activity getActivity() {
        return mWeakActivity.get();
    }



}
