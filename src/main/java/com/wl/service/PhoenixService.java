package com.wl.service;

import com.wl.Util.Base32Util;
import com.wl.Util.DateUtil;
import com.wl.Util.TypeUtil;
import com.wl.beans.*;
import com.wl.beans.fastgeo.FastGeoDO;
import com.wl.beans.fastgeo.FastGeoPoint;
import com.wl.beans.fastgeo.FastGeoSSW;
import com.wl.beans.fastgeo.MFastGeo;
import com.wl.constant.Constants;
import com.wl.encryptAlgorithm.AES;
import com.wl.encryptAlgorithm.OPE;
import com.wl.encryptAlgorithm.SSW;
import com.wl.mapper.PhoenixMapper;
import com.wl.stcoder.Cube;
import com.wl.stcoder.STCodeTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("ALL")
public class PhoenixService {

    private PhoenixMapper phoenixMapper;
    private STCoderOperator stCoderOperator;
    private FastGeoOperator fastGeoOperator;
    private AES aes;
    private OPE ope;
    private SSW sswLon;
    private SSW sswTime;
    private Base32Util base32Util = new Base32Util();

    @Autowired
    public PhoenixService(PhoenixMapper phoenixMapper, AES aes, OPE ope, SSW sswLon, SSW sswTime, STCoderOperator stCoderOperator, FastGeoOperator fastGeoOperator) {
        this.aes = aes;
        this.ope = ope;
        this.sswLon = sswLon;
        this.sswTime = sswTime;
        this.stCoderOperator = stCoderOperator;
        this.fastGeoOperator = fastGeoOperator;
        this.phoenixMapper = phoenixMapper;
    }

    /**
     * 加密表名和字段名，并创建表
     *
     * @param fields    字段名
     * @param tableName 表名
     */
    public void createEncrypt(String tableName, List<String> fields) throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {

        long start = System.currentTimeMillis();
        tableName = base32Util.encoder(aes.encrypt(tableName, KeyType.TABLENAME_ENCRYPT.getValue()));
        List<String> encryptFields = new ArrayList<>();
        for (String s : fields) {
            String field = base32Util.encoder(aes.encrypt(s, KeyType.TABLENAME_ENCRYPT.getValue()));
            encryptFields.add(field);
        }
        phoenixMapper.createTable(tableName, encryptFields);
        phoenixMapper.createIndex(tableName, encryptFields);
        long end = System.currentTimeMillis();
        System.out.println("密文创建数据表的时间：" + (end - start) * 1.0 / 1000 + "秒");
    }

    /**
     * 创建表
     *
     * @param tableName 表名
     * @param fields    字段名
     */
    public void create(String tableName, List<String> fields) throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {
        long start = System.currentTimeMillis();
        phoenixMapper.createTable(tableName, fields);
        phoenixMapper.createIndex(tableName, fields);
        long end = System.currentTimeMillis();
        System.out.println("明文创建数据表的时间：" + (end - start) * 1.0 / 1000 + "秒");
    }

    /**
     * STCode方案加密插入数据
     *
     * @param tableName 表名
     * @param data      数据
     */
    public void insertEncryptSTCode(String tableName, List<String> data) throws Exception {

        long start = System.currentTimeMillis();
        // 1.表名加密
        tableName = base32Util.encoder(aes.encrypt(tableName, KeyType.TABLENAME_ENCRYPT.getValue()));

        // 2.获取插入的经纬度和时间
        String lat = data.get(1);
        String lon = data.get(2);
        String time = data.get(3);
        STCodeTime minutes = DateUtil.transSTC(time);

        // 3.计算GeoHash编码
        String stCode = new Cube(Constants.LEVEL, Double.parseDouble(lat), Double.parseDouble(lon), minutes.getMinutes()).getKeyBinVal();
        data.add(stCode);

        // 4.对data进行加密，其中 4列采用AES加密算法，GeoHash采用OPE保序加密算法
        List<String> c_data = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if (i != data.size() - 1) {
                String c_temp = base32Util.encoder(aes.encrypt(data.get(i), KeyType.TABLEDATA_ENCRYPT.getValue()));
                c_data.add(c_temp);
            } else {
                String c_temp = ope.encryptGeohash(data.get(i));
                c_data.add(c_temp);
            }
        }

