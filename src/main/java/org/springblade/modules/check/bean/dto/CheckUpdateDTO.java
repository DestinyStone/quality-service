package org.springblade.modules.check.bean.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: xiaoxia
 * @Date: 2022/2/24 14:00
 * @Description:
 */
@Data
public class CheckUpdateDTO {
	@ApiModelProperty("种类 0新规格 1工变 2设变 3写误订正 4只变更检查法方法")
	@NotNull(message = "种类不能为空")
	private Integer type;

	@ApiModelProperty("确定内容")
	@NotBlank(message = "确定内容不能为空")
	private String confirmContent;

	@ApiModelProperty("变更内容")
	@NotBlank(message = "变更内容不能为空")
	private String changeContent;

	@ApiModelProperty("变更图片")
	@NotNull(message = "变更图片不能为空")
	private Long changeImageId;

	@ApiModelProperty("部门意见")
	@NotBlank(message = "部门意见不能为空")
	private String deptIdea;

	@ApiModelProperty("检查管理部门意见")
	@NotBlank(message = "检查管理部门意见不能为空")
	private String checkDeptIdea;

	@ApiModelProperty("供应商承认excel文件id")
	@NotNull(message = "供应商承认excel文件id不能为空")
	private Long providerExcelFileId;

	@ApiModelProperty("供应商承认excel文件名称")
	@NotBlank(message = "供应商承认excel文件名称不能为空")
	private String providerExcelFileName;

	@ApiModelProperty("供应商承认签字文件id")
	@NotNull(message = "供应商承认签字文件id不能为空")
	private Long providerSignatureId;

	@ApiModelProperty("供应商承认签字文件名称")
	@NotBlank(message = "供应商承认签字文件名称不能为空")
	private String providerSignatureName;

	@ApiModelProperty("其他附件ids")
	private String extendsFileIds;
}
