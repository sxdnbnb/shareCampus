package com.sharecampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 场馆id
     */
    private Long venueId;

    /**
     * 场馆券标题
     */
    private String title;


    /**
     * 使用时间规则
     */
    private String rules;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 发放时间
     */
    // @TableField(exist = false)
    private LocalDateTime beginTime;

    /**
     * 发放结束时间
     */
    // @TableField(exist = false)
    private LocalDateTime endTime;

    /**
     * 场所类型的id
     */
    private Long typeId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
