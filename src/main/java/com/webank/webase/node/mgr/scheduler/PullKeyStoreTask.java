///**
// * Copyright 2014-2019 the original author or authors.
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.webank.webase.node.mgr.scheduler;
//
//import com.webank.webase.node.mgr.base.enums.HasPk;
//import com.webank.webase.node.mgr.base.enums.UserType;
//import com.webank.webase.node.mgr.front.FrontService;
//import com.webank.webase.node.mgr.front.entity.FrontParam;
//import com.webank.webase.node.mgr.front.entity.TbFront;
//import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
//import com.webank.webase.node.mgr.group.GroupService;
//import com.webank.webase.node.mgr.group.entity.TbGroup;
//import com.webank.webase.node.mgr.user.UserMapper;
//import com.webank.webase.node.mgr.user.UserService;
//import com.webank.webase.node.mgr.user.entity.KeyPair;
//import com.webank.webase.node.mgr.user.entity.TbUser;
//import com.webank.webase.node.mgr.user.entity.UserParam;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.*;
//
///**
// * pull key store from different front and refresh local users
// * @author marsli
// */
//@Log4j2
//@Component
//public class PullKeyStoreTask {
//
//	@Autowired
//	private UserService userService;
//	@Autowired
//	private UserMapper userMapper;
//	@Autowired
//	private FrontInterfaceService frontInterfaceService;
//	@Autowired
//	private FrontService frontService;
//	@Autowired
//	private GroupService groupService;
//
//	@Scheduled(fixedDelayString = "${constant.pullKeyStoreTaskFixedDelay}")
//	public void taskStart() {
//		refreshLocalUser();
//	}
//
//	public synchronized void refreshLocalUser() {
//		Instant startTime = Instant.now();
//		log.info("start refreshLocalUser startTime:{}", startTime.toEpochMilli());
//		List<TbFront> frontList = frontService.getFrontList(new FrontParam());
//		if (frontList == null || frontList.size() == 0) {
//			log.warn("refreshLocalUser jump over: not found any front");
//			return;
//		}
//		List<Integer> groupIdList = getGroupIdList();
//
//		// <groupId, list of KeyPair>
//		Map<Integer, List<KeyPair>> pullAddressMap = pullAddressListFromFront(frontList, groupIdList);
//		for(Integer groupId : groupIdList) {
//			// // <groupId, list of address>
//			Map<Integer, List<String>> localAddressMap = getLocalUserAddressList();
//			// group's list of address
//			List<String> localAddressList = localAddressMap.get(groupId);
//			// all keypair from front
//			final List<KeyPair> pullAddressList = pullAddressMap.get(groupId);
//			if (localAddressList.isEmpty()) {
//				continue;
//			}
//
//			// delete user in db that in localList but not in pullList
//			localAddressList.forEach(address -> {
//				// judge contain
//				long equalCount = 0;
//				equalCount = pullAddressList.stream().filter(keyPair ->
//						address.equals(keyPair.getAddress())).count();
//				// if pull list not have local user's address, remove it
//				log.debug("refreshLocalUser localAddressList groupId:{}, address:{}, equalCount:{}",
//						groupId, address, equalCount);
//				if (equalCount == 0 ) {
//					userService.deleteByAddress(address);
//				}
//			});
//			// add user in db that in pullList but not in localList
//			pullAddressList.forEach(keyPair -> {
//				// if local list don't have key pair, add it in db
//				log.debug("refreshLocalUser pullAddressList groupId:{}, address:{}",
//						groupId, keyPair.getAddress());
//				if (!localAddressList.contains(keyPair.getAddress())) {
//					addUserInfo(groupId, keyPair);
//				}
//			});
//		}
//		log.info("end refreshLocalUser useTime:{} ",
//				Duration.between(startTime, Instant.now()).toMillis());
//	}
//
//	/**
//	 * get local user list of address
//	 * @return list sorted by groupId
//	 */
//	private Map<Integer, List<String>> getLocalUserAddressList() {
//		log.debug("start getLocalUserAddressList.");
//		Map<Integer, List<String>> localGroupIdAddressMap = new HashMap<>();
//		// get group id list
//		List<Integer> groupIdList = getGroupIdList();
//		for (Integer groupId: groupIdList) {
//			// init address list of groupId in map
//			localGroupIdAddressMap.put(groupId, new ArrayList<>());
//			// get user address list by group id
//			List<String> addressList = new ArrayList<>();
//			UserParam userParam = new UserParam();
//			userParam.setGroupId(groupId);
//			List<TbUser> localUserList = userService.qureyUserList(userParam);
//			localUserList.forEach(user -> addressList.add(user.getAddress()));
//			// set map's address list
//			localGroupIdAddressMap.get(groupId).addAll(addressList);
//		}
//		log.debug("end getLocalUserAddressList. localGroupIdAddressMap:{}", localGroupIdAddressMap);
//		return localGroupIdAddressMap;
//	}
//
//	/**
//	 * pull key store list from all front
//	 * @param frontList
//	 * @param groupIdList
//	 * @return list sorted by groupId
//	 */
//	private Map<Integer, List<KeyPair>> pullAddressListFromFront(List<TbFront> frontList, List<Integer> groupIdList) {
//		log.debug("start pullAddressListFromFront.");
//		Map<Integer, List<KeyPair>> groupKeyPairListMap = new HashMap<>();
//		// init map with empty list by groupId
//		groupIdList.forEach(groupId -> groupKeyPairListMap.put(groupId, new ArrayList<>()));
//		// set map
//		for (TbFront front : frontList) {
//			String frontIp = front.getFrontIp();
//			int frontPort = front.getFrontPort();
//			// group id useless in getting keyStore list
//			List<KeyPair> keyPairList = frontInterfaceService.getKeyStoreList(1, frontIp, frontPort);
//			keyPairList.forEach(keyPair -> {
//				String userName = keyPair.getUserName() + "_" + front.getFrontId();
//				keyPair.setUserName(userName);
//			});
//			// fill each group with the same key pair list
//			groupIdList.forEach(groupId ->
//					groupKeyPairListMap
//							.get(groupId)
//							.addAll(keyPairList));
//		}
//		log.debug("end getLocalUserAddressList. groupKeyPairListMap:{}", groupKeyPairListMap);
//		return groupKeyPairListMap;
//	}
//
//	/**
//	 * getGroupIdList
//	 * @return list of group id int
//	 */
//	private List<Integer> getGroupIdList() {
//		// get all group id
//		List<TbGroup> allGroup = groupService.getGroupList(null);
//		List<Integer> resList = new ArrayList<>();
//		allGroup.forEach(tbGroup -> resList.add(tbGroup.getGroupId()));
//		return resList;
//	}
//
//	/**
//	 *  add user by group id
//	 * @param keyPair
//	 */
//	private void addUserInfo(int groupId, KeyPair keyPair) {
//		String address = keyPair.getAddress();
//		String publicKey = keyPair.getPublicKey();
//		String[] nameAndDesc = keyPair.getUserName().split("_");
//		String userName = nameAndDesc[0] + "_" + address.substring(0, 5);
//		String descPrefix = "from front: ";
//		String description = descPrefix + nameAndDesc[1];
//		// add row in all group
//		TbUser newUserRow = new TbUser(HasPk.HAS.getValue(), UserType.GENERALUSER.getValue(),
//				userName,
//				groupId, address, publicKey,
//				description);
//		Integer affectRow = userMapper.addUserRow(newUserRow);
//		if (affectRow == 0) {
//			log.warn("pull user save: affect 0 rows of tb_user");
//		}
//
//	}
//
//}
