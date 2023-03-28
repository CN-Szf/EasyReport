package com.sdyx.report.service;

import com.sdyx.report.domain.MetaTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author SongZiFeng
 * @date 2023/3/23
 */
@Service
@Transactional
public class DynamicTableService {

    @Autowired
    private MetaTableService metaTableService;

    @Autowired
    private DatabaseService databaseService;

    public Map<String, Object> getData(Long tableId, Long id) {
        MetaTable metaTable = metaTableService.getMetaTable(tableId);
        return databaseService.selectData(metaTable.getName(), id);
    }

    public void saveData(Long tableId, Map<String, Object> data) {
        MetaTable metaTable = metaTableService.getMetaTable(tableId);
        databaseService.saveData(metaTable.getName(), data);
    }

    public void updateData(Long tableId, Map<String, Object> data) {
        MetaTable metaTable = metaTableService.getMetaTable(tableId);
        databaseService.updateData(metaTable.getName(), data);
    }

    public void deleteData(Long tableId, Long id) {
        MetaTable metaTable = metaTableService.getMetaTable(tableId);
        databaseService.deleteData(metaTable.getName(), id);
    }
}
