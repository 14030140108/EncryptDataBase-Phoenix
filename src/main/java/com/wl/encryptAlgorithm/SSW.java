package com.wl.encryptAlgorithm;

import com.wl.Util.Base32Util;
import com.wl.Util.FileUtil;
import com.wl.Util.GetBeanUtil;
import com.wl.beans.CipherText;
import com.wl.beans.Token;
import com.wl.constant.Constants;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import it.unisa.dia.gas.plaf.jpbc.util.ElementUtils;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Component
@Scope("prototype")
public class SSW {

    @Autowired
    FileUtil fileUtil;

    @Autowired
    Base32Util base32Util;

    private SecretKey key = null;
    private PairingParameters typeA1Param = null;
    private Pairing pr = null;
    private Integer length = null;

    @PostConstruct
    public void init() {
        String path = System.getProperty("user.dir") + "/a1.properties";
        if (typeA1Param == null) {
            generate(path);
        }
        if (pr == null) {
            pr = PairingFactory.getPairing(typeA1Param);
        }
    }

    public static void main(String[] args) {
        /*int qBit = 10;
        int numPrime = 4;

        TypeA1CurveGenerator pg = new TypeA1CurveGenerator(numPrime, qBit);
        PairingParameters typeA1Params = pg.generate();
        Pairing pr = PairingFactory.getPairing(typeA1Params);
        Element e1 = pr.getG1().newRandomElement();
        System.out.println(e1);
        byte[] by = e1.toBytes();
        System.out.println(e1.isZero());
        e1 = e1.setToZero();
        e1.setFromBytes(by);
        System.out.println(e1.isZero());*/
    }

    /**
     * 生成 PairingParameters 参数
     *
     * @param path 参数文件路径
     */
    private void generate(String path) {
        PropertiesParameters pp = new PropertiesParameters();
        typeA1Param = pp.load(path);
    }

    public Element getElement() {
        return pr.getG1().newOneElement();
    }

    /**
     * 1.生成密钥
     */
    public void setup(int vectorLength) {
        if (key == null) {
            String path = this.getClass().getResource("/" + Constants.SSW_SECRETKEY).getPath();
            String keyStr = fileUtil.readSSWKey(path, vectorLength);
            if (!StringUtils.isEmpty(keyStr)) {
                SecretKey ky = SecretKey.builder().build();
                key = ky.buildSecretKey(keyStr);
                this.length = key.getH_1_i().size();
                return;
            }
            this.length = vectorLength;
            Element generator = pr.getG1().newRandomElement().getImmutable();
            Element g_p = ElementUtils.getGenerator(pr, generator, typeA1Param, 0, Constants.NUMPRIME).getImmutable();
            Element g_q = ElementUtils.getGenerator(pr, generator, typeA1Param, 1, Constants.NUMPRIME).getImmutable();
            Element g_r = ElementUtils.getGenerator(pr, generator, typeA1Param, 2, Constants.NUMPRIME).getImmutable();
            Element g_s = ElementUtils.getGenerator(pr, generator, typeA1Param, 3, Constants.NUMPRIME).getImmutable();
            List<Element> h_1_i = generateSubGourpList(0);
            List<Element> h_2_i = generateSubGourpList(0);
            List<Element> u_1_i = generateSubGourpList(0);
            List<Element> u_2_i = generateSubGourpList(0);
            key = SecretKey.builder().g_p(g_p).g_q(g_q).g_r(g_r).g_s(g_s)
                    .h_1_i(h_1_i).h_2_i(h_2_i).u_1_i(u_1_i).u_2_i(u_2_i).build();
            fileUtil.writeSSWKey(path, key.toString(), vectorLength);
        }
    }


