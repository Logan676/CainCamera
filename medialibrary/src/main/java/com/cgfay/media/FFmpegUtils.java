package com.cgfay.media;

import android.util.Log;
import com.cgfay.uitls.utils.NativeLibraryLoader;

/**
 * @author CainHuang
 * @date 2019/6/7
 */
public final class FFmpegUtils {

    private static final String TAG = "FFmpegUtils";

    static {
        NativeLibraryLoader.loadLibraries(
                "ffmpeg",
                "ffcommand"
        );
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
