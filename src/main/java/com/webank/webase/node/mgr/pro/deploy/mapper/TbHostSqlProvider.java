package com.webank.webase.node.mgr.pro.deploy.mapper;

import com.webank.webase.node.mgr.pro.deploy.entity.TbHost;
import org.apache.ibatis.jdbc.SQL;

public class TbHostSqlProvider {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table tb_host
     *
     * @mbg.generated
     */
    public static final String ALL_COLUMN_FIELDS = "id,ip,root_dir,status,remark,create_time,modify_time";

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_host
     *
     * @mbg.generated
     */
    public String insertSelective(TbHost record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("tb_host");

        if (record.getIp() != null) {
            sql.VALUES("ip", "#{ip,jdbcType=VARCHAR}");
        }

        if (record.getRootDir() != null) {
            sql.VALUES("root_dir", "#{rootDir,jdbcType=VARCHAR}");
        }

        if (record.getStatus() != null) {
            sql.VALUES("status", "#{status,jdbcType=TINYINT}");
        }
        if (record.getRemark() != null) {
            sql.VALUES("remark", "#{remark,jdbcType=TINYINT}");
        }

        if (record.getCreateTime() != null) {
            sql.VALUES("create_time", "#{createTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getModifyTime() != null) {
            sql.VALUES("modify_time", "#{modifyTime,jdbcType=TIMESTAMP}");
        }
        
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_host
     *
     * @mbg.generated
     */
    public String updateByPrimaryKeySelective(TbHost record) {
        SQL sql = new SQL();
        sql.UPDATE("tb_host");

        if (record.getIp() != null) {
            sql.SET("ip = #{ip,jdbcType=VARCHAR}");
        }
        
        if (record.getRootDir() != null) {
            sql.SET("root_dir = #{rootDir,jdbcType=VARCHAR}");
        }

        if (record.getStatus() != null) {
            sql.SET("status = #{status,jdbcType=TINYINT}");
        }
        if (record.getRemark() != null) {
            sql.SET("remark = #{remark,jdbcType=TINYINT}");
        }

        if (record.getCreateTime() != null) {
            sql.SET("create_time = #{createTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getModifyTime() != null) {
            sql.SET("modify_time = #{modifyTime,jdbcType=TIMESTAMP}");
        }
        
        sql.WHERE("id = #{id,jdbcType=INTEGER}");
        
        return sql.toString();
    }
}