package com.cgfay.filter.glfilter.color.bean

open class DynamicColorBaseData {
    var name: String? = null                     // 滤镜名称
    var vertexShader: String? = null             // vertex shader名称
    var fragmentShader: String? = null           // fragment shader名称
    var uniformList: MutableList<String> = ArrayList()   // 统一变量字段列表
    var uniformDataList: MutableList<UniformData> = ArrayList()   // 统一变量数据列表
    var strength: Float = 0f                     // 默认强度
    var texelOffset: Boolean = false             // 是否存在宽高偏移量的统一变量
    var audioPath: String? = null                // 滤镜音乐滤镜
    var audioLooping: Boolean = false            // 音乐是否循环播放

    override fun toString(): String {
        return "DynamicColorData{" +
            "name='" + name + '\'' +
            ", vertexShader='" + vertexShader + '\'' +
            ", fragmentShader='" + fragmentShader + '\'' +
            ", uniformList=" + uniformList +
            ", uniformDataList=" + uniformDataList +
            ", strength=" + strength +
            ", texelOffset=" + texelOffset +
            ", audioPath='" + audioPath + '\'' +
            ", audioLooping=" + audioLooping +
            '}'
    }

    class UniformData(var uniform: String, var value: String)
}
