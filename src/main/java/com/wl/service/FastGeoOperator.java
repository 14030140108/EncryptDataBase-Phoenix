package com.wl.service;

import com.wl.Util.DateUtil;
import com.wl.Util.TypeUtil;
import com.wl.beans.*;
import com.wl.beans.fastgeo.FastGeoDO;
import com.wl.beans.fastgeo.FastGeoSSW;
import com.wl.beans.fastgeo.MFastGeo;
import com.wl.constant.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*
 *  Author : LinWang
 *  Date : 2020/12/25
 */
@Component
@SuppressWarnings("ALL")
public class FastGeoOperator {

    //密文的二级索引
    private FastGeoPointMap<String, List<FastGeoSSW>> c_data = new FastGeoPointMap<>(Constants.MAP_SIZE);

    //明文的二级索引
    private FastGeoPointMap<String, List<MFastGeo>> m_data = new FastGeoPointMap<>(Constants.MAP_SIZE);

    @PostConstruct
    public void init() {

    }

    public int[] getVectorLon(String lon) {
        int lonInt = TypeUtil.getIntFromString(lon);
        int[] result = new int[361];
        for (int i = -180; i <= 180; i++) {
            result[i + 180] = i == lonInt ? 1 : 0;
        }
        return result;
    }

    public int[] getVectorQueryLon(CuboidNode query) {
        int startLon = TypeUtil.getIntFromDouble(query.getNode1().getLon());
        int endLon = TypeUtil.getIntFromDouble(query.getNode2().getLon());
        int[] result = new int[361];
        for (int i = -180; i <= 180; i++) {
            result[i + 180] = i >= startLon && i <= endLon ? 0 : 1;
        }
        return result;
    }

    public int[] getVectorQueryTime(CuboidNode query) throws ParseException {
        Date startTime = DateUtil.parseDate(query.getNode1().getTime());
        Date endTime = DateUtil.parseDate(query.getNode2().getTime());
        int[] re = new int[366];

        if (endTime.getTime() - startTime.getTime() >= Constants.MAX_MILLSECOND) {
            for (int i = 1; i <= 366; i++) {
                re[i - 1] = 0;
            }
        } else {
            int startDay = Integer.parseInt(String.format("%tj", startTime));
            int endDay = Integer.parseInt(String.format("%tj", endTime));
            boolean isLeapStart = DateUtil.isLeapYear(startTime);
            boolean isLeapEnd = DateUtil.isLeapYear(startTime);
            if (!isLeapStart) {
                startDay = startDay > 59 ? startDay + 1 : startDay;
            }
            if (!isLeapEnd) {
                endDay = endDay > 59 ? endDay + 1 : endDay;
            }
            if (startDay > endDay) {
                for (int i = 1; i <= 366; i++) {
                    re[i - 1] = i > endDay && i < startDay ? 1 : 0;
                }
            } else {
                for (int i = 1; i <= 366; i++) {
                    re[i - 1] = i >= startDay && i <= endDay ? 0 : 1;
                }
            }
        }
        return re;
    }


    public int[] getVectorTime(String time) throws ParseException {
        Date d = DateUtil.parseDate(time);
        int day = Integer.parseInt(String.format("%tj", d));
        boolean isLeap = DateUtil.isLeapYear(d);
        if (!isLeap) {
            day = day > 59 ? day + 1 : day;
        }
        int[] re = new int[366];
        for (int i = 1; i <= 366; i++) {
            re[i - 1] = i == day ? 1 : 0;
        }
        return re;
    }

    public void putData(String key, FastGeoSSW fp) {
        List<FastGeoSSW> fpList = c_data.get(key);
        if (fpList == null) {
            c_data.put(key, Arrays.asList(fp));
        } else {
            fpList.add(fp);
        }
    }

    public void mputData(String key, MFastGeo fp) {
        List<MFastGeo> fpList = m_data.get(key);
        if (fpList == null) {
            m_data.put(key, Arrays.asList(fp));
        } else {
            fpList.add(fp);
        }
    }


    public void putDatas(String key, List<FastGeoSSW> fps) {
        if (c_data.containsKey(key)) {
            c_data.get(key).addAll(fps);
        } else {
            c_data.put(key, fps);
        }
    }

    public void mputDatas(String key, List<MFastGeo> fps) {
        if (m_data.containsKey(key)) {
            m_data.get(key).addAll(fps);
        } else {
            m_data.put(key, fps);
        }
    }


    public List<FastGeoSSW> getDataByKey(String key) {
        return c_data.getOrDefault(key, null);
    }

    public List<MFastGeo> mgetDataByKey(String key) {
        return m_data.getOrDefault(key, null);
    }

    public List<FastGeoSSW> getSSWFromDo(List<FastGeoDO> re) {
        CipherText ct = CipherText.builder().build();
        List<FastGeoSSW> result = new ArrayList<>();
        for (FastGeoDO f : re) {
            FastGeoSSW fg = new FastGeoSSW();
            fg.setId(f.getId());
            fg.setSswLon(ct.buildCipherText(f.getSswLon()));
            fg.setSswTime(ct.buildCipherText(f.getSswTime()));
            result.add(fg);
        }
        return result;
    }

    public List<MFastGeo> getMFromDo(List<FastGeoDO> re) {
        List<MFastGeo> result = new ArrayList<>();
        for (FastGeoDO f : re) {
            MFastGeo fg = new MFastGeo();
            fg.setId(f.getId());
            fg.setM_lon(TypeUtil.getIntArrayFromString(f.getSswLon()));
            fg.setM_time(TypeUtil.getIntArrayFromString(f.getSswTime()));
            result.add(fg);
        }
        return result;
    }

    /**
     * 明文情况下判断两个向量的内积是否为0
     *
     * @param x 向量X
     * @param v 向量V
     * @return 返回向量内积
     * @throws Exception 异常
     */
    public int vectorInner(int[] x, int[] v) throws Exception {
        if (x.length != v.length) {
            throw new Exception("两个向量的长度不一致");
        }
        int result = 0;
        for (int i = 0; i < x.length; i++) {
            result += x[i] * v[i];
        }
        return result;
    }


}
