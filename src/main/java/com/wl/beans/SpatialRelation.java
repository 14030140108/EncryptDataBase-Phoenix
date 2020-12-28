package com.wl.beans;

public enum SpatialRelation {

    /**
     * 包含
     */
    CONTAIN(0),

    /**
     * 相交
     */
    INTERSECT(1),

    /**
     * 相离
     */
    DISJOINT(2);

    SpatialRelation(int value) {
        this.value = value;
    }

    public int value;

}
