package com.wl.beans;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 *  Author : LinWang
 *  Date : 2020/12/25
 */
public class FastGeoPointMap<k, v> extends LinkedHashMap<k, v> {

    private int maxSize;

    public FastGeoPointMap(int maxSize) {
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxSize;
    }


}
