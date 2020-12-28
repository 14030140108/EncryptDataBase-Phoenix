package com.wl.threadHandler;

import com.wl.Util.GetBeanUtil;
import com.wl.beans.*;
import com.wl.beans.fastgeo.FastGeoPoint;
import com.wl.service.PhoenixService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@SuppressWarnings("ALL")
public class SelectHandler implements Handler {

    private PhoenixService phoenixService = GetBeanUtil.getBean(PhoenixService.class);
    private String sql;
    private boolean isEncrypt;

    public SelectHandler(String sql, boolean isEncrypt) {
        this.sql = sql;
        this.isEncrypt = isEncrypt;
    }

    @Override
    public String executeHandler() {
        boolean isSTCode = sql.contains("STCodeRangeQuery");
        return parseSelectSQL(isSTCode);
    }

    private String parseSelectSQL(boolean isSTCode) {

        try {
            // 获取表名
            String[] str = sql.split(" ");
            String tableName = str[str.length - 1];
            if (tableName.contains(";")) {
                tableName = tableName.substring(0, tableName.length() - 1);
            }
            // 获取查询区域的两个点坐标
            CuboidNode queryRange = getRangeFromSQL();

            if (isSTCode) {
                List<STCodePoint> result = null;
                if (isEncrypt) {
                    result = phoenixService.selectEncryptSTCode(tableName, queryRange);
                } else {
                    result = phoenixService.selectSTCode(tableName, queryRange);
                }
                return toStringSTCode(result);
            } else {
                List<FastGeoPoint> result = null;
                if (isEncrypt) {
                    result = phoenixService.selectEncryptFastGeo(tableName, queryRange);
                } else {
                    result = phoenixService.selectFastGeo(tableName, queryRange);
                }
                return toStringFastGeo(result);
            }
        } catch (Exception e) {
            log.error(e.toString());
            return "数据查询失败";
        }
    }

    private String toStringSTCode(List<STCodePoint> rs) {
        if (rs.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (STCodePoint r : rs) {
            sb.append("维度：" + r.getLat())
                    .append(" , ")
                    .append("经度：" + r.getLon())
                    .append(" , ")
                    .append("时间：" + r.getTime())
                    .append("$");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private String toStringFastGeo(List<FastGeoPoint> rs) {
        if (rs.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (FastGeoPoint r : rs) {
            sb.append("维度：" + r.getLat())
                    .append(" , ")
                    .append("经度：" + r.getLon())
                    .append(" , ")
                    .append("时间：" + r.getTime())
                    .append("$");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 从SQL语句中获取查询的区域
     *
     * @return
     */
    private CuboidNode getRangeFromSQL() {
        int startIndex = sql.indexOf("(");
        int endIndex = sql.lastIndexOf(")");
        sql = sql.substring(startIndex + 1, endIndex).trim();

        //获取左下角的点坐标
        startIndex = sql.indexOf("(");
        endIndex = sql.indexOf(")");
        String leftPoint = sql.substring(startIndex + 1, endIndex).trim();
        String[] leftStr = leftPoint.split(",");
        RectNode node1 = new RectNode();
        node1.setLat(Double.parseDouble(leftStr[0].trim()));
        node1.setLon(Double.parseDouble(leftStr[1].trim()));
        String trim = leftStr[2].trim();
        node1.setTime(trim.contains("\"") ? trim.substring(1, trim.length() - 1) : trim);

        // 获取右上角的点坐标
        startIndex = sql.lastIndexOf("(");
        endIndex = sql.lastIndexOf(")");
        String rightPoint = sql.substring(startIndex + 1, endIndex);
        String[] rightStr = rightPoint.split(",");
        RectNode node2 = new RectNode();
        node2.setLat(Double.parseDouble(rightStr[0].trim()));
        node2.setLon(Double.parseDouble(rightStr[1].trim()));
        trim = rightStr[2].trim();
        node2.setTime(trim.contains("\"") ? trim.substring(1, trim.length() - 1) : trim);


        return new CuboidNode(node1, node2);
    }
}
