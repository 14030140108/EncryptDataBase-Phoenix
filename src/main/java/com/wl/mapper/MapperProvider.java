package com.wl.mapper;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@SuppressWarnings("ALL")
public class MapperProvider {

    /**
     * 手动构造创建表的SQL语句
     *
     * @param tableName 表名
     * @param fields    字段列表
     * @return SQL语句
     */
    public String createTable(String tableName, List<String> fields) {
        String result = "CREATE TABLE IF NOT EXISTS \"" +
                tableName + "\" ( ";
        String temp = "\"";
        for (int i = 0; i < fields.size(); i++) {
            if (i == 0) {
                temp += fields.get(i) + "\" VARCHAR NOT NULL PRIMARY KEY, \"";
            } else if (i != fields.size() - 1) {
                temp += fields.get(i) + "\" VARCHAR, \"";
            } else {
                temp += fields.get(i) + "\" VARCHAR)";
            }
        }
        log.info("创建表的SQL语句：" + result + temp);
        return result + temp;
    }

    /**
     * 手动构造创建表的索引
     *
     * @param tableName 表名
     * @param fields    字段列表
     * @return SQL语句
     */
    public String createIndex(String tableName, List<String> fields) {
        String result = "CREATE INDEX \"" +
                fields.get(fields.size() - 1) +
                "\" ON \"" + tableName + "\"(\"" + fields.get(fields.size() - 1) +
                "\") INCLUDE(";

        for (int i = 1; i < fields.size() - 1; i++) {
            result += "\"";
            result += fields.get(i);
            result += "\",";
        }
        result = result.substring(0, result.length() - 1);
        result += ")";
        log.info("创建索引的SQL语句：" + result);
        return result;
    }


    /**
     * 插入数据的SQL语句
     *
     * @param tableName 表名
     * @param data      数据
     * @return SQL语句
     */
    public String insert(String tableName, List<String> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPSERT INTO ")
                .append("\"")
                .append(tableName)
                .append("\" ")
                .append("VALUES (");
        for (int i = 0; i < data.size(); i++) {
            sb.append("\'")
                    .append(data.get(i))
                    .append("\' ,");
        }
        String result = sb.substring(0, sb.length() - 2);
        return result + ")";
    }

    public String select(String tableName, String column, String minSTCode, String maxSTCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM \"")
                .append(tableName)
                .append("\" ")
                .append("WHERE \"")
                .append(column)
                .append("\" BETWEEN \'")
                .append(minSTCode)
                .append("\' AND \'")
                .append(maxSTCode)
                .append("\'");
        return sb.toString();
    }

    public String selectFastGeoSSWByAESLat(String tableName, String aesLat, List<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(columns.get(0)).append("\",\"")
                .append(columns.get(1)).append("\",\"")
                .append(columns.get(2)).append("\" FROM \"")
                .append(tableName).append("\" WHERE \"")
                .append(columns.get(3)).append("\" = \'")
                .append(aesLat).append("\'");
        return sb.toString();
    }

    public String selectPointById(String tableName, String id, List<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(columns.get(0)).append("\",\"")
                .append(columns.get(1)).append("\",\"")
                .append(columns.get(2)).append("\",\"")
                .append(columns.get(3)).append("\" FROM \"")
                .append(tableName).append("\" WHERE \"")
                .append(columns.get(0)).append("\" = \'")
                .append(id).append("\'");
        return sb.toString();
    }

    public String selectColumn(String tableName) {
        return "SELECT COLUMN_NAME FROM SYSTEM.CATALOG WHERE TABLE_NAME=\'" + tableName + "\'";
    }

    public String selectFastGeo(String tableName, List<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"").append(columns.get(0)).append("\",\"")
                .append(columns.get(1)).append("\",\"")
                .append(columns.get(2)).append("\",\"")
                .append(columns.get(3)).append("\" FROM \"")
                .append(tableName).append("\"");
        return sb.toString();
    }
}
