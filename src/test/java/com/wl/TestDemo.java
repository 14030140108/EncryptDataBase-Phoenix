package com.wl;

import com.wl.Util.Base32Util;
import com.wl.Util.DateUtil;
import com.wl.beans.CipherText;
import com.wl.beans.KeyType;
import com.wl.beans.Token;
import com.wl.constant.Constants;
import com.wl.encryptAlgorithm.AES;
import com.wl.encryptAlgorithm.SSW;
import com.wl.mapper.PhoenixMapper;
import com.wl.stcoder.Cube;
import com.wl.stcoder.STCodeTime;
import it.unisa.dia.gas.jpbc.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDemo {

    @Autowired
    private SSW ssw;

    @Autowired
    PhoenixMapper phoenixMapper;

    @Autowired
    Base32Util base32Util;

    @Autowired
    AES aes;

    public static void main(String[] args) throws ParseException {

        String lat = "42.7733";
        String lon = "101.6894";
        String time = "1994-02-10 24:29:16";
        STCodeTime minutes = DateUtil.transSTC(time);

        // 3.计算GeoHash编码
        String stCode = new Cube(Constants.LEVEL, Double.parseDouble(lat), Double.parseDouble(lon), minutes.getMinutes()).getKeyBinVal();
        System.out.println(stCode);
    }

    @Test
    public void testSSW() {

        int[] x = {0, 0, 0, 0, 0, 0, 0, 1, 0, 0};
        int[] v = {1, 0, 0, 0, 0, 0, 1, 0, 1, 1};

        CipherText ciphertext = ssw.encryptVector(x);
        Token token = ssw.genToken(v);
        Element result = ssw.query(ciphertext, token);
        System.out.println(result);
        System.out.println(result.isOne());
    }

    @Test
    public void select() throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {

        String tableName = base32Util.encoder(aes.encrypt("FastGeoTest", KeyType.TABLENAME_ENCRYPT.getValue()));
        List<String> re = phoenixMapper.selectColumn(tableName);
        re = re.stream().filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(re);
    }
}
