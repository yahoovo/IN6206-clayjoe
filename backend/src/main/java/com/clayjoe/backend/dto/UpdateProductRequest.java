package com.clayjoe.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 255, message = "商品名称不能超过255个字符")
        String name,

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须至少为0.01")
        @Digits(integer = 8, fraction = 2, message = "价格最多8位整数、2位小数")
        BigDecimal price,

        @NotNull(message = "库存不能为空")
        @Min(value = 0, message = "库存不能小于0")
        Integer stock,

        @NotBlank(message = "分类不能为空")
        @Pattern(
                regexp = "CUP|FIGURINE|TABLEWARE",
                message = "分类必须为CUP、FIGURINE或TABLEWARE"
        )
        String category,

        @Size(max = 2000, message = "商品介绍不能超过2000个字符")
        String description,

        @Size(max = 500, message = "图片地址不能超过500个字符")
        String imageUrl,

        @Size(max = 255, message = "英文名称不能超过255个字符")
        String nameEn,

        @Size(max = 2000, message = "英文介绍不能超过2000个字符")
        String descriptionEn

) {}