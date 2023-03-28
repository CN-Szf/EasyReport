package com.sdyx.report.domain;

import com.sdyx.report.domain.MetaColumn;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author SongZiFeng
 * @date 2023/3/23 21:22
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "MetaTable")
@Table(name = "meta_table",
        uniqueConstraints = {@UniqueConstraint(name = "udx_meta_table_name", columnNames = "name"),
                @UniqueConstraint(name = "udx_meta_table_comment", columnNames = "comment")
})
@EntityListeners(AuditingEntityListener.class)
public class MetaTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint unsigned")
    private Long id;

    /** 数据库表名称*/
    @Column(nullable = false, length = 20)
    private String name;

    /** 数据库表注释*/
    @Column(nullable = false, length = 20)
    private String comment;

    /** 创建时间*/
    @CreatedDate
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /** 修改时间*/
    @LastModifiedDate
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "table_id", foreignKey = @ForeignKey(name = "fk_meta_table_id"))
    private List<MetaColumn> columnList;

    @Version
    @Column(nullable = false)
    private Integer version;
}
