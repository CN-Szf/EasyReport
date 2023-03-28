package com.sdyx.report.service;

import com.alibaba.excel.EasyExcel;
import com.sdyx.report.consts.ColumnType;
import com.sdyx.report.consts.DatabaseConst;
import com.sdyx.report.consts.ExcelConst;
import com.sdyx.report.domain.bo.DynamicDataBo;
import com.sdyx.report.domain.MetaColumn;
import com.sdyx.report.domain.MetaTable;
import com.sdyx.report.mapper.repository.MetaTableRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author SongZiFeng
 * @date 2023/3/23
 */
@Service
@Transactional
public class MetaTableService {

    @Autowired
    private MetaTableRepository metaTableRepository;

    @Autowired
    private DatabaseService databaseService;

    public List<MetaTable> getMetaTableList() {
        return metaTableRepository.findAll();
    }

    public MetaTable getMetaTable(Long id) {
        return metaTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("未找到该数据[id] = %d，其可能已被删除，请刷新", id)));
    }

    public void saveMetaTable(MetaTable metaTable) {
        MetaTable saveTable;
        Long id = metaTable.getId();
        if (null == id) {
            String tableComment = metaTable.getComment();
            metaTableRepository.findByComment(tableComment).ifPresent((t) -> {
                throw new RuntimeException(String.format("已存在同名的表：%s", tableComment));
            });
            saveTable = createMetaTable(tableComment);
            saveTable.setColumnList(metaTable.getColumnList());
        } else {
            MetaTable existedMetaTable = metaTableRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(String.format("未找到该数据[id] = %d，其可能已被删除，请刷新", id)));
            saveTable = existedMetaTable;
            BeanUtils.copyProperties(metaTable, existedMetaTable, "id", "name");
        }

        doSave(saveTable);
    }

    public void dropTable(Long id) {
        MetaTable metaTable = getMetaTable(id);
        databaseService.dropDynTable(metaTable);
        metaTableRepository.delete(metaTable);
    }

    /**
     * 导入excel
     *
     * @param inputStream 输入流
     */
    public void importExcel(InputStream inputStream) {
        // 读取Excel
        DynamicTableExcelListener excelListener = new DynamicTableExcelListener();
        EasyExcel.read(inputStream)
                .registerReadListener(excelListener)
                .ignoreEmptyRow(true)
                .headRowNumber(ExcelConst.DEFAULT_HEAD_ROW)
                .sheet(ExcelConst.DEFAULT_SHEET)
                .doRead();

        // 处理表结构
        String sheetName = excelListener.getSheetName();
        List<DynamicDataBo> headList = excelListener.getHeadList();
        MetaTable metaTable = proceedTableStructure(sheetName, headList);

        // 处理数据
        List<List<DynamicDataBo>> dataList = excelListener.getDataList();
        databaseService.saveData(metaTable, dataList);
    }

    /**
     * 进行表结构调整
     *
     * @param sheetName 表名字
     * @param headList  属性集
     */
    private MetaTable proceedTableStructure(String sheetName, List<DynamicDataBo> headList) {
        MetaTable metaTable = metaTableRepository.findByComment(sheetName)
                .orElseGet(() -> createMetaTable(sheetName));
        updateMetaColumns(metaTable, headList);
        doSave(metaTable);
        return metaTable;
    }

    private void doSave(MetaTable metaTable) {
        Integer preVersion = metaTable.getVersion();
        metaTableRepository.saveAndFlush(metaTable);
        Integer curVersion = metaTable.getVersion();
        // 更新表结构
        if (preVersion == null || !preVersion.equals(curVersion)) {
            databaseService.saveDynTable(metaTable);
        }
    }

    /**
     * 创建元表
     *
     * @param comment 表名字
     * @return {@link MetaTable}
     */
    private MetaTable createMetaTable(String comment) {
        long tableNo = metaTableRepository.count() + 1;
        String tableName = DatabaseConst.TABLE_PREFIX + tableNo;
        MetaTable metaTable = new MetaTable();
        metaTable.setName(tableName);
        metaTable.setComment(comment);
        return metaTable;
    }

    /**
     * 创列列表
     * @param metaTable 元表
     * @param headList  属性集
     */
    private void updateMetaColumns(MetaTable metaTable, List<DynamicDataBo> headList) {
        List<MetaColumn> columnList = metaTable.getColumnList();
        if (columnList == null) {
            columnList = new ArrayList<>(headList.size());
            metaTable.setColumnList(columnList);
        }
        // 属性注释集合
        Map<String, MetaColumn> commentMap = columnList.stream()
                .collect(Collectors.toMap(MetaColumn::getComment, Function.identity()));
        int columnCount = columnList.size();
        // 新属性序号
        int newColumnNo = 0;
        for (DynamicDataBo head : headList) {
            ColumnType columnType = ColumnType.get(head.getType());
            String columnComment = head.getComment();
            MetaColumn metaColumn = commentMap.get(columnComment);
            // 新属性
            if (metaColumn == null) {
                newColumnNo++;
                int newColumnIndex = columnCount + newColumnNo;
                String columnName = DatabaseConst.COLUMN_PREFIX + newColumnIndex;
                metaColumn = new MetaColumn();
                metaColumn.setName(columnName);
                metaColumn.setComment(columnComment);
                metaColumn.setType(columnType);
                metaColumn.setTable(metaTable);
                columnList.add(metaColumn);
            }
            int columnSort = head.getIndex() + 1;
            metaColumn.setSort(columnSort);

            commentMap.remove(columnComment);
        }
        // 移除多余的属性
        columnList.removeAll(commentMap.values());
    }

}
