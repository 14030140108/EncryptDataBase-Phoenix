package com.wl.beans.fastgeo;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 *  Author : LinWang
 *  Date : 2020/12/25
 */
@Data
@AllArgsConstructor
public class FastGeoPoint {
    private String id;
    private String lat;
    private String lon;
    private String time;
}
