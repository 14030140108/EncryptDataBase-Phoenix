package com.wl.Util;

import com.buck.common.codec.Base32;
import org.springframework.stereotype.Component;

@Component
public class Base32Util {

    private Base32 base32 = new Base32();

    public String encoder(byte[] src) {
        return new String(base32.newEncoder().encode(src)).replaceAll("=", "_");
    }

    public byte[] decoder(String src) {
        if (src == null) {
            return null;
        }
        src = src.replaceAll("_", "=");
        return base32.newDecoder().decode(src.getBytes());
    }
}
