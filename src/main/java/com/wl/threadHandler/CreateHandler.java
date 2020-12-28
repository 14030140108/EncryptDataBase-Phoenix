package com.wl.threadHandler;

import com.wl.Util.GetBeanUtil;
import com.wl.beans.KeyType;
import com.wl.constant.Constants;
import com.wl.encryptAlgorithm.AES;
import com.wl.service.PhoenixService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@Slf4j
@SuppressWarnings("ALL")
public class CreateHandler implements Handler {

    PhoenixService phoenixService = GetBeanUtil.getBean(PhoenixService.class);

    private String sql;
    private boolean isEncrypt;

    CreateHandler(String sql, boolean isEncrypt) {
        this.sql = sql;
        this.isEncrypt = isEncrypt;
    }

    @Override
    public String executeHandler() {
        Pattern pattern = Pattern.compile("([eE][xX][iI][sS][tT][sS])");
        boolean isMatch = pattern.matcher(sql).find();
        boolean isSTCode = sql.contains("STCodePoint");
        return parseCreateSQL(sql, isMatch, isSTCode);
    }

    private String parseCreateSQL(String sql, boolean isMatch, boolean isSTCode) {
        try {
            String tableName;
            if (isMatch) {
                tableName = sql.split(" ")[5];
            } else {
                tableName = sql.split(" ")[2];
            }
            List<String> fields = new ArrayList<>();
            fields = generateField(sql, isSTCode);
            if (isEncrypt) {
                phoenixService.createEncrypt(tableName, fields);
            } else {
                phoenixService.create(tableName, fields);
            }
            return "表创建成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "表创建失败";
        }
    }

    /**
     * 生成表的列名
     *
     * @param sql
     * @param isSTCode
     * @return
     */
    private List<String> generateField(String sql, boolean isSTCode) {
        List<String> result = new ArrayList<>();
        int startIndex = sql.lastIndexOf("(");
        int endIndex = sql.lastIndexOf(")");
        sql = sql.substring(startIndex + 1, endIndex).trim();
        String fieldsStr = sql.split(",")[0];
        String id = fieldsStr.split(" ")[0];
        if (id.contains("\"")) {
            id = id.substring(1, id.length() - 1);
        }
        result.add(id);
        for (String field : Constants.FIELDS) {
            result.add(field);
        }
        if (isSTCode) {
            result.add(Constants.STCODE_COLUMN);
        } else {
            for (String fastgeoField : Constants.FASTGEO_FIELDS) {
                result.add(fastgeoField);
            }
        }
        return result;
    }
}
