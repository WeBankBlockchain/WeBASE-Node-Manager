package com.webank.webase.node.mgr.external.mapper;

import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.external.entity.RspAllExtContract;
import com.webank.webase.node.mgr.external.entity.TbExternalContract;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

public interface TbExternalContractMapper {


    @Select({"select id,group_id,contract_address,deploy_address,deploy_tx_hash,deploy_time,contract_status,contract_type,contract_name,contract_version,create_time,modify_time,contract_bin,contract_abi,bytecode_bin,description from tb_external_contract where group_id = #{groupId} and contract_address = #{contractAddress}"})
    TbExternalContract getByGroupIdAndAddress(@Param("groupId") int groupId, @Param("contractAddress") String contractAddress);

    @SelectProvider(value = TbExternalContractSqlProvider.class, method = "listJoin")
    List<RspAllExtContract> listContractJoinTbAbi(ContractParam param);

    @Delete({ "delete from tb_external_contract where group_id = #{groupId,jdbcType=INTEGER}" })
    int deleteByGroupId(Integer groupId);

    @SelectProvider(type = TbExternalContractSqlProvider.class, method = "getList")
    List<TbExternalContract> listExtContract(ContractParam param);

    @SelectProvider(type = TbExternalContractSqlProvider.class, method = "count")
    int countExtContract(ContractParam param);

    @Select({ "select count(1) from tb_external_contract where group_id = #{groupId} and contract_address = #{address}" })
    int countOfExtContract(@Param("groupId") Integer groupId, @Param("address") String address);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_external_contract
     *
     * @mbg.generated
     */
    @Delete({ "delete from tb_external_contract where id = #{id,jdbcType=INTEGER}" })
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_external_contract
     *
     * @mbg.generated
     */
    @InsertProvider(type = TbExternalContractSqlProvider.class, method = "insertSelective")
    @SelectKey(statement = "SELECT currval(id)", keyProperty = "id", before = false, resultType = Integer.class)
    int insertSelective(TbExternalContract record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_external_contract
     *
     * @mbg.generated
     */
    @Select({ "select id, group_id, contract_address, deploy_address, deploy_tx_hash, deploy_time, contract_status, contract_type, contract_name, contract_version, create_time, modify_time, contract_bin, contract_abi, bytecode_bin, description from tb_external_contract where id = #{id,jdbcType=INTEGER}" })
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.INTEGER), @Result(column = "contract_address", property = "contractAddress", jdbcType = JdbcType.VARCHAR), @Result(column = "deploy_address", property = "deployAddress", jdbcType = JdbcType.VARCHAR), @Result(column = "deploy_tx_hash", property = "deployTxHash", jdbcType = JdbcType.VARCHAR), @Result(column = "deploy_time", property = "deployTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "contract_status", property = "contractStatus", jdbcType = JdbcType.INTEGER), @Result(column = "contract_type", property = "contractType", jdbcType = JdbcType.TINYINT), @Result(column = "contract_name", property = "contractName", jdbcType = JdbcType.VARCHAR), @Result(column = "contract_version", property = "contractVersion", jdbcType = JdbcType.VARCHAR), @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "modify_time", property = "modifyTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "contract_bin", property = "contractBin", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "contract_abi", property = "contractAbi", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "bytecode_bin", property = "bytecodeBin", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "description", property = "description", jdbcType = JdbcType.LONGVARCHAR) })
    TbExternalContract selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_external_contract
     *
     * @mbg.generated
     */
    @UpdateProvider(type = TbExternalContractSqlProvider.class, method = "updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(TbExternalContract record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_external_contract
     *
     * @mbg.generated
     */
    //TODO 无调用 psql不支持<script> 直接注释
    //@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    //@Insert({ "<script>", "insert into tb_external_contract (group_id, ", "contract_address, deploy_address, ", "deploy_tx_hash, deploy_time, ", "contract_status, contract_type, ", "contract_name, contract_version, ", "create_time, modify_time, ", "contract_bin, contract_abi, ", "bytecode_bin, description)", "values<foreach collection=\"list\" item=\"detail\" index=\"index\" separator=\",\">(#{detail.groupId,jdbcType=INTEGER}, ", "#{detail.contractAddress,jdbcType=VARCHAR}, #{detail.deployAddress,jdbcType=VARCHAR}, ", "#{detail.deployTxHash,jdbcType=VARCHAR}, #{detail.deployTime,jdbcType=TIMESTAMP}, ", "#{detail.contractStatus,jdbcType=INTEGER}, #{detail.contractType,jdbcType=SMALLINT}, ", "#{detail.contractName,jdbcType=VARCHAR}, #{detail.contractVersion,jdbcType=VARCHAR}, ", "#{detail.createTime,jdbcType=TIMESTAMP}, #{detail.modifyTime,jdbcType=TIMESTAMP}, ", "#{detail.contractBin,jdbcType=LONGVARCHAT}, #{detail.contractAbi,jdbcType=LONGVARCHAT}, ", "#{detail.bytecodeBin,jdbcType=LONGVARCHAT}, #{detail.description,jdbcType=LONGVARCHAT})</foreach></script>" })
    //int batchInsert(java.util.List<TbExternalContract> list);
}
