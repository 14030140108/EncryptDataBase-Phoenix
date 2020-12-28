package com.wl.threadHandler;

import com.wl.Util.GetBeanUtil;
import com.wl.service.PhoenixService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
@SuppressWarnings("ALL")
public class UpsertHandler implements Handler {

    private PhoenixService phoenixService = GetBeanUtil.getBean(PhoenixService.class);
    private String sql;
    private boolean isEncrypt;

    UpsertHandler(String sql, boolean isEncrypt) {
        this.sql = sql;
        this.isEncrypt = isEncrypt;

    }

    @Override
    public String executeHandler() {
        boolean isSTCode = sql.contains("STCodePoint");
        return parseUpsertSQL(isSTCode);
    }

    private String parseUpsertSQL(boolean isSTCode) {
        try {
            // 获取表名
            String tableName = sql.split(" ")[2];

            // 获取插入的数据值
            List<String> data = generateData(sql);
            //插入数据
            if (isSTCode) {
                if (isEncrypt) {
                    phoenixService.insertEncryptSTCode(tableName, data);
                } else {
                    phoenixService.insertSTCode(tableName, data);
                }
            } else {
                if (isEncrypt) {
                    phoenixService.insertEncryptFastGeo(tableName, data);
                } else {
                    phoenixService.insertFastGeo(tableName, data);
                }
            }
            return "数据插入成功";

        } catch (Exception e) {
            log.error("插入数据失败");
            return "插入数据失败";
        }
    }

    /**
     * 获取语句中需要插入的数据
     *
     * @param sql
     * @param isSTCode
     * @return
     */
    private List<String> generateData(String sql) {
        List<String> result = new ArrayList<>();

        //获取id
        int startIndex = sql.indexOf("(");
        int endIndex = sql.lastIndexOf(")");
        sql = sql.substring(startIndex + 1, endIndex).trim();
        String id = sql.split(",")[0].trim();
        result.add(id);

        //获取点的坐标
        startIndex = sql.indexOf("(");
        endIndex = sql.lastIndexOf(")");
        sql = sql.substring(startIndex + 1, endIndex).trim();
        String[] points = sql.split(",");
        for (String point : points) {
            point = point.trim();
            if (point.contains("\"")) {
                point = point.substring(1, point.length() - 1);
            }
            result.add(point);
        }
        return result;
    }
}
