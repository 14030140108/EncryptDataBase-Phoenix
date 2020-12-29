package com.wl.beans.fastgeo;

import com.wl.beans.CipherText;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FastGeoSSW {

    private String id;
    private String aesLat;
    private CipherText sswLon;
    private CipherText sswTime;

    public FastGeoSSW(String id, String aesLat, CipherText sswLon, CipherText sswTime) {
        CipherText ct = CipherText.builder().build();
        this.id = id;
        this.aesLat = aesLat;
        this.sswLon = sswLon;
        this.sswTime = sswTime;
    }

}
