package com.sdyx.report.controller;

import com.sdyx.report.domain.MetaTable;
import com.sdyx.report.service.MetaTableService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author SongZiFeng
 * @date 2023/3/23
 */
@Slf4j
@RequestMapping("/table/meta")
@RestController
public class MetaTableController {

    @Autowired
    private MetaTableService metaTableService;

    @GetMapping("/all")
    public List<MetaTable> getMetaTableList() {
        return metaTableService.getMetaTableList();
    }

    @GetMapping("/{id}")
    public MetaTable getMetaTable(@PathVariable Long id) {
        return metaTableService.getMetaTable(id);
    }

    @PostMapping
    public void saveMetaTable(@RequestBody MetaTable metaTable) {
        metaTableService.saveMetaTable(metaTable);
    }

    @DeleteMapping("/{id}")
    public void dropTable(@PathVariable Long id) {
        metaTableService.dropTable(id);
    }

    @PostMapping("/excel")
    public void importExcel(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (!(StringUtils.endsWithAny(filename, ".xlsx", ".xls"))) {
            throw new RuntimeException("请上传Excel类型的文件");
        }

        try (InputStream inputStream = file.getInputStream()){
            metaTableService.importExcel(inputStream);
        } catch (IOException e) {
            log.error("读取Excel文件时异常", e);
            throw new RuntimeException("读取文件时异常");
        }
    }

}
