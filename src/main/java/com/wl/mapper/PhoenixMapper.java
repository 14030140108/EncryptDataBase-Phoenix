package com.wl.mapper;

import com.wl.beans.fastgeo.FastGeoPoint;
import com.wl.beans.fastgeo.FastGeoDO;
import com.wl.beans.STCodePoint;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PhoenixMapper {

    @SelectProvider(type = com.wl.mapper.MapperProvider.class, method = "select")
    List<STCodePoint> select(@Param("tableName") String tableName, String column, String minSTCode, String maxSTCode);

    @SelectProvider(type = com.wl.mapper.MapperProvider.class, method = "selectFastGeoSSWByAESLat")
    List<FastGeoDO> selectFastGeoSSWByAESLat(String tableName, String aesLat, List<String> columns);

    @SelectProvider(type = com.wl.mapper.MapperProvider.class, method = "selectPointById")
    FastGeoPoint selectPointById(String tableName, String id, List<String> columns);

    @SelectProvider(type = com.wl.mapper.MapperProvider.class, method = "selectColumn")
    List<String> selectColumn(String tableName);

    // @Update("CREATE TABLE IF NOT EXISTS ${tableName} (id BIGINT NOT NULL PRIMARY KEY,name CHAR(20),age INTEGER)")
    @UpdateProvider(type = com.wl.mapper.MapperProvider.class, method = "createTable")
    void createTable(@Param("tableName") String tableName, List<String> fields);

    @UpdateProvider(type = com.wl.mapper.MapperProvider.class, method = "createIndex")
    void createIndex(@Param("tableName") String tableName, List<String> fields);

    //    @Update("UPSERT INTO \"${tableName}\" VALUES(${user.id},'${user.name}',${user.age})")
    @UpdateProvider(type = com.wl.mapper.MapperProvider.class, method = "insert")
    void insert(@Param("tableName") String tableName, List<String> data);

}
