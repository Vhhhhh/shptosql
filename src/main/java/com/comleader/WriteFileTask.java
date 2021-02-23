package com.comleader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @Author: Daisen.Z
 * @Date: 2021/2/23 17:02
 * @Version: 1.0
 * @Description: 将大文件拆分成小文件的工具类
 */
public class WriteFileTask implements Callable<String> {

    private List<String> content;
    private String filePath;

    public WriteFileTask(List<String> content, String filePath) {
        this.content = content;
        this.filePath = filePath;
    }

    @Override
    public String call() throws Exception {
        System.out.println(filePath);
        FileUtil.writeLines(content, filePath, ExportPoiToSql.SQL_ENCODEING, true);
        System.out.println(filePath+" 》》Success !");
        return "";
    }

}
