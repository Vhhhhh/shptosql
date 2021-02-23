package com.comleader;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @Author: Daisen.Z
 * @Date: 2021/2/22 16:02
 * @Version: 1.0
 * @Description:
 */
public class ImportPoiUtil {

    public static void main(String[] args) throws IOException {

        String tableName="routinemonitor.t_bas_grid_layer";
        List<String> grid_fields = Arrays.asList("the_geom", "gd_code", "region");
        List<Integer> grid_fields_location = Arrays.asList(0, 1, 2);
        String shpFilePath="E:\\信大网御\\互联网业务部\\离线地图\\POI导入\\canyin.shp";
        String sqlFileName="上海市矢量数据_出入口";

        ImportPoiUtil.shpConvertSql(tableName, grid_fields, grid_fields_location, shpFilePath, sqlFileName);
    }

    private static String CHAR_ENCODEING="GBK";

    /**
     * 将shp文件生成sql语句
     * @param tableName 需要入库的表格名称
     * @param tableField 需要入库的表格属性名称集合
     * @param fieldLocation 需要入库shp文件：入库表格数据对应shp文件的位置
     * @param shpFilePath 需要入库shp文件的绝对路径
     * @param sqlFileName 生成sql文件名称
     * @throws IOException
     */
    private static void shpConvertSql(String tableName, List<String> tableField, List<Integer> fieldLocation, String shpFilePath, String sqlFileName) throws IOException {

        StringBuffer field = new StringBuffer();
        tableField.stream().forEach(s -> {
            field.append(s+",");
        });

        ShapefileDataStore sds = (ShapefileDataStore)new ShapefileDataStoreFactory().createDataStore(new File(shpFilePath).toURI().toURL());
        sds.setCharset(Charset.forName(CHAR_ENCODEING));
        SimpleFeatureIterator itertor = sds.getFeatureSource().getFeatures().features();
        StringBuffer sql = new StringBuffer();
        while(itertor.hasNext()){
            SimpleFeature feature = itertor.next();
            System.out.println("gml_id:"+feature.getAttribute("gml_id"));
            System.out.println("name:"+feature.getAttribute("Name"));
            System.out.println("pyname:"+feature.getAttribute("pyname"));
            System.out.println("kind:"+feature.getAttribute("kind"));
            Object zipcode = feature.getAttribute("zipcode");
            System.out.println("zipcode:"+feature.getAttribute("zipcode"));
            System.out.println("telephone:"+feature.getAttribute("telephone"));
            System.out.println("display_x:"+feature.getAttribute("display_x"));
            System.out.println("display_y:"+feature.getAttribute("display_y"));
            System.out.println("side:"+feature.getAttribute("side"));
            System.out.println("address:"+feature.getAttribute("address"));
            System.out.println("the_geom:"+feature.getAttribute("the_geom")+"\n\n");
            sql.append("insert into "+tableName+"("+field.substring(0, field.length()-1)+")values(");
            for (Integer integer : fieldLocation) {

                sql.append("'"+ feature.getAttributes().get(integer).toString()+"',");
            }
            sql=new StringBuffer(sql.substring(0, sql.length()-1));
            sql.append(");\n");
            //System.out.println(sql.toString());
        }
        itertor.close();
        //System.out.println(sql.toString());
    }


}
