package com.sdyx.report.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据类型
 * 不要轻易删改
 *
 * @author SongZiFeng
 * @date 2023/3/26 10:36
 */
@AllArgsConstructor
@Getter
public enum ColumnType {

    /** 字符串类型*/
    VARCHAR(String.class, "varchar(100)"),
    /** 整数类型*/
    BIGINT(Long.class, "bigint"),
    /** 浮点类型*/
    DECIMAL(BigDecimal.class, "decimal(10,2)"),
    /** 时间类型*/
    DATETIME(LocalDateTime.class, "datetime(0)");

    /** 对应的Java类型*/
    private final Class<?> clazz;

    /** 在数据库里的类型定义*/
    private final String definition;

    private static final Map<Class<?>, ColumnType> LOOKUP = new HashMap<>();

    static {
        for (ColumnType t : ColumnType.values()) {
            LOOKUP.put(t.getClazz(), t);
        }
    }

    public static ColumnType get(Class<?> clazz) {
        return LOOKUP.getOrDefault(clazz, VARCHAR);
    }

    public boolean isIncompatible(Class<?> type) {
        return this != ColumnType.VARCHAR && type != null && this.getClazz() != type;
    }
}