        // 5.插入数据
        phoenixMapper.insert(tableName, c_data);
        long end = System.currentTimeMillis();
        System.out.println("加密后插入数据的时间：" + (end - start) * 1.0 / 1000 + "秒");
    }

    public void insertSTCode(String tableName, List<String> data) throws Exception {

        long start = System.currentTimeMillis();
        // 1.获取插入的经纬度和时间
        String lat = data.get(1);
        String lon = data.get(2);
        String time = data.get(3);
        STCodeTime minutes = DateUtil.transSTC(time);

        // 2.计算GeoHash编码
        String stCode = new Cube(Constants.LEVEL, Double.parseDouble(lat), Double.parseDouble(lon), minutes.getMinutes()).getKeyBinVal();
        data.add(stCode);

        // 3.插入数据
        phoenixMapper.insert(tableName, data);
        long end = System.currentTimeMillis();
        System.out.println("明文插入数据的时间：" + (end - start) * 1.0 / 1000 + "秒");
    }

    /**
     * STCode方案密文上查询数据
     *
     * @param tableName 表名
     * @param points    查询区域的点坐标
     */
    public List<STCodePoint> selectEncryptSTCode(String tableName, CuboidNode points) throws Exception {
        long start = System.currentTimeMillis();
        List<STCodePoint> rs = new ArrayList<>();
        List<String[]> intervals = new ArrayList<>();
        CuboidNode cuboidNode = stCoderOperator.transRectNode(points.getNode1(), points.getNode2());
        if (stCoderOperator.isSubQuery()) {
            String endTime = String.valueOf((Constants.TIME_RANGE / ((int) Math.pow(2, Constants.LEVEL) * 1.0)) * (Math.pow(2, Constants.LEVEL) - 1));
            CuboidNode cuboidNode1 = new CuboidNode(new RectNode(cuboidNode.getNode1().getLat(), cuboidNode.getNode1().getLon(), cuboidNode.getNode1().getTime()),
                    new RectNode(cuboidNode.getNode1().getLat(), cuboidNode.getNode1().getLon(), endTime));

            CuboidNode cuboidNode2 = new CuboidNode(new RectNode(cuboidNode.getNode2().getLat(), cuboidNode.getNode2().getLon(), "0"),
                    new RectNode(cuboidNode.getNode2().getLat(), cuboidNode.getNode2().getLon(), cuboidNode.getNode2().getTime()));
            rectQueryEncrypt(cuboidNode1, tableName, rs, intervals);
            rectQueryEncrypt(cuboidNode2, tableName, rs, intervals);
        } else {
            rectQueryEncrypt(cuboidNode, tableName, rs, intervals);
        }

        // 解密数据
        rs = decryptPoint(rs);

        // 过滤数据点
        rs = filterPoint(rs, points);

        long end = System.currentTimeMillis();
        System.out.println("在密文上查询数据的时间：" + (end - start) * 1.0 / 1000 + "秒");
        return rs;
    }

    private List<STCodePoint> filterPoint(List<STCodePoint> rs, CuboidNode points) throws ParseException {
        List<STCodePoint> result = new ArrayList<>();
        for (STCodePoint r : rs) {
            if (isContain(r, points)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<STCodePoint> selectSTCode(String tableName, CuboidNode points) throws Exception {
        long start = System.currentTimeMillis();
        List<STCodePoint> rs = new ArrayList<>();
        List<String[]> intervals = new ArrayList<>();
        CuboidNode cuboidNode = stCoderOperator.transRectNode(points.getNode1(), points.getNode2());
        if (stCoderOperator.isSubQuery()) {
            CuboidNode cuboidNode2 = new CuboidNode(new RectNode(cuboidNode.getNode2().getLat(), cuboidNode.getNode2().getLon(), "0"),
                    new RectNode(cuboidNode.getNode2().getLat(), cuboidNode.getNode2().getLon(), cuboidNode.getNode2().getTime()));
            String endTime = String.valueOf((Constants.TIME_RANGE / ((int) Math.pow(2, Constants.LEVEL) * 1.0)) * (Math.pow(2, Constants.LEVEL) - 1));
            CuboidNode cuboidNode1 = new CuboidNode(new RectNode(cuboidNode.getNode1().getLat(), cuboidNode.getNode1().getLon(), cuboidNode.getNode1().getTime()),
                    new RectNode(cuboidNode.getNode1().getLat(), cuboidNode.getNode1().getLon(), endTime));

            rectQuery(cuboidNode1, tableName, rs, intervals);
            rectQuery(cuboidNode2, tableName, rs, intervals);
        } else {
            rectQuery(cuboidNode, tableName, rs, intervals);
        }

        // 过滤数据点
        rs = filterPoint(rs, points);

        long end = System.currentTimeMillis();
        System.out.println("在明文上查询数据的时间：" + (end - start) * 1.0 / 1000 + "秒");
        return rs;
    }

    /**
     * 判断点是否在查询区域里面
     *
     * @param st     点
     * @param points 查询区域
     * @return true：在 | false ： 不在
     * @throws ParseException 异常
     */
    private boolean isContain(STCodePoint st, CuboidNode points) throws ParseException {
        double lat = Double.parseDouble(st.getLat());
        double lon = Double.parseDouble(st.getLon());
        long time = DateUtil.parseDate(st.getTime()).getTime();

        double startLat = points.getNode1().getLat();
        double endLat = points.getNode2().getLat();
        double startLon = points.getNode1().getLon();
        double endLon = points.getNode2().getLon();
        long startTime = DateUtil.parseDate(points.getNode1().getTime()).getTime();
        long endTime = DateUtil.parseDate(points.getNode2().getTime()).getTime();

        return lat >= startLat && lat <= endLat && lon >= startLon && lon <= endLon && time >= startTime && time <= endTime;
    }

    /**
     * 解密加密的点数据
     *
     * @return 明文数据
     */
    private List<STCodePoint> decryptPoint(List<STCodePoint> rs) throws Exception {
        for (STCodePoint r : rs) {
            r.setId(aes.decrypt(base32Util.decoder(r.getId()), KeyType.TABLEDATA_ENCRYPT.getValue()));
            r.setLat(aes.decrypt(base32Util.decoder(r.getLat()), KeyType.TABLEDATA_ENCRYPT.getValue()));
            r.setLon(aes.decrypt(base32Util.decoder(r.getLon()), KeyType.TABLEDATA_ENCRYPT.getValue()));
            r.setTime(aes.decrypt(base32Util.decoder(r.getTime()), KeyType.TABLEDATA_ENCRYPT.getValue()));
        }
        return rs;
    }


    /**
     * 经度、纬度和时间三个纬度构成的长方体的空间加密范围查询
     *
     * @param cuboidNode 查询区域所在的规范长方体
     * @throws ParseException 解析异常
     * @throws IOException    IO异常
     */
    private void rectQueryEncrypt(CuboidNode cuboidNode, String tableName, List<STCodePoint> rs, List<String[]> intervals) throws Exception {

        // 1.表名加密
        tableName = base32Util.encoder(aes.encrypt(tableName, KeyType.TABLENAME_ENCRYPT.getValue()));

        String column = base32Util.encoder(aes.encrypt(Constants.STCODE_COLUMN, KeyType.TABLENAME_ENCRYPT.getValue()));

        stCoderOperator.rectSearch(cuboidNode, intervals);
        System.out.print("生成的区间链表为: ");
        intervals.forEach(t1 -> {
            System.out.print("[");
            for (String s : t1) {
                System.out.print(s + " ");
            }
            System.out.print("] ");
        });
        System.out.println();

        //直接在数据库上密文进行查询
        for (String[] interval : intervals) {
            interval[0] = ope.encryptGeohash(interval[0]);
            interval[1] = ope.encryptGeohash(interval[1]);
            List<STCodePoint> result = phoenixMapper.select(tableName, column, interval[0], interval[1]);
            rs.addAll(result);
        }
    }

    /**
     * 经度、纬度和时间三个纬度构成的长方体的空间范围查询
     *
     * @param cuboidNode 查询区域所在的规范长方体
     * @throws ParseException 解析异常
     * @throws IOException    IO异常
     */
    private void rectQuery(CuboidNode cuboidNode, String tableName, List<STCodePoint> rs, List<String[]> intervals) throws Exception {

        String column = Constants.STCODE_COLUMN;

        stCoderOperator.rectSearch(cuboidNode, intervals);
        System.out.print("生成的区间链表为: ");
        intervals.forEach(t1 -> {
            System.out.print("[");
            for (String s : t1) {
                System.out.print(s + " ");
            }
            System.out.print("] ");
        });
        System.out.println();

        for (String[] interval : intervals) {
            List<STCodePoint> result = phoenixMapper.select(tableName, column, interval[0], interval[1]);
            rs.addAll(result);
        }
    }

    // -------------------------------FastGeo方案 ------------------------------------------------------------------------------------------------

    /**
     * 采用SSW和AES加密算法加密数据，并插入数据库
     *
     * @param tableName
     * @param data
     * @throws Exception
     */
    public void insertEncryptFastGeo(String tableName, List<String> data) throws Exception {
        long start = System.currentTimeMillis();
        // 1.表名加密
        tableName = base32Util.encoder(aes.encrypt(tableName, KeyType.TABLENAME_ENCRYPT.getValue()));

        // 2.获取插入的经纬度和时间
        String lat = data.get(1);
        String lon = data.get(2);
        String time = data.get(3);

        // 3. 使用SSW算法处理三个维度的数据
        int[] m_lon = fastGeoOperator.getVectorLon(lon);
        int[] m_time = fastGeoOperator.getVectorTime(time);
        sswLon.setup(m_lon.length);
        sswTime.setup(m_time.length);
        CipherText ctLon = sswLon.encryptVector(m_lon);
        CipherText ctTime = sswTime.encryptVector(m_time);

        String ssw_lon = ctLon.toString();
        String ssw_time = ctTime.toString();
        lat = String.valueOf(TypeUtil.getIntFromString(lat));

        List<String> c_data = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String c_temp = base32Util.encoder(aes.encrypt(data.get(i), KeyType.TABLEDATA_ENCRYPT.getValue()));
            c_data.add(c_temp);
        }
        c_data.add(ssw_lon);
        c_data.add(ssw_time);
        c_data.add(base32Util.encoder(aes.encrypt(lat, KeyType.TABLEDATA_ENCRYPT.getValue())));

        FastGeoSSW fp = new FastGeoSSW(c_data.get(0), ctLon, ctTime);
        fastGeoOperator.putData(c_data.get(c_data.size() - 1), fp);
        // 4. 插入数据
        phoenixMapper.insert(tableName, c_data);
        long end = System.currentTimeMillis();
        System.out.println("加密后插入数据的时间：" + (end - start) * 1.0 / 1000 + "秒");
    }

    public void insertFastGeo(String tableName, List<String> data) throws Exception {
        long start = System.currentTimeMillis();

        String lat = data.get(1);
        String lon = data.get(2);
        String time = data.get(3);

        // 3. 使用SSW算法处理经度和时间的数据
        int[] m_lon = fastGeoOperator.getVectorLon(lon);
        int[] m_time = fastGeoOperator.getVectorTime(time);
        lat = String.valueOf(TypeUtil.getIntFromString(lat));

        data.add(Arrays.toString(m_lon));
        data.add(Arrays.toString(m_time));
        data.add(lat);

        MFastGeo fg = new MFastGeo(data.get(0), m_lon, m_time);
        fastGeoOperator.mputData(lat, fg);
        // 4. 插入数据
        phoenixMapper.insert(tableName, data);
        long end = System.currentTimeMillis();
        System.out.println("明文插入数据的时间：" + (end - start) * 1.0 / 1000 + "秒");
    }

    public List<FastGeoPoint> selectEncryptFastGeo(String tableName, CuboidNode points) throws Exception {
        long start = System.currentTimeMillis();

        // 1. 获取起始的纬度坐标范围
        int startLat = TypeUtil.getIntFromDouble(points.getNode1().getLat());
        int endLat = TypeUtil.getIntFromDouble(points.getNode2().getLat());

        // 2. 表名加密
        tableName = base32Util.encoder(aes.encrypt(tableName, KeyType.TABLENAME_ENCRYPT.getValue()));

        // 3 . 获取查询的列名
        List<String> re = phoenixMapper.selectColumn(tableName).stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<String> columns = new ArrayList<>();
        for (String s : re) {
            String de = aes.decrypt(base32Util.decoder(s), KeyType.TABLENAME_ENCRYPT.getValue());
            boolean idFlag = Pattern.compile("([Ii][Dd])").matcher(de).find();
            if (idFlag) {
                columns.add(s);
            }
        }
        columns.add(base32Util.encoder(aes.encrypt(Constants.FASTGEO_FIELDS[0], KeyType.TABLENAME_ENCRYPT.getValue())));
        columns.add(base32Util.encoder(aes.encrypt(Constants.FASTGEO_FIELDS[1], KeyType.TABLENAME_ENCRYPT.getValue())));
        columns.add(base32Util.encoder(aes.encrypt(Constants.FASTGEO_FIELDS[2], KeyType.TABLENAME_ENCRYPT.getValue())));

        //获取待查询区域加密后的经度和时间
        Token ssw_lon = sswLon.genToken(fastGeoOperator.getVectorQueryLon(points));
        Token ssw_time = sswTime.genToken(fastGeoOperator.getVectorQueryTime(points));

        List<String> ids = new ArrayList<>();
        for (int i = startLat; i <= endLat; i++) {
            String key = base32Util.encoder(aes.encrypt(String.valueOf(i), KeyType.TABLEDATA_ENCRYPT.getValue()));
            List<FastGeoSSW> value = fastGeoOperator.getDataByKey(key);
            if (value == null || value.size() == 0) {
                List<FastGeoDO> res = phoenixMapper.selectFastGeoSSWByAESLat(tableName, key, columns);
                value = fastGeoOperator.getSSWFromDo(res);
                if (value == null || value.size() == 0) {
                    continue;
                }
                fastGeoOperator.putDatas(key, value);
            }

            for (FastGeoSSW fp : value) {
                if (sswLon.query(fp.getSswLon(), ssw_lon).isOne() && sswTime.query(fp.getSswTime(), ssw_time).isOne()) {
                    ids.add(fp.getId());
                }
            }
        }

        List<FastGeoPoint> fgs = new ArrayList<>();

        List<String> cl = new ArrayList<>();
        cl.add(columns.get(0));
        cl.add(base32Util.encoder(aes.encrypt(Constants.FIELDS[0], KeyType.TABLENAME_ENCRYPT.getValue())));
        cl.add(base32Util.encoder(aes.encrypt(Constants.FIELDS[1], KeyType.TABLENAME_ENCRYPT.getValue())));
        cl.add(base32Util.encoder(aes.encrypt(Constants.FIELDS[2], KeyType.TABLENAME_ENCRYPT.getValue())));

        for (String id : ids) {
            FastGeoPoint fg = phoenixMapper.selectPointById(tableName, id, cl);
            fgs.add(fg);
        }

        //解密数据
        decryptFastGeoPoint(fgs);

        //过滤数据
        filterFastGeoPoint(fgs, points);

        long end = System.currentTimeMillis();
        System.out.println("在密文上查询数据的时间：" + (end - start) * 1.0 / 1000 + "秒");
        return fgs;
    }

    public List<FastGeoPoint> selectFastGeo(String tableName, CuboidNode points) throws Exception {
        long start = System.currentTimeMillis();

        // 1. 获取起始的纬度坐标范围
        int startLat = TypeUtil.getIntFromDouble(points.getNode1().getLat());
        int endLat = TypeUtil.getIntFromDouble(points.getNode2().getLat());

        // 2 . 获取查询的列名
        List<String> re = phoenixMapper.selectColumn(tableName).stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<String> columns = new ArrayList<>();
        for (String s : re) {
            boolean idFlag = Pattern.compile("([Ii][Dd])").matcher(s).find();
            if (idFlag) {
                columns.add(s);
            }
        }
        columns.add(Constants.FASTGEO_FIELDS[0]);
        columns.add(Constants.FASTGEO_FIELDS[1]);
        columns.add(Constants.FASTGEO_FIELDS[2]);

        //获取待查询区域的经度和时间
        int[] mLon = fastGeoOperator.getVectorQueryLon(points);
        int[] mTime = fastGeoOperator.getVectorQueryTime(points);

        List<String> ids = new ArrayList<>();
        for (int i = startLat; i <= endLat; i++) {
            String key = String.valueOf(i);
            List<MFastGeo> value = fastGeoOperator.mgetDataByKey(key);
            if (value == null || value.size() == 0) {
                List<FastGeoDO> res = phoenixMapper.selectFastGeoSSWByAESLat(tableName, key, columns);
                value = fastGeoOperator.getMFromDo(res);
                if (value == null || value.size() == 0) {
                    continue;
                }
                fastGeoOperator.mputDatas(key, value);
            }

            for (MFastGeo fp : value) {
                if (fastGeoOperator.vectorInner(fp.getM_lon(), mLon) == 0 && fastGeoOperator.vectorInner(fp.getM_time(), mTime) == 0) {
                    ids.add(fp.getId());
                }
            }
        }

        List<FastGeoPoint> fgs = new ArrayList<>();

        List<String> cl = new ArrayList<>();
        cl.add(columns.get(0));
        cl.add(Constants.FIELDS[0]);
        cl.add(Constants.FIELDS[1]);
        cl.add(Constants.FIELDS[2]);

        for (String id : ids) {
            FastGeoPoint fg = phoenixMapper.selectPointById(tableName, id, cl);
            fgs.add(fg);
        }

        //过滤数据
        filterFastGeoPoint(fgs, points);

        long end = System.currentTimeMillis();
        System.out.println("在明文上查询数据的时间：" + (end - start) * 1.0 / 1000 + "秒");
        return fgs;
    }

    /**
     * 解密加密的点数据
     *
     * @return 明文数据
     */
    private List<FastGeoPoint> decryptFastGeoPoint(List<FastGeoPoint> fg) throws Exception {
        for (FastGeoPoint r : fg) {
            r.setId(aes.decrypt(base32Util.decoder(r.getId()), KeyType.TABLEDATA_ENCRYPT.getValue()));
            r.setLat(aes.decrypt(base32Util.decoder(r.getLat()), KeyType.TABLEDATA_ENCRYPT.getValue()));
            r.setLon(aes.decrypt(base32Util.decoder(r.getLon()), KeyType.TABLEDATA_ENCRYPT.getValue()));
            r.setTime(aes.decrypt(base32Util.decoder(r.getTime()), KeyType.TABLEDATA_ENCRYPT.getValue()));
        }
        return fg;
    }

    private List<FastGeoPoint> filterFastGeoPoint(List<FastGeoPoint> fg, CuboidNode points) throws ParseException {
        List<FastGeoPoint> result = new ArrayList<>();
        for (FastGeoPoint r : fg) {
            if (isContain(r, points)) {
                result.add(r);
            }
        }
        return result;
    }

    private boolean isContain(FastGeoPoint fg, CuboidNode points) throws ParseException {
        double lat = Double.parseDouble(fg.getLat());
        double lon = Double.parseDouble(fg.getLon());
        long time = DateUtil.parseDate(fg.getTime()).getTime();

        double startLat = points.getNode1().getLat();
        double endLat = points.getNode2().getLat();
        double startLon = points.getNode1().getLon();
        double endLon = points.getNode2().getLon();
        long startTime = DateUtil.parseDate(points.getNode1().getTime()).getTime();
        long endTime = DateUtil.parseDate(points.getNode2().getTime()).getTime();

        return lat >= startLat && lat <= endLat && lon >= startLon && lon <= endLon && time >= startTime && time <= endTime;
    }
}