    /**
     * 2.将给定的向量X加密，返回密文
     *
     * @param x 明文向量
     * @return 密文
     */
    public CipherText encryptVector(int[] x) {
        Element generator = pr.getG1().newRandomElement().getImmutable();
        Element y = pr.getZr().newRandomElement().getImmutable();
        Element z = pr.getZr().newRandomElement().getImmutable();
        Element α = pr.getZr().newRandomElement().getImmutable();
        Element β = pr.getZr().newRandomElement().getImmutable();
        Element S = ElementUtils.getGenerator(pr, generator, typeA1Param, 3, Constants.NUMPRIME).getImmutable();
        Element S0 = ElementUtils.getGenerator(pr, generator, typeA1Param, 3, Constants.NUMPRIME).getImmutable();
        List<Element> R_1_i = generateSubGourpList(2);
        List<Element> R_2_i = generateSubGourpList(2);
        Element C = S.mul(key.getG_p().powZn(y)).getImmutable();
        Element C0 = S0.mul(key.getG_p().powZn(z)).getImmutable();
        List<Element> C_1_i = new ArrayList<>();
        List<Element> C_2_i = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            C_1_i.add(key.getH_1_i().get(i).powZn(y).mul(key.getU_1_i().get(i).powZn(z)).mul(key.getG_q().powZn(α.mul(x[i]))).mul(R_1_i.get(i)));
            C_2_i.add(key.getH_2_i().get(i).powZn(y).mul(key.getU_2_i().get(i).powZn(z)).mul(key.getG_q().powZn(β.mul(x[i]))).mul(R_2_i.get(i)));
        }
        return CipherText.builder().C(C).C0(C0).C_1_i(C_1_i).C_2_i(C_2_i).build();

    }

    /**
     * 生成Token令牌
     *
     * @param v 向量V
     * @return 返回令牌
     */
    public Token genToken(int[] v) {
        setup(v.length);
        Element generator = pr.getG1().newRandomElement().getImmutable();
        Element f1 = pr.getZr().newRandomElement().getImmutable();
        Element f2 = pr.getZr().newRandomElement().getImmutable();
        List<Element> r_1_i = generateZnList();
        List<Element> r_2_i = generateZnList();
        Element R = ElementUtils.getGenerator(pr, generator, typeA1Param, 2, Constants.NUMPRIME).getImmutable();
        Element R0 = ElementUtils.getGenerator(pr, generator, typeA1Param, 2, Constants.NUMPRIME).getImmutable();
        List<Element> S_1_i = generateSubGourpList(3);
        List<Element> S_2_i = generateSubGourpList(3);
        Element K = R, K0 = R0;
        for (int i = 0; i < length; i++) {
            K = K.mul(key.getH_1_i().get(i).powZn(r_1_i.get(i).negate()).mul(key.getH_2_i().get(i).powZn(r_2_i.get(i).negate())));
            K0 = K0.mul(key.getU_1_i().get(i).powZn(r_1_i.get(i).negate()).mul(key.getU_2_i().get(i).powZn(r_2_i.get(i).negate())));
        }

        List<Element> K_1_i = new ArrayList<>();
        List<Element> K_2_i = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            K_1_i.add(key.getG_p().powZn(r_1_i.get(i)).mul(key.getG_q().powZn(f1.mul(v[i]))).mul(S_1_i.get(i)));
            K_2_i.add(key.getG_p().powZn(r_2_i.get(i)).mul(key.getG_q().powZn(f2.mul(v[i]))).mul(S_2_i.get(i)));
        }

        return Token.builder().K(K).K0(K0).K_1_i(K_1_i).K_2_i(K_2_i).build();
    }

    /**
     * 根据生成的密文和令牌计算内积
     *
     * @param ciphertext 密文
     * @param token      令牌
     */
    public Element query(CipherText ciphertext, Token token) {
        Element temp = pr.pairing(ciphertext.getC(), token.getK()).mul(
                pr.pairing(ciphertext.getC0(), token.getK0())).getImmutable();
        for (int i = 0; i < length; i++) {
            temp = temp.mul(pr.pairing(ciphertext.getC_1_i().get(i), token.getK_1_i().get(i))).mul(
                    pr.pairing(ciphertext.getC_2_i().get(i), token.getK_2_i().get(i)));
        }
        return temp;
    }

    /**
     * 在子群中随机生成指定位数个Element
     *
     * @param subgroupIndex 子群索引
     * @return 生成的List列表
     */
    private List<Element> generateSubGourpList(int subgroupIndex) {
        Element generator = pr.getG1().newRandomElement().getImmutable();
        List<Element> result = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Element h_1_i = ElementUtils.getGenerator(pr, generator, typeA1Param, subgroupIndex, Constants.NUMPRIME).getImmutable().getImmutable();
            result.add(h_1_i);
        }
        return result;
    }

    /**
     * 生成ZnList
     *
     * @return 返回List
     */
    private List<Element> generateZnList() {
        List<Element> result = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Element r_1_i = pr.getZr().newRandomElement().getImmutable();
            result.add(r_1_i);
        }
        return result;
    }

}

