/*
 * Decompiled with CFR 0_123.
 */
package com.commoninf;

import com.commoninf.DbmsTable;

public class DbmsColumn {
    private String columnName;
    private String dataType;
    private boolean pk;
    private boolean notNull;
    private boolean unique;
    private DbmsTable fkTable;
    private DbmsColumn fkColumn;
    private Boolean dateType;
    private Boolean timeType;
    private Boolean textType;

    public DbmsColumn(String name) {
        this.columnName = name;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String nm) {
        this.columnName = nm;
    }

    public String getDataType() {
        return this.dataType;
    }

    public boolean isDate() {
        if (this.dateType == null) {
            boolean b = false;
            if (this.dataType != null) {
                b = this.dataType.toLowerCase().indexOf("date") >= 0;
            }
            this.dateType = b;
        }
        return this.dateType;
    }

    public boolean isTime() {
        if (this.timeType == null) {
            boolean b = false;
            if (this.dataType != null) {
                boolean bl = b = this.dataType.toLowerCase().indexOf("text(5)") >= 0;
                if (b) {
                    b = this.columnName.toLowerCase().endsWith("_time");
                }
                this.timeType = b;
            }
        }
        return this.timeType;
    }

    public boolean isText() {
        if (this.textType == null) {
            boolean b = false;
            if (this.dataType != null) {
                b = this.dataType.toLowerCase().indexOf("text") >= 0;
            }
            this.textType = b;
        }
        return this.textType;
    }

    public void setDataType(String dt) {
        this.dataType = dt;
    }

    public boolean isPk() {
        return this.pk;
    }

    public void setPk(boolean b) {
        this.pk = b;
    }

    public DbmsTable getFkTable() {
        return this.fkTable;
    }

    public void setFkTable(DbmsTable t) {
        this.fkTable = t;
    }

    public DbmsColumn getFkColumn() {
        return this.fkColumn;
    }

    public void setFkColumn(DbmsColumn col) {
        this.fkColumn = col;
    }

    public boolean isUnique() {
        return this.unique;
    }

    public void setUnique(boolean b) {
        this.unique = b;
    }

    public boolean isNotNull() {
        return this.notNull;
    }

    public void setNotNull(boolean b) {
        this.notNull = b;
    }
}

