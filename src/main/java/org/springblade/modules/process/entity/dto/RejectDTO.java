package org.springblade.modules.process.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: DestinyStone
 * @Date: 2022/1/31 09:11
 * @Description:
 */
@Data
public class RejectDTO {
	@ApiModelProperty("业务主键")
	@NotNull(message = "业务主键不能为空")
	private Long busId;


	@ApiModelProperty("拒绝原因")
	@NotBlank(message = "拒绝原因不能为空")
	private String backCause;
}
