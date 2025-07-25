package com.cgfay.filter.glfilter.color.bean

/**
 * 滤镜数据
 */
class DynamicColor {
    // 滤镜解压的文件夹路径
    var unzipPath: String? = null

    // 滤镜列表
    val filterList: MutableList<DynamicColorData> = ArrayList()

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("unzipPath: ")
            .append(unzipPath)
            .append("\n")

        builder.append("data: [")
        for (i in filterList.indices) {
            builder.append(filterList[i].toString())
            if (i < filterList.size - 1) {
                builder.append(",")
            }
        }
        builder.append("]")
        return builder.toString()
    }
}
