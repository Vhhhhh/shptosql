package com.comleader;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * @author 张志航
 * @description  用于转换数据格式的工具类
 * @date 2020-11-25 14:20
 * method:countFileSize: 数据大小格式转换
 * toDuration： 间隔时常毫秒转换为时长
 */
public class TypeFormatUtil {

    public static SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * @param dataSize
     * @description: 数据大小格式转换(接收单位是B的)
     * @return: int
     * @author: zhanghang
     * @date: 2020/4/10
     **/
    public static String formatFileSize(long dataSize) {
        double size = dataSize / 1024;  // 先转换为kb
        if (size < 1024) {
            return new DecimalFormat("#.##").format(size) + "KB";
        } else if (size < 1024 * 1024) {
            size = size / 1024.0;
            return new DecimalFormat("#.##").format(size) + "MB";
        } else if (size < 1024 * 1024 * 1024) {
            size = size / 1024.0 / 1024.0;
            return new DecimalFormat("#.##").format(size) + "GB";
        } else {
            size = size / 1024.0 / 1024.0 / 1024.0;
            return new DecimalFormat("#.##").format(size) + "T";
        }
    }

    /**
     * @param time
     * @return
     * @description 间隔时间毫秒转换为时长
     * @author zhanghang
     * @date 2020-09-25 12:59:27
     **/
    public static String toDuration(long time) {
        int day = (int) (time / (1000 * 60 * 60 * 24));
        int hours = (int) ((time % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        int minutes = (int) ((time % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((time % (1000 * 60)) / 1000);
        StringBuilder duraTime = new StringBuilder();
        if (day > 0) {
            duraTime.append(day + "天");
        }
        if (hours > 0) {
            duraTime.append(hours + "时");
        }
        if (minutes > 0) {
            duraTime.append(minutes + "分");
        }
        duraTime.append(seconds + "秒");
        return duraTime.toString();
    }

    /**
     * 时间戳格式化为时间
     * @param time
     * @return
     */
    public static String formatToDateTime(long time){
        String dataTime = sdfTime.format(time);
        return dataTime;
    }


    /**
     * 时间戳格式化为日期
     * @param time
     * @return
     */
    public static String formatToDate(long time){
        String dataTime = sdfDate.format(time);
        return dataTime;
    }

    /**
     * 数字转换为带单位的，如：10201转换为1万201
     * @param count
     * @return
     */
    public static String intToUnit(int count){
        StringBuilder sb = new StringBuilder();
        if (count > 100000000){  // 亿的换算
            int calculate =  count/ 100000000;
            sb.append(calculate+"亿");
            count = count % 100000000;
        }
        if (count > 10000){  // 万的换算
            int wan =  count / 10000;
            sb.append(wan+"万");
            count = count % 10000;
        }
        if (count>0){
            sb.append(count);
        }
        return sb.toString();
    }

    /**
     * 数字转换为带单位的，如：10201转换为1万201
     * @param count
     * @return
     */
    public static String intToDoubleUnit(long count){
        // 平均每个图片20K
        String unit = "";
        double size = count;
        if (size > 10000) {
            unit = "万";
            size = size/10000;
        }
        if (size > 10000) {
            unit = "亿";
            size = size / 10000;
        }
        if (size > 10000)  {
            unit = "万亿";
            size = size / 10000;
        }
        return new DecimalFormat("#.##").format(size) + "_"+unit;
    }
}
