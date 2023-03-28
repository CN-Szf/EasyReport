package com.sdyx.report.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdyx.report.consts.ColumnType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author SongZiFeng
 * @date 2023/3/23 21:37
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "MetaColumn")
@Table(name = "meta_column")
public class MetaColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint unsigned")
    private Long id;

    /** 属性名称*/
    @Column(nullable = false, length = 20)
    private String name;

    /** 属性注释*/
    @Column(nullable = false, length = 20)
    private String comment;

    /** 数据类型*/
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ColumnType type;

    /** 排序*/
    @Column(nullable = false)
    private Integer sort;

    @JsonIgnore
    @ManyToOne
    private MetaTable table;

}
