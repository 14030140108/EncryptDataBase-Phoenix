package com.wl.beans;

import com.wl.Util.Base32Util;
import com.wl.Util.GetBeanUtil;
import com.wl.constant.Constants;
import com.wl.encryptAlgorithm.SSW;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/*
 *  Author : LinWang
 *  Date : 2020/12/23
 */

@Data
@Builder
@SuppressWarnings("ALL")
public class Token {

    private Element K;
    private Element K0;
    private List<Element> K_1_i;
    private List<Element> K_2_i;

    public String toString() {
        Base32Util base32Util = new Base32Util();
        StringBuilder sb = new StringBuilder();
        sb.append(base32Util.encoder(K.toBytes()))
                .append("|")
                .append(base32Util.encoder(K0.toBytes()));
        for (int i = 0; i < K_1_i.size(); i++) {
            sb.append("|")
                    .append(base32Util.encoder(K_1_i.get(i).toBytes()));
        }
        for (int i = 0; i < K_2_i.size(); i++) {
            sb.append("|")
                    .append(base32Util.encoder(K_2_i.get(i).toBytes()));
        }
        return sb.toString();
    }

   /* public Token buildToken(String par) {
        SSW ssw = GetBeanUtil.getBean(SSW.class);
        Base32Util base32Util = GetBeanUtil.getBean(Base32Util.class);
        String[] str = par.split("\\|");
        Element K = ssw.getElement();
        K.setFromBytes(base32Util.decoder(str[0]));
        Element K0 = ssw.getElement();
        K0.setFromBytes(base32Util.decoder(str[1]));
        List<Element> K_1_i = new ArrayList<>();
        for (int i = 0; i < Constants.VECTOR_BIT; i++) {
            Element K_1 = ssw.getElement();
            K_1.setFromBytes(base32Util.decoder(str[i + 2]));
            K_1_i.add(K_1);
        }
        List<Element> K_2_i = new ArrayList<>();
        for (int i = 0; i < Constants.VECTOR_BIT; i++) {
            Element K_2 = ssw.getElement();
            K_2.setFromBytes(base32Util.decoder(str[i + 2 + Constants.VECTOR_BIT]));
            K_2_i.add(K_2);
        }
        return Token.builder().K(K).K0(K0).K_1_i(K_1_i).K_2_i(K_2_i).build();
    }*/
}
