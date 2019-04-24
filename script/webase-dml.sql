
-- ----------------------------
-- 1、init tb_account_info data   admin/Abcd1234
-- ----------------------------
INSERT INTO tb_account_info (account,account_pwd,role_id,create_time,modify_time)values('admin', '$2a$10$F/aEB1iEx/FvVh0fMn6L/uyy.PkpTy8Kd9EdbqLGo7Bw7eCivpq.m',100000,now(),now());



-- ----------------------------
-- 2、init tb_role data
-- ----------------------------
INSERT INTO `tb_role` (role_name,role_name_zh,create_time,modify_time)VALUES ('admin', '管理员', now(), now());
INSERT INTO `tb_role` (role_name,role_name_zh,create_time,modify_time)VALUES ('visitor', '访客', now(), now());


-- ----------------------------
-- 3、init tb_user data
-- ----------------------------
INSERT INTO tb_user(user_name, public_key, user_status, user_type, address, has_pk, create_time, modify_time) VALUES ( 'systemUser', '0xc5d877bff9923af55f248fb48b8907dc7d00cac3ba19b4259aebefe325510af7bd0a75e9a8e8234aa7aa58bc70510ee4bef02201a86006196da4e771c47b71b4', 1, 2, '0xf1585b8d0e08a0a00fff662e24d67ba95a438256', 1, Now(), Now());



-- ----------------------------
-- 4、init tb_user_key_mapping data
-- ----------------------------
INSERT INTO tb_user_key_mapping(user_id, private_key, map_status, create_time, modify_time) VALUES (700001, 'SzK9KCjpyVCW0T9K9r/MSlmcpkeckYKVn/D1X7fzzp18MM7yHhUHQugTxKXVJJY5XWOb4zZ79IXMBu77zmXsr0mCRnATZTUqFfWLX6tUBIw=', 1, now(), now());
