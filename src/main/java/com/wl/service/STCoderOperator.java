package com.wl.service;

import com.wl.Util.DateUtil;
import com.wl.beans.CuboidNode;
import com.wl.beans.RectNode;
import com.wl.beans.SpatialRelation;
import com.wl.constant.Constants;
import com.wl.stcoder.Cube;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/*
 *  Author : LinWang
 *  Date : 2020/12/14
 */
@Component
@SuppressWarnings("ALL")
public class STCoderOperator {

    private boolean subQuery = false;

    /**
     * 求解包含给定长方体的最小规范长方体
     *
     * @param node1 左下角坐标
     * @param node2 右上角坐标
     * @return 长方体
     */
    public CuboidNode transRectNode(RectNode node1, RectNode node2) throws ParseException {

        Date node1Date = DateUtil.parseDate(node1.getTime());
        Date node2Date = DateUtil.parseDate(node2.getTime());
        double node1Time, node2Time;
        if (node2Date.getTime() - node1Date.getTime() > Constants.MAX_MILLSECOND) {
            node1Time = 0;
            node2Time = (Constants.TIME_RANGE / ((int) Math.pow(2, Constants.LEVEL) * 1.0)) * (Math.pow(2, Constants.LEVEL) - 1);
        } else {
            node1Time = DateUtil.transSTC(node1).getMinutes();
            node2Time = DateUtil.transSTC(node2).getMinutes();
        }
        if (node1Time > node2Time) {
            subQuery = true;
        }
        Cube node1Cube = new Cube(Constants.LEVEL, node1.getLat(), node1.getLon(), node1Time);
        Cube node2Cube = new Cube(Constants.LEVEL, node2.getLat(), node2.getLon(), node2Time);
        return new CuboidNode(new RectNode(node1Cube), new RectNode(node2Cube));
    }

    /**
     * 长方体空间查询
     *
     * @param cuboidNode 长方体查询区域
     * @throws ParseException 时间信息解析异常
     */
    public void rectSearch(CuboidNode cuboidNode, List<String[]> intevals) throws ParseException {
        Cube node1Cube = new Cube(Constants.LEVEL, cuboidNode.getNode1().getLat(), cuboidNode.getNode1().getLon(), Double.parseDouble(cuboidNode.getNode1().getTime()));
        Cube node2Cube = new Cube(Constants.LEVEL, cuboidNode.getNode2().getLat(), cuboidNode.getNode2().getLon(), Double.parseDouble(cuboidNode.getNode2().getTime()));
        String prefix = longestCommonPrefix(node1Cube.getKeyBinVal(), node2Cube.getKeyBinVal());
        CuboidNode queryNode = new CuboidNode(new RectNode(node1Cube), new RectNode(node2Cube));
        rectToIntervals(queryNode, prefix, intevals);
        combineIntervals(intevals);
    }