@Data
@Builder
@SuppressWarnings("ALL")
class SecretKey {

    Element g_p;
    Element g_q;
    Element g_r;
    Element g_s;
    List<Element> h_1_i;
    List<Element> h_2_i;
    List<Element> u_1_i;
    List<Element> u_2_i;

    public String toString() {
        Base32Util base32Util = new Base32Util();
        StringBuilder sb = new StringBuilder();
        sb.append(base32Util.encoder(g_p.toBytes()))
                .append("|")
                .append(base32Util.encoder(g_q.toBytes()))
                .append("|")
                .append(base32Util.encoder(g_r.toBytes()))
                .append("|")
                .append(base32Util.encoder(g_s.toBytes()));

        for (int i = 0; i < h_1_i.size(); i++) {
            sb.append("|")
                    .append(base32Util.encoder(h_1_i.get(i).toBytes()));
        }
        for (int i = 0; i < h_2_i.size(); i++) {
            sb.append("|")
                    .append(base32Util.encoder(h_2_i.get(i).toBytes()));
        }
        for (int i = 0; i < u_1_i.size(); i++) {
            sb.append("|")
                    .append(base32Util.encoder(u_1_i.get(i).toBytes()));
        }
        for (int i = 0; i < u_2_i.size(); i++) {
            sb.append("|")
                    .append(base32Util.encoder(u_2_i.get(i).toBytes()));
        }
        return sb.toString();
    }

    public SecretKey buildSecretKey(String par) {
        SSW ssw = GetBeanUtil.getBean(SSW.class);
        Base32Util base32Util = GetBeanUtil.getBean(Base32Util.class);
        String[] str = par.split("\\|");
        int size = (str.length - 4) / 4;
        Element g_p = ssw.getElement();
        g_p.setFromBytes(base32Util.decoder(str[0]));
        g_p = g_p.getImmutable();
        Element g_q = ssw.getElement();
        g_q.setFromBytes(base32Util.decoder(str[1]));
        g_q = g_q.getImmutable();
        Element g_r = ssw.getElement();
        g_r.setFromBytes(base32Util.decoder(str[2]));
        g_r = g_r.getImmutable();
        Element g_s = ssw.getElement();
        g_s.setFromBytes(base32Util.decoder(str[3]));
        g_s = g_s.getImmutable();
        List<Element> h_1_i = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Element h_1 = ssw.getElement();
            h_1.setFromBytes(base32Util.decoder(str[i + 4]));
            h_1 = h_1.getImmutable();
            h_1_i.add(h_1);
        }
        List<Element> h_2_i = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Element h_2 = ssw.getElement();
            h_2.setFromBytes(base32Util.decoder(str[i + 4 + size]));
            h_2 = h_2.getImmutable();
            h_2_i.add(h_2);
        }

        List<Element> u_1_i = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Element u_1 = ssw.getElement();
            u_1.setFromBytes(base32Util.decoder(str[i + 4 + size * 2]));
            u_1 = u_1.getImmutable();
            u_1_i.add(u_1);
        }
        List<Element> u_2_i = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Element u_2 = ssw.getElement();
            u_2.setFromBytes(base32Util.decoder(str[i + 4 + size * 3]));
            u_2 = u_2.getImmutable();
            u_2_i.add(u_2);
        }
        return SecretKey.builder().g_p(g_p).g_q(g_q).g_r(g_r).g_s(g_s)
                .h_1_i(h_1_i).h_2_i(h_2_i).u_1_i(u_1_i).u_2_i(u_2_i).build();
    }
}
