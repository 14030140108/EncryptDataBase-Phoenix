package com.wl.beans;

import com.wl.stcoder.Cube;
import lombok.Data;

@Data
public class RectNode {

    private double lat;
    private double lon;
    /**
     * STCodeTime中的三个变量：
     * (1) date 传入的日期和时间
     * (2) periodStart 整好整除14天计算出来的时间日期
     * (3) minutes 不足14天的分钟数
     */
    private String time;

    public RectNode() {}

    public RectNode(double lat, double lon, String time) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
    }

    public RectNode(Cube cube) {
        this.lat = cube.getLat() - cube.getLatError() / 2.0D;
        this.lon = cube.getLon() - cube.getLonError() / 2.0D;
        this.time = String.valueOf(cube.getTime() - cube.getTimeError() / 2.0D);
    }
}
