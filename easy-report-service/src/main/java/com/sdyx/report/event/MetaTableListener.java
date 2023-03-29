package com.sdyx.report.event;

import cn.hutool.core.collection.CollectionUtil;
import com.sdyx.common.constant.UserConstants;
import com.sdyx.common.core.domain.entity.SysMenu;
import com.sdyx.report.consts.DatabaseConst;
import com.sdyx.report.consts.ColumnQuery;
import com.sdyx.report.domain.MetaColumn;
import com.sdyx.report.domain.MetaTable;
import com.sdyx.report.domain.ReportDatabase;
import com.sdyx.report.domain.ReportSql;
import com.sdyx.report.domain.ReportSqlColumn;
import com.sdyx.report.domain.bo.ReportSqlBo;
import com.sdyx.report.service.IReportDatabaseService;
import com.sdyx.report.service.IReportSqlService;
import com.sdyx.system.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author SongZiFeng
 * @date 2023/3/29
 */
@Component
public class MetaTableListener {

    private static final String QUERY_SQL = "select * from %s order by id asc";
    private static final String MENU_NAME = "报表管理";

    @Autowired
    private IReportSqlService reportSqlService;

    @Autowired
    private IReportDatabaseService reportDatabaseService;

    @Autowired
    private ISysMenuService sysMenuService;

    @PostPersist
    public void postPersist(MetaTable metaTable) {
        // 创建查询SQL
        ReportSqlBo reportSqlBo = buildReportSql(metaTable);
        reportSqlService.addReportSql(reportSqlBo);
        // 创建菜单
        ReportSqlBo savedReportSqlBo = reportSqlService.selectReportSqlBySqlName(reportSqlBo.getReportSql().getSqlName());
        SysMenu sysMenu = buildSysMenu(savedReportSqlBo);
        sysMenuService.insertMenu(sysMenu);
    }

    @PostUpdate
    public void postUpdate(MetaTable metaTable) {
        String comment = metaTable.getComment();
        ReportSqlBo reportSqlBo = reportSqlService.selectReportSqlBySqlName(comment);
        if (reportSqlBo == null) {
            postPersist(metaTable);
            return;
        }

        List<ReportSqlColumn> reportSqlColumnList = buildReportSqlColumnList(metaTable);
        reportSqlBo.setReportSqlColumn(reportSqlColumnList);
        reportSqlService.updateReportSql(reportSqlBo);

        // 没有菜单则创建
        SysMenu sysMenu = sysMenuService.selectMenuByName(comment);
        if (sysMenu == null) {
            sysMenu = buildSysMenu(reportSqlBo);
            sysMenuService.insertMenu(sysMenu);
        }
    }

    @PostRemove
    public void postRemove(MetaTable metaTable) {
        ReportSqlBo reportSqlBo = reportSqlService.selectReportSqlBySqlName(metaTable.getComment());
        if (reportSqlBo == null) {
            return;
        }
        Long id = reportSqlBo.getReportSql().getId();
        reportSqlService.delById(id);

        // 删除菜单
        String menuName = reportSqlBo.getReportSql().getSqlName();
        SysMenu sysMenu = sysMenuService.selectMenuByName(menuName);
        if (sysMenu != null) {
            sysMenuService.deleteMenuById(sysMenu.getMenuId());
        }
    }

    private ReportSqlBo buildReportSql(MetaTable metaTable) {
        ReportSql reportSql = new ReportSql();
        List<ReportDatabase> reportDatabases = reportDatabaseService.selectReportDatabaseOptionsList();
        if (!CollectionUtils.isEmpty(reportDatabases)) {
            Long databaseId = CollectionUtil.getFirst(reportDatabases).getId();
            reportSql.setDatabaseId(databaseId);
        }
        reportSql.setQuerySql(String.format(QUERY_SQL, metaTable.getName()));
        reportSql.setSqlName(metaTable.getComment());

        // 创建SQL属性
        List<ReportSqlColumn> reportSqlColumnList = buildReportSqlColumnList(metaTable);

        ReportSqlBo reportSqlBo = new ReportSqlBo();
        reportSqlBo.setReportSql(reportSql);
        reportSqlBo.setReportSqlColumn(reportSqlColumnList);
        return reportSqlBo;
    }

    private List<ReportSqlColumn> buildReportSqlColumnList(MetaTable metaTable) {
        List<MetaColumn> columnList = metaTable.getColumnList();
        return columnList.stream()
                .filter(column -> !DatabaseConst.DEFAULT_PRIMARY_KEY.equals(column.getName()))
                .map(column -> {
                    String columnName = column.getName();
                    String comment = column.getComment();
                    ColumnQuery columnQuery = ColumnQuery.getQuery(column.getType());
                    ReportSqlColumn reportSqlColumn = new ReportSqlColumn();
                    reportSqlColumn.setSqlField(columnName);
                    reportSqlColumn.setColumnName(comment);
                    reportSqlColumn.setIsList("1");
                    reportSqlColumn.setIsExport("1");
                    reportSqlColumn.setIsQuery("1");
                    reportSqlColumn.setQueryType(columnQuery.getQueryType());
                    reportSqlColumn.setHtmlType(columnQuery.getHtmlType());
                    return reportSqlColumn;
                })
                .collect(Collectors.toList());
    }

    private SysMenu buildSysMenu(ReportSqlBo reportSqlBo) {
        SysMenu parentMenu = sysMenuService.selectMenuByName(MENU_NAME);
        Long parentId = (parentMenu != null) ? parentMenu.getMenuId() : 1L;
        ReportSql reportSql = reportSqlBo.getReportSql();
        SysMenu sysMenu = new SysMenu();
        sysMenu.setComponent("report/gen/generalReport");
        sysMenu.setIcon("build");
        sysMenu.setIsCache("1");
        sysMenu.setIsFrame(UserConstants.NO_FRAME);
        sysMenu.setMenuName(reportSql.getSqlName());
        sysMenu.setMenuType(UserConstants.TYPE_MENU);
        sysMenu.setParentId(parentId);
        sysMenu.setOrderNum("0");
        sysMenu.setQuery(String.format("{\"reportId\": %d}", reportSql.getId()));
        sysMenu.setPath("user");
        sysMenu.setStatus("0");
        sysMenu.setVisible("0");
//        String username = SecurityUtils.getLoginUser().getUsername();
//        sysMenu.setCreateBy(username);
        return sysMenu;
    }

}