    /**
     * 求解两个二进制字符串的最长公共前缀
     *
     * @param key1 字符串1
     * @param key2 字符串2
     * @return 最长公共前缀
     */
    private String longestCommonPrefix(String key1, String key2) {
        StringBuilder sb = new StringBuilder();
        char[] key1Chars = key1.toCharArray();
        char[] key2Chars = key2.toCharArray();
        for (int i = 0; i < key1Chars.length; i++) {
            if (key1Chars[i] != key2Chars[i]) {
                break;
            } else {
                sb.append(key1Chars[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 通过查询区域和公共前缀求解带查询区域链表
     *
     * @param cuboidNode1 查询区域对应的长方体节点信息
     * @param prefix      公共前缀编码
     */
    private void rectToIntervals(CuboidNode cuboidNode1, String prefix, List<String[]> intervals) {
        //公共前缀对应的外接长方体
        CuboidNode cuboidNode2 = prefixToRect(prefix);
        SpatialRelation recLocate = judgeRelation(cuboidNode1, cuboidNode2);
        if (recLocate == SpatialRelation.CONTAIN) {
            intervals.add(cuboidToInterval(cuboidNode2));
        } else if (recLocate == SpatialRelation.INTERSECT) {
            if (prefix.length() >= Constants.MAX_LENGTH) {
                intervals.add(cuboidToInterval(cuboidNode2));
            } else {
                rectToIntervals(cuboidNode1, prefix + "0", intervals);
                rectToIntervals(cuboidNode1, prefix + "1", intervals);
            }
        }
    }

    //根据公共前缀编码求解外接长方体
    private CuboidNode prefixToRect(String prefix) {
        StringBuilder lons = new StringBuilder();
        StringBuilder lats = new StringBuilder();
        StringBuilder times = new StringBuilder();
        int i;
        for (i = 0; i < prefix.length(); i += 3) {
            lons.append(prefix.charAt(i));
        }
        for (i = 1; i < prefix.length(); i += 3) {
            lats.append(prefix.charAt(i));
        }
        for (i = 2; i < prefix.length(); i += 3) {
            times.append(prefix.charAt(i));
        }
        double[] lonsRange = calcRangeFromPrefix("longitude", lons.toString());
        //System.out.println("经度范围：" + lonsRange[0] + " - " + lonsRange[1]);
        double[] latsRange = calcRangeFromPrefix("latitude", lats.toString());
        //System.out.println("纬度范围：" + latsRange[0] + " - " + latsRange[1]);
        double[] timesRange = calcRangeFromPrefix("time", times.toString());
        //System.out.println("时间范围：" + timesRange[0] + " - " + timesRange[1]);

        RectNode node1 = new RectNode(latsRange[0], lonsRange[0], String.valueOf(timesRange[0]));
        RectNode node2 = new RectNode(latsRange[1] - Constants.LAT_SIZE, lonsRange[1] - Constants.LON_SIZE, String.valueOf(timesRange[1] - Constants.TIME_SIZE));
        return new CuboidNode(node1, node2);
    }

    //根据指定的前缀编码得到每个维度的取值范围，进而求解最大外接矩形
    private double[] calcRangeFromPrefix(String type, String prefix) {
        double[] result = new double[2];
        if (type.equals("longitude")) {
            result[0] = -180.0D;
            result[1] = 180.0D;

        } else if (type.equals("latitude")) {
            result[0] = -90.0D;
            result[1] = 90.0D;
        } else {
            result[0] = 0D;
            result[1] = Constants.TIME_RANGE;
        }

        char[] lonChars = prefix.toCharArray();
        for (char lonChar : lonChars) {
            double mid = (result[0] + result[1]) / 2;
            if (lonChar == '0') {
                result[1] = mid;
            } else if (lonChar == '1') {
                result[0] = mid;
            }
        }
        return result;
    }

    /**
     * 判断两个长方体的空间位置关系
     *
     * @param cuboidNode1 待查询区域长方体
     * @param cuboidNode2 前缀编码对应的外接长方体
     * @return 返回空间位置关系Spatial：
     * （1）CONTAIN：表示待查询区域包含外接区域
     * （2）INTERSECT：表示两个查询区域相交，当外接区域包含待查询区域时，也属于相交关系
     * （3）DISJOINT：表示两个查询区域相离
     */
    private SpatialRelation judgeRelation(CuboidNode cuboidNode1, CuboidNode cuboidNode2) {
        RectNode[] rectNode1 = generateAllNode(cuboidNode1);
        RectNode[] rectNode2 = generateAllNode(cuboidNode2);

        boolean[] cub2Iscub1 = new boolean[8];
        for (int i = 0; i < rectNode2.length; i++) {
            cub2Iscub1[i] = isContain(rectNode2[i], cuboidNode1);
        }
        boolean[] cub1Iscub2 = new boolean[8];
        for (int i = 0; i < rectNode1.length; i++) {
            cub1Iscub2[i] = isContain(rectNode1[i], cuboidNode2);
        }
        int count_true = 0;
        int count_false = 0;

        for (boolean b : cub2Iscub1) {
            if (b == true)
                count_true++;
            else
                count_false++;
        }
        for (boolean b : cub1Iscub2) {
            if (b == false)
                count_false++;
        }
        if (count_true == cub2Iscub1.length)
            return SpatialRelation.CONTAIN;

        if (count_false == cub1Iscub2.length + cub2Iscub1.length) {
            if (isDisJoint(cuboidNode1, cuboidNode2)) {
                return SpatialRelation.DISJOINT;
            } else {
                return SpatialRelation.INTERSECT;
            }
        }
        return SpatialRelation.INTERSECT;
    }

    private boolean isDisJoint(CuboidNode cuboidNode1, CuboidNode cuboidNode2) {
        double node1Time1 = Double.parseDouble(cuboidNode1.getNode1().getTime());
        double node1Time2 = Double.parseDouble(cuboidNode1.getNode2().getTime());
        double node2Time1 = Double.parseDouble(cuboidNode2.getNode1().getTime());
        double node2Time2 = Double.parseDouble(cuboidNode2.getNode2().getTime());
        if (cuboidNode1.getNode1().getLat() >= cuboidNode2.getNode2().getLat() || cuboidNode1.getNode2().getLat() <= cuboidNode2.getNode1().getLat()
                || cuboidNode1.getNode1().getLon() >= cuboidNode2.getNode2().getLon() || cuboidNode1.getNode2().getLon() <= cuboidNode2.getNode1().getLon()
                || node1Time1 >= node2Time2 || node1Time2 <= node2Time1)
            return true;
        else
            return false;
    }

    /**
     * 根据长方体左下角和右上角的坐标生成长方体八个顶点的坐标数组
     *
     * @param cuboidNode 长方体坐标信息
     * @return 长方体所有顶点集合
     */
    private RectNode[] generateAllNode(CuboidNode cuboidNode) {
        RectNode[] rn = new RectNode[8];
        rn[0] = new RectNode(cuboidNode.getNode1().getLat(), cuboidNode.getNode2().getLon(), cuboidNode.getNode2().getTime());
        rn[1] = cuboidNode.getNode2();
        rn[2] = new RectNode(cuboidNode.getNode1().getLat(), cuboidNode.getNode2().getLon(), cuboidNode.getNode1().getTime());
        rn[3] = new RectNode(cuboidNode.getNode2().getLat(), cuboidNode.getNode2().getLon(), cuboidNode.getNode1().getTime());
        rn[4] = new RectNode(cuboidNode.getNode1().getLat(), cuboidNode.getNode1().getLon(), cuboidNode.getNode2().getTime());
        rn[5] = new RectNode(cuboidNode.getNode2().getLat(), cuboidNode.getNode1().getLon(), cuboidNode.getNode2().getTime());
        rn[6] = cuboidNode.getNode1();
        rn[7] = new RectNode(cuboidNode.getNode2().getLat(), cuboidNode.getNode1().getLon(), cuboidNode.getNode1().getTime());
        return rn;
    }

    /**
     * 判断一个立方体是否在一个长方体内部
     *
     * @param node       立方体的左下角坐标
     * @param cuboidNode 长方体左下角和右上角坐标
     * @return true 表示：在内部
     * false表示：不在内部
     */
    private boolean isContain(RectNode node, CuboidNode cuboidNode) {
        //长方体左下角坐标
        double lat1 = cuboidNode.getNode1().getLat();
        double lon1 = cuboidNode.getNode1().getLon();
        double time1 = Double.valueOf(cuboidNode.getNode1().getTime());

        //长方体右上角坐标
        double lat2 = cuboidNode.getNode2().getLat();
        double lon2 = cuboidNode.getNode2().getLon();
        double time2 = Double.valueOf(cuboidNode.getNode2().getTime());

        //立方体坐标
        double lat3 = node.getLat();
        double lon3 = node.getLon();
        double time3 = Double.valueOf(node.getTime());

        return lat3 >= lat1 && lat3 <= lat2 && lon3 >= lon1 && lon3 <= lon2 && time3 >= time1 && time3 <= time2;
    }

    /**
     * 将长方体坐标转换成字符串数组
     *
     * @param cuboidNode 长方体坐标信息
     * @return 字符串数组链表
     */
    private String[] cuboidToInterval(CuboidNode cuboidNode) {
        Cube cubeL = new Cube(Constants.LEVEL, cuboidNode.getNode1().getLat(), cuboidNode.getNode1().getLon(), Double.valueOf(cuboidNode.getNode1().getTime()));
        Cube cubeR = new Cube(Constants.LEVEL, cuboidNode.getNode2().getLat(), cuboidNode.getNode2().getLon(), Double.valueOf(cuboidNode.getNode2().getTime()));
        return new String[]{cubeL.getKeyBinVal(), cubeR.getKeyBinVal()};
    }

    /**
     * 合并链表区间
     */
    private void combineIntervals(List<String[]> intervals) {
        for (int i = intervals.size() - 1; i > 0; i--) {
            String[] temp1 = intervals.get(i);
            String[] temp2 = intervals.get(i - 1);
            int num1 = Integer.valueOf(temp2[temp2.length - 1], 2);
            int num2 = Integer.valueOf(temp1[0], 2);
            if (num1 == num2 - 1) {
                temp2[temp2.length - 1] = temp1[temp1.length - 1];
                intervals.remove(i);
            }
        }
    }

    public boolean isSubQuery() {
        return subQuery;
    }

    public void setSubQuery(boolean subQuery) {
        this.subQuery = subQuery;
    }
}
