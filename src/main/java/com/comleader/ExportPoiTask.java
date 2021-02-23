package com.comleader;

import cn.hutool.core.io.FileUtil;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @Author: Daisen.Z
 * @Date: 2021/2/23 9:52
 * @Version: 1.0
 * @Description: 导出sql的线程工具类
 */
public class ExportPoiTask implements Callable<String> {

    private String tableName;
    private List<String> tableField;
    private List<String> gridFields;
    private String shpFileaPath;

    public ExportPoiTask(String tableName, List<String> tableField, List<String> gridFields, String shpFileaPath) {
        this.tableName = tableName;
        this.tableField = tableField;
        this.gridFields = gridFields;
        this.shpFileaPath = shpFileaPath;
    }

    @Override
    public String call() throws Exception {
        System.out.println(shpFileaPath + " 》》》生成SQL 》》》 Start");
        StringBuffer field = new StringBuffer();
        tableField.stream().forEach(s -> field.append(s + ","));
        String field1 = field.substring(0, field.length() - 1);
        ShapefileDataStore sds = (ShapefileDataStore) new ShapefileDataStoreFactory().createDataStore(FileUtil.file(new File(shpFileaPath)).toURI().toURL());
        sds.setCharset(Charset.forName(ExportPoiToSql.SHP_ENCODEING));
        SimpleFeatureIterator itertor = sds.getFeatureSource().getFeatures().features();
        StringBuffer sql = new StringBuffer();
        while (itertor.hasNext()) {
            SimpleFeature feature = itertor.next();
            System.out.println(feature.getAttributes());
            //System.out.println(feature.getAttribute("gml_id"));
            //System.out.println(feature.getAttribute("Name"));
            //System.out.println(feature.getAttribute("pyname"));
            //System.out.println(feature.getAttribute("kind"));
            //System.out.println(feature.getAttribute("zipcode"));
            //System.out.println(feature.getAttribute("telephone"));
            //System.out.println(feature.getAttribute("display_x"));
            //System.out.println(feature.getAttribute("display_y"));
            //System.out.println(feature.getAttribute("side"));
            //System.out.println(feature.getAttribute("address"));
            //System.out.println(feature.getAttribute("geom"));
            sql.append("insert into " + tableName + "(" + field1 + ")values(");
            for (int i = 0; i < gridFields.size(); i++) {
                String fieldName = gridFields.get(i);
                if (feature.getAttribute(fieldName) != null || String.valueOf(feature.getAttribute(fieldName)).contains("'")) { // 去除掉列中的特殊字符
                    feature.setAttribute(fieldName,feature.getAttribute(fieldName).toString().replaceAll("'", " "));
                }
                if (i == gridFields.size() - 1) {
                    sql.append("'" + feature.getAttribute(fieldName) + "');\n");
                } else {
                    sql.append("'" + feature.getAttribute(fieldName) + "',");
                }
            }
            ExportPoiToSql.total++;
        }
        //itertor.close();
        FileUtil.appendString(sql.toString(), ExportPoiToSql.sqlFile, ExportPoiToSql.SQL_ENCODEING);
        System.out.println(shpFileaPath + " 》》》生成SQL 》》》Success");
        return sql.toString();
    }
}
