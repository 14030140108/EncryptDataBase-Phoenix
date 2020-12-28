package com.wl.beans;

import lombok.Data;

@Data
public class CuboidNode {

    //长方体左下角方块信息
    private RectNode node1;

    //长方体右上角方块信息
    private RectNode node2;

    public CuboidNode(RectNode node1, RectNode node2) {
        this.node1 = node1;
        this.node2 = node2;
    }
}
