package com.sdyx.report.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ConverterKeyBuild;
import com.alibaba.excel.converters.bigdecimal.BigDecimalNumberConverter;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeNumberConverter;
import com.alibaba.excel.converters.longconverter.LongNumberConverter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Cell;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author SongZiFeng
 * @date 2023/3/25 18:14
 */
public class StringNumberTimeConverter {

    private final GlobalConfiguration globalConfiguration;

    private final LocalDateTimeNumberConverter timeNumberConverter;

    private final LongNumberConverter longNumberConverter;

    private final BigDecimalNumberConverter decimalNumberConverter;

    public StringNumberTimeConverter(AnalysisContext analysisContext) {
        this.globalConfiguration = analysisContext.readRowHolder().getGlobalConfiguration();
        Map<ConverterKeyBuild.ConverterKey, Converter<?>> converterMap = analysisContext.readSheetHolder().getConverterMap();
        ConverterKeyBuild.ConverterKey timeConverterKey = ConverterKeyBuild.buildKey(LocalDateTime.class, CellDataTypeEnum.NUMBER);
        this.timeNumberConverter = (LocalDateTimeNumberConverter) converterMap.get(timeConverterKey);
        ConverterKeyBuild.ConverterKey longConverterKey = ConverterKeyBuild.buildKey(Long.class, CellDataTypeEnum.NUMBER);
        this.longNumberConverter = (LongNumberConverter) converterMap.get(longConverterKey);
        ConverterKeyBuild.ConverterKey decimalConverterKey = ConverterKeyBuild.buildKey(BigDecimal.class, CellDataTypeEnum.NUMBER);
        this.decimalNumberConverter = (BigDecimalNumberConverter) converterMap.get(decimalConverterKey);
    }

    public Object convertToJavaData(Cell cell) {
        if (cell == null) {
            return null;
        }

        ReadCellData<?> readCellData = (ReadCellData<?>) cell;
        CellDataTypeEnum type = readCellData.getType();
        if (type == CellDataTypeEnum.NUMBER) {
            String format = readCellData.getDataFormatData().getFormat();
            // 日期
            if (StringUtils.containsAny(format, 'y', 'm')) {
                return timeNumberConverter.convertToJavaData(readCellData, null, globalConfiguration);
            }
            BigDecimal originalNumberValue = readCellData.getOriginalNumberValue();
            // 整数
            if (originalNumberValue.scale() == 0 && StringUtils.containsNone(format, '#', '¥', '$')) {
                return longNumberConverter.convertToJavaData(readCellData, null, globalConfiguration);
            }
            // 小数
            return decimalNumberConverter.convertToJavaData(readCellData, null, globalConfiguration);
        }
        return readCellData.getStringValue();
    }
}
