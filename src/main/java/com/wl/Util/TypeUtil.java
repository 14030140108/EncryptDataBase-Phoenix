package com.wl.Util;

/*
 *  Author : LinWang
 *  Date : 2020/12/25
 */
public class TypeUtil {

    public static int getIntFromString(String param) {
        double p = Double.parseDouble(param);
        return getIntFromDouble(p);
    }

    public static int getIntFromDouble(double param) {
        return (int) Math.floor(param);
    }

    public static int[] getIntArrayFromString(String par) {
        par = par.substring(1, par.length() - 1);
        String[] parStr = par.split(",");
        int[] re = new int[parStr.length];
        for (int i = 0; i < parStr.length; i++) {
            re[i] = Integer.parseInt(parStr[i].trim());
        }
        return re;
    }
}
