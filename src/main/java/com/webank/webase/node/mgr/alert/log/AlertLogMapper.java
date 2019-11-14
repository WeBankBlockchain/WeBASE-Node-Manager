package com.webank.webase.node.mgr.alert.log;

import com.webank.webase.node.mgr.alert.log.entity.AlertLog;
import com.webank.webase.node.mgr.alert.log.entity.ReqLogListParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertLogMapper {

    List<AlertLog> listOfAlertLog(@Param("param") ReqLogListParam param);

    Integer countOfLog();

    AlertLog queryByLogId(@Param("logId") int logId);

    void add(AlertLog alertLog);

    void update(AlertLog alertLog);

    void deleteByLogId(@Param("logId") int logId);

}
