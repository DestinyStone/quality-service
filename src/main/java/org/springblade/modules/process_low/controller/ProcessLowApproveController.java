package org.springblade.modules.process_low.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springblade.common.constant.RootMappingConstant;
import org.springblade.common.enums.ApproveStatusEnum;
import org.springblade.common.utils.CommonUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.out_buy_low.bean.dto.OutBuyQprDTO;
import org.springblade.modules.out_buy_low.bean.entity.OutBuyQpr;
import org.springblade.modules.out_buy_low.service.OutBuyQprService;
import org.springblade.modules.process.entity.bean.BpmProcess;
import org.springblade.modules.process.entity.dto.RejectDTO;
import org.springblade.modules.process.enums.ApproveNodeStatusEnum;
import org.springblade.modules.process.service.BpmProcessService;
import org.springblade.modules.process_low.bean.entity.ProcessLow;
import org.springblade.modules.process_low.bean.vo.ProcessLowApproveQualityVO;
import org.springblade.modules.process_low.bean.vo.ProcessLowApproveVO;
import org.springblade.modules.process_low.enums.LowBpmNodeEnum;
import org.springblade.modules.process_low.service.ProcessLowApproveService;
import org.springblade.modules.process_low.service.ProcessLowService;
import org.springblade.modules.process_low.utils.ProcessLowEmailUtils;
import org.springblade.modules.work.enums.SettleBusType;
import org.springblade.modules.work.service.SettleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

/**
 * @Author: DestinyStone
 * @Date: 2022/1/30 22:25
 * @Description: ???????????????????????????
 */
@RequestMapping(RootMappingConstant.root + "/process/low/approve")
@RestController
@Api(value = "?????????????????????", tags = "?????????????????????")
public class ProcessLowApproveController {

	@Autowired
	private ProcessLowApproveService processLowApproveService;

	@Autowired
	private BpmProcessService processService;

	@Autowired
	private ProcessLowService lowService;

	@Autowired
	private OutBuyQprService qprService;

	@Autowired
	private SettleLogService settleLogService;

	@PostMapping("/handler/qpr/reject")
	@ApiOperation("??????qpr????????????")
	@Transactional
	public R handlerQprReject(@RequestBody @Valid RejectDTO rejectDTO) {
		lowService.handlerQprReject(rejectDTO);
		return R.data(true);
	}

	@PostMapping("/handler/qpr/save/{id}")
	@ApiOperation("??????qpr????????????")
	@Transactional
	public R handlerQprSave(@PathVariable("id") Long id,
							@RequestBody @Valid OutBuyQprDTO qprDTO) {
		qprDTO.validate();
		BpmProcess process = processService.getByBusId(id);

		OutBuyQpr qpr = BeanUtil.copy(qprDTO, OutBuyQpr.class);
		qpr.setId(id);
		qpr.setBpmStatus(ApproveStatusEnum.PROCEED.getCode());
		qpr.setBpmNode(LowBpmNodeEnum.QPR_SAVE.getCode());
		Boolean status = qprService.saveUnActiveTask(qpr);

		processService.pass(process.getBpmId());
		ProcessLow processLow = new ProcessLow();
		processLow.setId(id);
		processLow.setBpmNode(LowBpmNodeEnum.QPR_APPROVE.getCode());
		processLow.setIsHideApprove(1);
		if (processService.isProcessEnd(process.getBpmId())) {
			processLow.setBpmStatus(ApproveStatusEnum.FINISN.getCode());
			lowService.updateById(processLow);
		}else {
			processLow.setBpmStatus(ApproveStatusEnum.PROCEED.getCode());
			lowService.updateById(processLow);
		}

		return R.status(status);

	}

	@GetMapping("/pass")
	@ApiOperation("????????????")
	@Transactional
	public R passAndValidate(@RequestParam("id") Long id, @RequestParam("bpmId") Long bpmId) {
		BpmProcess process = processService.getById(bpmId);
		if (process.getEndTime() != null && System.currentTimeMillis() > process.getEndTime().getTime()) {
			throw new ServiceException("???????????????");
		}

		if (process.getBpmStatus().equals(ApproveNodeStatusEnum.SUCCESS.getCode())) {
			throw new ServiceException("???????????????????????????");
		}

		processService.pass(process.getBpmId());
		ProcessLow before = lowService.getById(id);
		ProcessLow processLow = new ProcessLow();
		processLow.setId(id);
		processLow.setBpmNode(before.getBpmNode() + 1);
		settleLogService.processLog(processLow.getTitle(), SettleBusType.LOW);
		if (processService.isProcessEnd(process.getBpmId())) {
			processLow.setBpmStatus(ApproveStatusEnum.FINISN.getCode());
			lowService.updateById(processLow);
			ProcessLowEmailUtils.sendCompleteWarningEmail(processLow);
			settleLogService.finishLog(before.getTitle(), SettleBusType.LOW, before.getCreateUser());
		}else {
			processLow.setBpmStatus(ApproveStatusEnum.PROCEED.getCode());
			lowService.updateById(processLow);
		}
		return R.status(true);
	}

	@PostMapping("/reject")
	@ApiOperation("????????????")
	public R reject(@RequestBody @Valid RejectDTO rejectDTO) {
		BpmProcess process = processService.getByBusId(rejectDTO.getBusId());
		processService.reject(process.getBpmId(), rejectDTO.getBackCause());

		ProcessLow processLow = new ProcessLow();
		processLow.setId(new Long(process.getBusId()));
		processLow.setBpmStatus(ApproveStatusEnum.BACK.getCode());
		processLow.setBpmNode(-1);
		lowService.updateById(processLow);

		ProcessLow current = lowService.getById(rejectDTO.getBusId());
		settleLogService.rejectLog(current.getTitle(), SettleBusType.LOW);
		return R.status(true);
	}

	@GetMapping("/page")
	@ApiOperation("??????")
	public R<IPage<ProcessLowApproveVO>> page(ProcessLowApproveVO processLowVO, Query query) {
		// ?????????????????????????????????
		LambdaUpdateWrapper<BpmProcess> processWrapper = new LambdaUpdateWrapper<>();
		processWrapper.le(BpmProcess::getEndTime, new Date())
			.eq(BpmProcess::getAccessDept, CommonUtil.getDeptId())
			.set(BpmProcess::getBpmPushStatus, 1);

		processService.update(processWrapper);

		if (processLowVO.getTagFlag() == null) {
			processLowVO.setTagFlag(0);
		}
		IPage<ProcessLowApproveVO> page = processLowApproveService.page(processLowVO, CommonUtil.getDeptId(), Condition.getPage(query));
		return R.data(page);
	}

	@GetMapping("/quality")
	@ApiOperation("??????")
	public R<ProcessLowApproveQualityVO> quality() {
		ProcessLowApproveQualityVO qualityVO = lowService.quality();
		return R.data(qualityVO);
	}
}
