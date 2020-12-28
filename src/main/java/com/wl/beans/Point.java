package com.wl.beans;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class Point {
    String lat;
    String lon;
    String time;
}
