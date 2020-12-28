package com.wl.beans;

import com.wl.Util.Base32Util;
import com.wl.Util.GetBeanUtil;
import com.wl.constant.Constants;
import com.wl.encryptAlgorithm.SSW;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/*
 *  Author : LinWang
 *  Date : 2020/12/23
 */

@Data
@Builder
@SuppressWarnings("ALL")
public class CipherText {

    private Element C;
    private Element C0;
    private List<Element> C_1_i;
    private List<Element> C_2_i;

    public String toString() {
        Base32Util base32Util = new Base32Util();
        StringBuilder sb = new StringBuilder();
        sb.append(base32Util.encoder(C.toBytes()))
                .append("|")
                .append(base32Util.encoder(C0.toBytes()));
        for (int i = 0; i < C_1_i.size(); i++) {
            sb.append("|")
                    .append(base32Util.encoder(C_1_i.get(i).toBytes()));
        }
        for (int i = 0; i < C_2_i.size(); i++) {
            sb.append("|")
                    .append(base32Util.encoder(C_2_i.get(i).toBytes()));
        }
        return sb.toString();
    }

    public CipherText buildCipherText(String par) {
        SSW ssw = GetBeanUtil.getBean(SSW.class);
        Base32Util base32Util = GetBeanUtil.getBean(Base32Util.class);
        String[] str = par.split("\\|");
        int size = (str.length - 2) / 2;
        Element C = ssw.getElement();
        C.setFromBytes(base32Util.decoder(str[0]));
        C = C.getImmutable();
        Element C0 = ssw.getElement();
        C0.setFromBytes(base32Util.decoder(str[1]));
        C0 = C0.getImmutable();
        List<Element> C_1_i = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Element C_1 = ssw.getElement();
            C_1.setFromBytes(base32Util.decoder(str[i + 2]));
            C_1 = C_1.getImmutable();
            C_1_i.add(C_1);
        }
        List<Element> C_2_i = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Element C_2 = ssw.getElement();
            C_2.setFromBytes(base32Util.decoder(str[i + 2 + size]));
            C_2 = C_2.getImmutable();
            C_2_i.add(C_2);
        }
        return CipherText.builder().C(C).C0(C0).C_1_i(C_1_i).C_2_i(C_2_i).build();
    }
}
