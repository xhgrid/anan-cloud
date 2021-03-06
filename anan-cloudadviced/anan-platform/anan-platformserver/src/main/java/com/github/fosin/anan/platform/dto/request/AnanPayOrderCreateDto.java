package com.github.fosin.anan.platform.dto.request;

import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.github.fosin.anan.util.DateTimeUtil;

/**
 * 系统支付订单表(AnanPayOrder)创建DTO
 *
 * @author fosin
 * @date 2019-02-19 18:14:31
 * @since 1.0.0
 */
@Data
@ApiModel(value = "系统支付订单表创建DTO", description = "表(anan_pay_order)的对应的创建DTO")
public class AnanPayOrderCreateDto implements Serializable {
    private static final long serialVersionUID = -16509988727597750L;

    @NotNull(message = "订单机构" + "{javax.validation.constraints.NotNull.message}")
    @ApiModelProperty(value = "订单机构", required = true)
    private Long organizId;

    @NotNull(message = "订单用户" + "{javax.validation.constraints.NotNull.message}")
    @ApiModelProperty(value = "订单用户", required = true)
    private Long userId;

    @NotNull(message = "版本ID" + "{javax.validation.constraints.NotNull.message}")
    @ApiModelProperty(value = "版本ID", required = true)
    private Long versionId;

    @NotNull(message = "版本金额" + "{javax.validation.constraints.NotNull.message}")
    @ApiModelProperty(value = "版本金额", required = true)
    private Double money;

    @ApiModelProperty(value = "订单日期", required = true)
    @DateTimeFormat(pattern = DateTimeUtil.DATETIME_PATTERN)
    private Date orderTime;

    @NotNull(message = "订单状态：0=新建，1=支付，2=取消，3=作废" + "{javax.validation.constraints.NotNull.message}")
    @ApiModelProperty(value = "订单状态：0=新建，1=支付，2=取消，3=作废", required = true)
    private Integer status;

    @ApiModelProperty(value = "支付日期")
    @DateTimeFormat(pattern = DateTimeUtil.DATETIME_PATTERN)
    private Date payTime;

    @ApiModelProperty(value = "取消日期")
    @DateTimeFormat(pattern = DateTimeUtil.DATETIME_PATTERN)
    private Date cancleTime;

    @ApiModelProperty(value = "作废日期")
    @DateTimeFormat(pattern = DateTimeUtil.DATETIME_PATTERN)
    private Date invalidTime;

}
