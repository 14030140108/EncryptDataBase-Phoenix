package com.wl.beans.fastgeo;

import com.wl.beans.CipherText;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FastGeoSSW {

    private String id;
    private CipherText sswLon;
    private CipherText sswTime;

    public FastGeoSSW(String id, CipherText sswLon, CipherText sswTime) {
        CipherText ct = CipherText.builder().build();
        this.id = id;
        this.sswLon = sswLon;
        this.sswTime = sswTime;
    }

}
