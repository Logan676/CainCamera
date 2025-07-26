package com.cgfay.media;

import android.util.Log;

/**
 * @author CainHuang
 * @date 2019/6/7
 */
public final class FFmpegUtils {

    private static final String TAG = "FFmpegUtils";

    static {
        try {
            System.loadLibrary("ffmpeg");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library ffmpeg", e);
            throw new RuntimeException("Failed to load native library: ffmpeg", e);
        }
        try {
            System.loadLibrary("ffcommand");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library ffcommand", e);
            throw new RuntimeException("Failed to load native library: ffcommand", e);
        }
    }

    private FFmpegUtils() {}

    private static native int _execute(String[] command);

    /**
     * 执行命令行，执行成功返回0，失败返回错误码。
     * @param commands  命令行数组
     * @return  执行结果
     */
    public static int execute(String[] commands) {
        return _execute(commands);
    }
}
