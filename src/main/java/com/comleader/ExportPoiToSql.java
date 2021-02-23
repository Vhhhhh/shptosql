package com.comleader;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.util.StringUtil;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * @Author: Daisen.Z
 * @Date: 2021/2/23 9:51
 * @Version: 1.0
 * @Description:
 */
public class ExportPoiToSql {

    public static volatile int total = 0;

    public static String SHP_ENCODEING="GBK";
    public static String SQL_ENCODEING="GBK";
    // 执行任务的线程池
    private static ExecutorService es;

    private static CompletionService<String> completionService;

    private static int threadNum = 20; // 线程池个数

    public static File sqlFile = null;


    public static void main(String[] args) throws Exception {
        System.out.println("功能简介：将shp文件格式的poi数据导出为sql语句");

        // 打开监听输入,输入参数
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入文件绝对路径或目录路径(必填)：");
        String shpFilePath = scanner.nextLine();
        while (StringUtils.isEmpty(shpFilePath)){
            System.out.println("请输入文件绝对路径或目录路径(必填)：");
            shpFilePath = scanner.nextLine();
        }

        // 表名称
        System.out.println("请输入表名称(必填)：");
        String tableName = scanner.nextLine();
        while (StringUtils.isEmpty(tableName)){
            System.out.println("请输入表名称(必填)：");
            tableName = scanner.nextLine();
        }

        // 表列
        System.out.println("是否使用默认列（y/n）");
        System.out.println("\"gml_id\", \"name\", \"pyname\",\"kind\",\"zipcode\",\"telephone\",\"display_x\",\"display_y\",\"side\",\"address\",\"geom\"");
        String defaultFlag = scanner.nextLine();
        // 文件中数据格式
        List<String> grid_fields = Arrays.asList("gml_id", "Name", "pyname","kind","zipcode","telephone","display_x","display_y","side","address","the_geom");
        // 数据库表格式
        List<String> table_fields = Arrays.asList("gml_id", "name", "pyname","kind","zipcode","telephone","display_x","display_y","side","address","geom");

        if ("n".equals(defaultFlag)){
            System.out.println("请输入自定义列，与默认列顺序保持一致，多列以‘，’分隔，最后一个‘，’不要：");
            String customTableField = scanner.nextLine();
            table_fields = Arrays.asList(customTableField);
        }

        // 输出的sql文件路径
        System.out.println("输出sql文件路径,使用默认请按回车跳过（默认与shp文件同目录下poiSql文件夹，文件名称为当前日期+时间.sql）：");
        String saveSqlFilePath= scanner.nextLine();
        // 拼接sql文件名称
        LocalDateTime now = LocalDateTimeUtil.now();
        String sqlFileName = now.getYear()+""+now.getMonthValue()+""+now.getDayOfMonth()+""+now.getHour()+""+now.getMinute()+".sql";
        if (StringUtils.isEmpty(saveSqlFilePath)){
            File file = new File(shpFilePath);
            if (file.isDirectory()){
                saveSqlFilePath = shpFilePath + File.separator+"poiSql"+File.separator+sqlFileName;
            }else {
                saveSqlFilePath = file.getParent() + File.separator+"poiSql"+File.separator+sqlFileName;
            }
        }

        System.out.println("shp文件编码（默认GBK）：");
        String code = scanner.nextLine();
        if (StringUtils.isNoneEmpty(code)){
            SHP_ENCODEING = code;
        }

        System.out.println("输出sql文件编码（默认UTF-8）：");
        String sqlCode = scanner.nextLine();
        if (StringUtils.isNoneEmpty(sqlCode)){
            SQL_ENCODEING = code;
        }

        System.out.println("线程数量（默认20）：");
        String threadNumberStr = scanner.nextLine();
        if (StringUtils.isEmpty(threadNumberStr)){
            threadNum = 20;
        }else {
            threadNum = Integer.valueOf(threadNumberStr);
        }

        // 开始导出sql[shp文件路径(文件绝对路径或者目录)，表名称，文件解析列，表列，sql保存绝对路径,文件编码]
        ExportPoiToSql.shpConvertSql(shpFilePath,tableName,grid_fields,table_fields,saveSqlFilePath,SHP_ENCODEING);
    }

    /**
     *
     * @param shpFilePath shp文件路径(文件绝对路径或者目录)
     * @param tableName 表名称
     * @param gridFields 文件解析列
     * @param tableFields 表列
     * @param saveSqlFilePath sql保存绝对路径
     * @param charEncodeing 文件编码
     */
    private static void shpConvertSql(String shpFilePath, String tableName, List<String> gridFields, List<String> tableFields, String saveSqlFilePath, String charEncodeing) throws Exception {
        long startTime = System.currentTimeMillis();

        // 导出sql文件
        sqlFile = FileUtil.file(saveSqlFilePath);
        es = Executors.newFixedThreadPool(threadNum);
        completionService = new ExecutorCompletionService<>(es);
        // 创建shp文件
        File shpFile = FileUtil.file(shpFilePath);
        List<Future<String>> futures = new ArrayList<>();

        if (!shpFile.isDirectory()){// 如果是单个文件夹可以直接导出
            if (!shpFile.getName().endsWith(".shp")){
                System.out.println("文件格式不对!");
                System.exit(0);
            }else {
                System.out.println("开始导出...");
                // 提交导出任务
                Future<String> future = completionService.submit(new ExportPoiTask(tableName, tableFields, gridFields, shpFilePath));
                futures.add(future);
            }

        }else { // 如果是文件目录导出,则递归目录下的所有shp文件
            File[] files = shpFile.listFiles();
            System.out.println("开始导出...");
            boolean flag = true; // 记录是否有文件导出
            for (File file : files) {
                if (file.getName().endsWith(".shp")){
                    flag = false;
                    // 提交导出任务
                    Future<String> future = completionService.submit(new ExportPoiTask(tableName, tableFields, gridFields, file.getPath()));
                    futures.add(future);
                }
            }

            if (flag){
                System.out.println("没有找到符合文件！");
                System.exit(0);
            }
        }

        //File sqlFile = new File(saveSqlFilePath);
        //File parentFile = sqlFile.getParentFile();
        //if (!parentFile.exists() ||  !parentFile.isDirectory()){ // 如果父目录不存在，创建出来
        //    parentFile.mkdirs();
        //}
        for (Future<String> future : futures) {
            future.get();
        }

        // 显示结果
        long endTime = System.currentTimeMillis();
        StringBuilder result = new StringBuilder();
        result.append("导出成功!\n");
        result.append("总用时："+TypeFormatUtil.toDuration(endTime - startTime));
        result.append("\n文件大小："+FileUtil.readableFileSize(sqlFile));
        result.append("\n总条数："+TypeFormatUtil.intToUnit(total)+"条");
        System.out.println(result.toString());
        System.exit(0);
    }


}
