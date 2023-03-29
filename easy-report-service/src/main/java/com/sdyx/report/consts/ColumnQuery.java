package com.sdyx.report.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.sdyx.report.consts.ColumnType.*;

/**
 * @author SongZiFeng
 * @date 2023/3/29
 */
@Getter
@AllArgsConstructor
public enum ColumnQuery {

    /** 默认*/
    DEFAULT(null, "LIKE", null),
    /** 时间类型*/
    BETWEEN(DATETIME, "BETWEEN", "datetime");

    /** 属性类型*/
    private ColumnType columnType;
    /** 查询类型*/
    private String queryType;
    /** html类型*/
    private String htmlType;

    public static ColumnQuery getQuery(ColumnType columnType) {
        for (ColumnQuery value : ColumnQuery.values()) {
            if (value.columnType == columnType) {
                return value;
            }
        }
        return DEFAULT;
    }
}
