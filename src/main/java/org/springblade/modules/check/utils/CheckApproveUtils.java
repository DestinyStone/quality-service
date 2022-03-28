package org.springblade.modules.check.utils;

import org.springblade.common.cache.ParamCache;
import org.springblade.common.constant.ParamConstant;
import org.springblade.common.utils.ApproveUtils;
import org.springblade.common.utils.CommonUtil;
import org.springblade.modules.process.core.ProcessContainer;
import org.springblade.modules.process.enums.ApproveNodeStatusEnum;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: xiaoxia
 * @Date: 2022/2/23 18:16
 * @Description:
 */
public class CheckApproveUtils {
	public static List<ProcessContainer> getProcessTaskList(String busId) {
		// 第一个节点 用户提交
		ProcessContainer userCommit =new ProcessContainer(
			busId,
			"-1",
			"-1",
			"用户已提交",
			0,
			"commit",
			ApproveUtils.ServerFlagEnum.CHECK_APPROVE.getMessage(),
			ApproveNodeStatusEnum.SUCCESS.getCode());


		// 第二个节点 系长 审批
		ProcessContainer downLoadResourceApprove = new ProcessContainer(
			busId,
			CommonUtil.getDeptId() + "",
			ParamCache.getValue(ParamConstant.CHIEF),
			"担当审批",
			1,
			"downloadResourceFile",
			ApproveUtils.ServerFlagEnum.CHECK_APPROVE.getMessage(),
			ApproveNodeStatusEnum.ACTIVE.getCode()
		);

		// 第三个节点 科长 领导审批
		ProcessContainer bossProcess = new ProcessContainer(
			busId,
			CommonUtil.getDeptId() + "",
			ParamCache.getValue(ParamConstant.DEPT),
			"科长审批",
			2,
			"downloadResourceFile",
			ApproveUtils.ServerFlagEnum.CHECK_APPROVE.getMessage(),
			ApproveNodeStatusEnum.UN_ACTIVE.getCode()
		);

		return Arrays.asList(userCommit, downLoadResourceApprove, bossProcess);
	}
}
