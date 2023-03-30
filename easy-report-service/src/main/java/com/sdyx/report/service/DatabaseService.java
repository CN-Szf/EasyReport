package com.sdyx.report.service;

import com.sdyx.report.consts.ColumnType;
import com.sdyx.report.consts.DatabaseConst;
import com.sdyx.report.domain.MetaColumn;
import com.sdyx.report.domain.MetaTable;
import com.sdyx.report.domain.bo.DynamicDataBo;
import lombok.extern.slf4j.Slf4j;
import org.anyline.data.entity.Column;
import org.anyline.data.entity.Table;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.proxy.CacheProxy;
import org.anyline.service.AnylineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author SongZiFeng
 * @date 2023/3/23 21:45
 */
@Slf4j
@Service
@Transactional
public class DatabaseService {

    @Autowired
    private AnylineService anylineService;

    /**
     * 根据原表创建（修改）动态表
     *
     * @param metaTable 元表
     */
    public void saveDynTable(MetaTable metaTable) {
        String tableName = metaTable.getName();
        Table table = anylineService.metadata().table(tableName);
        if (table == null) {
            table = createNewTable(metaTable);
        } else {
            CacheProxy.clearColumnCache(tableName);
            modifyColumn(metaTable, table);
        }

        try {
            anylineService.ddl().save(table);
        } catch (Exception e) {
            log.error("保存表结构时异常", e);
            throw new RuntimeException("保存表结构时出现未知错误");
        }
    }

    private void modifyColumn(MetaTable metaTable, Table table) {
        // 原表持有的属性
        LinkedHashMap<String, Column> columns = table.getColumns();
        // key: 属性注释 value: 属性
        Map<String, Column> commentMap = columns.values().stream()
                .filter(c -> c.isPrimaryKey() == -1)
                .collect(Collectors.toMap(Column::getComment, Function.identity()));
        List<MetaColumn> columnList = metaTable.getColumnList().stream()
                .sorted(Comparator.comparing(MetaColumn::getSort))
                .collect(Collectors.toList());
        // 新增的属性集 key: 在哪个属性之后插入
        LinkedHashMap<String, MetaColumn> newColumnList = new LinkedHashMap<>();

        // 给原有属性排序
        String lastColumnName = table.primary().getName();
        for (MetaColumn metaColumn : columnList) {
            String comment = metaColumn.getComment();
            Column column = commentMap.get(comment);
            if (column == null) {
                newColumnList.put(lastColumnName, metaColumn);
                continue;
            }
            column.setType(metaColumn.getType().getDefinition());
            column.setAfter(lastColumnName);
            lastColumnName = column.getName();

            commentMap.remove(comment);
        }

        // 移除多余的属性
        table.setAutoDropColumn(true);
        for (Column column : commentMap.values()) {
            String columnName = column.getName().toUpperCase();
            columns.remove(columnName);
        }

        // 添加新增的属性
        for (Map.Entry<String, MetaColumn> entry : newColumnList.entrySet()) {
            String afterColumnName = entry.getKey();
            MetaColumn metaColumn = entry.getValue();
            String columnName = metaColumn.getName();
            String columnComment = metaColumn.getComment();
            String columnTypeDefinition = metaColumn.getType().getDefinition();
            table.addColumn(columnName, columnTypeDefinition)
                    .setComment(columnComment)
                    .setAfter(" " + afterColumnName);
        }
    }

    private Table createNewTable(MetaTable metaTable) {
        String tableName = metaTable.getName();
        String tableComment = metaTable.getComment();
        List<MetaColumn> columnList = metaTable.getColumnList();
        Table table = new Table(tableName);
        table.setComment(tableComment);
        table.addColumn(DatabaseConst.DEFAULT_PRIMARY_KEY, DatabaseConst.DEFAULT_PRIMARY_KEY_TYPE)
                .setPrimaryKey(true)
                .setAutoIncrement(true);
        for (MetaColumn metaColumn : columnList) {
            String columnName = metaColumn.getName();
            String columnComment = metaColumn.getComment();
            String columnTypeDefinition = metaColumn.getType().getDefinition();
            table.addColumn(columnName, columnTypeDefinition).setComment(columnComment);
        }
        return table;
    }

    /**
     * 删除动态表
     *
     * @param metaTable 元表
     */
    public void dropDynTable(MetaTable metaTable) {
        String tableName = metaTable.getName();
        Table table = new Table(tableName);
        try {
            anylineService.ddl().drop(table);
        } catch (Exception e) {
            log.error("删除表时异常", e);
            throw new RuntimeException("删除表时出现未知错误");
        }
    }

    /**
     * 保存数据
     *
     * @param metaTable 元表
     * @param dataList  数据列表
     */
    public void saveData(MetaTable metaTable, List<List<DynamicDataBo>> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        Map<String, MetaColumn> commentMap = metaTable.getColumnList().stream()
                .collect(Collectors.toMap(MetaColumn::getComment, Function.identity()));
        List<Map<String, Object>> collect = dataList.stream()
                .map(row -> {
                    Map<String, Object> rowMap = new HashMap<>(row.size());
                    for (DynamicDataBo cell : row) {
                        String comment = cell.getComment();
                        Object data = cell.getContent();
                        MetaColumn metaColumn = commentMap.get(comment);
                        ColumnType columnType = metaColumn.getType();
                        if (columnType.isIncompatible(cell.getType())) {
                            throw new RuntimeException(String.format("数据格式与定义不符：%s(位于第%d行，%s列), 请检查", data.toString(), cell.getRowNo(), comment));
                        }
                        String columnName = metaColumn.getName();
                        rowMap.put(columnName, data);
                    }
                    return rowMap;
                })
                .collect(Collectors.toList());
        DataSet dataSet = new DataSet(collect);
        anylineService.insert(metaTable.getName(), dataSet);
    }

    public Map<String, Object> selectData(String tableName, Long id) {
        DataRow dataRow = new DataRow();
        dataRow.set(DatabaseConst.DEFAULT_PRIMARY_KEY, id);
        checkExists(tableName, dataRow);
        return anylineService.query(tableName, dataRow);
    }

    public void saveData(String tableName, Map<String, Object> data) {
        DataRow dataRow = new DataRow(data);
        anylineService.save(tableName, dataRow);
    }

    public void updateData(String tableName, Map<String, Object> data) {
        DataRow dataRow = new DataRow(data);
        checkExists(tableName, dataRow);
        anylineService.update(tableName, dataRow);
    }

    public void deleteData(String tableName, Long id) {
        DataRow dataRow = new DataRow();
        dataRow.set(DatabaseConst.DEFAULT_PRIMARY_KEY, id);
        checkExists(tableName, dataRow);
        anylineService.delete(tableName, dataRow);
    }

    private void checkExists(String tableName, DataRow dataRow) {
        if (!anylineService.exists(tableName, dataRow)) {
            throw new RuntimeException("数据过期，请刷新重试");
        }
    }

}
