
-- ----------------------------
-- 1、init tb_account_info data   admin/Abcd1234
-- ----------------------------
INSERT INTO tb_account_info (account,account_pwd,role_id,create_time,modify_time)values('admin', '$2a$10$F/aEB1iEx/FvVh0fMn6L/uyy.PkpTy8Kd9EdbqLGo7Bw7eCivpq.m',100000,now(),now());



-- ----------------------------
-- 2、init tb_role data
-- ----------------------------
INSERT INTO `tb_role` (role_name,role_name_zh,create_time,modify_time)VALUES ('admin', '管理员', now(), now());
INSERT INTO `tb_role` (role_name,role_name_zh,create_time,modify_time)VALUES ('visitor', '访客', now(), now());



