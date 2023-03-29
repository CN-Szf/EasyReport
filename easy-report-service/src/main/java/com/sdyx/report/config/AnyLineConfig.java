package com.sdyx.report.config;

import org.anyline.util.ConfigTable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * @author SongZiFeng
 * @date 2023/3/28
 */
@Configuration
public class AnyLineConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        configAnyLine();
    }

    private void configAnyLine() {
        ConfigTable.IS_DEBUG = false;
        ConfigTable.IS_SQL_DELIMITER_OPEN = true;
    }

}
