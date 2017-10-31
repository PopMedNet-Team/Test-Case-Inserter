/*
 * Decompiled with CFR 0_123.
 */
package com.commoninf;

import com.commoninf.DbmsColumn;
import java.util.ArrayList;
import java.util.List;

public class DbmsTable {
    private String tableName;
    private List<DbmsColumn> columns;
    private String compositeKey;

    public DbmsTable(String name) {
        this.tableName = name;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String n) {
        this.tableName = n;
    }

    public List<DbmsColumn> getColumns() {
        return this.columns;
    }

    public void setColumns(List<DbmsColumn> cols) {
        this.columns = cols;
    }

    public void addColumn(DbmsColumn col) {
        if (this.columns == null) {
            this.columns = new ArrayList<DbmsColumn>();
        }
        this.columns.add(col);
    }

    public DbmsColumn findCol(String colName) {
        DbmsColumn col = null;
        for (DbmsColumn c : this.columns) {
            if (!c.getColumnName().equals(colName)) continue;
            col = c;
            break;
        }
        return col;
    }

    public String getCompositeKey() {
        return this.compositeKey;
    }

    public void setCompositeKey(String ck) {
        this.compositeKey = ck;
    }
}

