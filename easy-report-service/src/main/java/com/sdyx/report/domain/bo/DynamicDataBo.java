package com.sdyx.report.domain.bo;

import lombok.Data;

/**
 * 记录Excel表头列信息
 *
 * @author SongZiFeng
 * @date 2023/3/25 13:15
 */
@Data
public class DynamicDataBo {

    /** 属性下标(列标)*/
    private Integer index;

    /** 行标*/
    private Integer rowNo;

    /** 属性名*/
    private String comment;

    /** 内容*/
    private Object content;

    /** 数据类型*/
    private Class<?> type;

    public DynamicDataBo(Integer index, String comment) {
        this.index = index;
        this.comment = comment;
    }

    public DynamicDataBo(Integer index, Integer rowNo, String comment, Object content) {
        this(index, comment);
        this.rowNo = rowNo;
        if (content != null) {
            this.content = content;
            this.type = content.getClass();
        }
    }

    public boolean isDefineType() {
        return this.type != null;
    }

    public boolean isNotBelong(Object value) {
        if (value == null || !isDefineType()) {
            return false;
        }
        return type != value.getClass();
    }

}
