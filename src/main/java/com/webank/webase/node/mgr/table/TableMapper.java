package com.webank.webase.node.mgr.table;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TableMapper {

    List<String> queryTables(@Param("dbName") String dbName, @Param("tableName") String tableName);

    int dropTable(@Param("dbName") String dbName, @Param("tableName") String tableName);

    int deleteByTableName(@Param("tableName") String tableName);

    int createTbBlock(@Param("tableName") String tableName);

    int createTransHash(@Param("tableName") String tableName);

    int createUserTransactionMonitor(@Param("tableName") String tableName);
}