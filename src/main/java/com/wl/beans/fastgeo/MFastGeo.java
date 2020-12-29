package com.wl.beans.fastgeo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MFastGeo {
    String id;
    int[] m_lon;
    int[] m_time;
    String aesLat;
}
