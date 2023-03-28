package com.sdyx.report.mapper.repository;

import com.sdyx.report.domain.MetaTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author SongZiFeng
 * @date 2023/3/23 21:42
 */
public interface MetaTableRepository extends JpaRepository<MetaTable, Long> {

    /**
     * 根据注释查找
     *
     * @param comment 表注释
     * @return {@link Optional}<{@link MetaTable}>
     */
    Optional<MetaTable> findByComment(String comment);
}
