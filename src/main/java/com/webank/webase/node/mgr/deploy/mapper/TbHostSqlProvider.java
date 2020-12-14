package com.webank.webase.node.mgr.deploy.mapper;

import org.apache.ibatis.jdbc.SQL;

import com.webank.webase.node.mgr.deploy.entity.TbHost;

public class TbHostSqlProvider {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table tb_host
     *
     * @mbg.generated
     */
    public static final String ALL_COLUMN_FIELDS = "id,agency_id,agency_name,ip,ssh_user,ssh_port,root_dir,docker_port,status,remark,create_time,modify_time";

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_host
     *
     * @mbg.generated
     */
    public String insertSelective(TbHost record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("tb_host");
        
        if (record.getAgencyId() != null) {
            sql.VALUES("agency_id", "#{agencyId,jdbcType=INTEGER}");
        }
        
        if (record.getAgencyName() != null) {
            sql.VALUES("agency_name", "#{agencyName,jdbcType=VARCHAR}");
        }
        
        if (record.getIp() != null) {
            sql.VALUES("ip", "#{ip,jdbcType=VARCHAR}");
        }
        
        if (record.getSshUser() != null) {
            sql.VALUES("ssh_user", "#{sshUser,jdbcType=VARCHAR}");
        }
        
        if (record.getSshPort() != null) {
            sql.VALUES("ssh_port", "#{sshPort,jdbcType=SMALLINT}");
        }
        
        if (record.getRootDir() != null) {
            sql.VALUES("root_dir", "#{rootDir,jdbcType=VARCHAR}");
        }
        
        if (record.getDockerPort() != null) {
            sql.VALUES("docker_port", "#{dockerPort,jdbcType=SMALLINT}");
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
        
        if (record.getAgencyId() != null) {
            sql.SET("agency_id = #{agencyId,jdbcType=INTEGER}");
        }
        
        if (record.getAgencyName() != null) {
            sql.SET("agency_name = #{agencyName,jdbcType=VARCHAR}");
        }
        
        if (record.getIp() != null) {
            sql.SET("ip = #{ip,jdbcType=VARCHAR}");
        }
        
        if (record.getSshUser() != null) {
            sql.SET("ssh_user = #{sshUser,jdbcType=VARCHAR}");
        }
        
        if (record.getSshPort() != null) {
            sql.SET("ssh_port = #{sshPort,jdbcType=SMALLINT}");
        }
        
        if (record.getRootDir() != null) {
            sql.SET("root_dir = #{rootDir,jdbcType=VARCHAR}");
        }
        
        if (record.getDockerPort() != null) {
            sql.SET("docker_port = #{dockerPort,jdbcType=SMALLINT}");
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