package com.wl.beans.fastgeo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FastGeoDO {

    private String id;
    private String sswLon;
    private String sswTime;
    private String aesLat;

    public FastGeoDO(String id, String sswLon, String sswTime) {
        this.id = id;
        this.sswLon = sswLon;
        this.sswTime = sswTime;
    }

}
