package com.sdyx.report.controller;

import com.sdyx.report.service.DynamicTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author SongZiFeng
 * @date 2023/3/23
 */
@Slf4j
@RequestMapping("/table/dynamic/{tableId}")
@RestController
public class DynamicTableController {

    @Autowired
    private DynamicTableService dynamicTableService;

    @GetMapping("/{id}")
    public Map<String, Object> getData(@PathVariable Long tableId, @PathVariable Long id) {
        return dynamicTableService.getData(tableId, id);
    }

    @PostMapping
    public void saveData(@PathVariable Long tableId, @RequestBody Map<String, Object> data) {
        dynamicTableService.saveData(tableId, data);
    }

    @PutMapping
    public void updateData(@PathVariable Long tableId, @RequestBody Map<String, Object> data) {
        dynamicTableService.updateData(tableId, data);
    }

    @DeleteMapping("/{id}")
    public void deleteData(@PathVariable Long tableId, @PathVariable Long id) {
        dynamicTableService.deleteData(tableId, id);
    }

}
