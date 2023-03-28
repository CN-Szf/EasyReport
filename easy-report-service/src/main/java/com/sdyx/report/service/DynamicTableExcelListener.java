package com.sdyx.report.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Cell;
import com.sdyx.report.consts.ExcelConst;
import com.sdyx.report.domain.bo.DynamicDataBo;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SongZiFeng
 * @date 2023/3/23 19:54
 */
@Getter
public class DynamicTableExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    private String sheetName;
    private List<DynamicDataBo> headList;
    private List<List<DynamicDataBo>> dataList;
    private Map<Integer, DynamicDataBo> headMap;
    private StringNumberTimeConverter converter;
    private boolean isDefined = false;

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext analysisContext) {
        this.sheetName = analysisContext.readSheetHolder().getSheetName();
        Integer maxDataHeadSize = analysisContext.readSheetHolder().getMaxNotEmptyDataHeadSize();
        this.dataList = new ArrayList<>(maxDataHeadSize);
        this.converter = new StringNumberTimeConverter(analysisContext);
        this.headMap = invokeHead(headMap);
    }

    @Override
    public void invoke(Map<Integer, String> row, AnalysisContext analysisContext) {
        Integer rowNo = analysisContext.readRowHolder().getRowIndex() + ExcelConst.DEFAULT_HEAD_ROW;
        Map<Integer, Cell> cellMap = analysisContext.readRowHolder().getCellMap();
        List<DynamicDataBo> cellList = new ArrayList<>(row.size());
        for (Integer index : row.keySet()) {
            Cell cell = cellMap.get(index);
            DynamicDataBo head = headMap.get(index);
            Object content = converter.convertToJavaData(cell);
            if (head.isNotBelong(content)) {
                throw new RuntimeException(String.format("未统一格式的值：%s, 表头：%s", content, head.getComment()));
            }
            DynamicDataBo data = new DynamicDataBo(index,rowNo, head.getComment(), content);
            cellList.add(data);
        }
        dataList.add(cellList);

        if (!isDefined) {
            defineJavaType(cellList);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!StringUtils.isAlphanumericSpace(sheetName)) {
            throw new RuntimeException("sheet名称不能包含特殊符号");
        }
        if (StringUtils.equalsIgnoreCase(ExcelConst.DEFAULT_SHEET_NAME, sheetName)) {
            throw new RuntimeException("sheet名称不能以Sheet1等默认名称命名");
        }
        if (CollectionUtils.isEmpty(headMap)) {
            throw new RuntimeException("请输入内容");
        }
        this.headList = new ArrayList<>(headMap.values());
    }

    private Map<Integer, DynamicDataBo> invokeHead(Map<Integer, String> headMap) {
        Map<Integer, DynamicDataBo> headBoMap = new LinkedHashMap<>(headMap.size());
        List<String> headList = new ArrayList<>(headMap.size());
        for (int i = 0; i < headMap.size(); i++) {
            String comment = headMap.get(i);
            if (StringUtils.isBlank(comment)) {
                throw new RuntimeException(String.format("未定义的表头, 第%d列", i + 1));
            }
            if (headList.contains(comment)) {
                throw new RuntimeException(String.format("重复定义的表头：%s, 第%d列", comment, i + 1));
            }
            headList.add(comment);
            headBoMap.put(i, new DynamicDataBo(i, comment));
        }
        return headBoMap;
    }

    private void defineJavaType(List<DynamicDataBo> cellList) {
        int settledCount = 0;
        for (Map.Entry<Integer, DynamicDataBo> entry : headMap.entrySet()) {
            DynamicDataBo head = entry.getValue();
            if (head.isDefineType()) {
                settledCount++;
                continue;
            }
            Integer index = entry.getKey();
            Object data = cellList.get(index).getContent();
            if (data == null) {
                continue;
            }
            head.setType(data.getClass());
            if (++settledCount == headMap.size()) {
                isDefined = true;
                break;
            }
        }
    }

}
