/*
 * Decompiled with CFR 0_123.
 */
package com.commoninf;

import com.commoninf.DbmsTable;
import java.util.ArrayList;

public class DataTable {
    private DbmsTable dbmsTable;
    private ArrayList<ArrayList<Object>> rows = new ArrayList();
    private ArrayList<String> colNames;

    public DataTable(DbmsTable t) {
        this.dbmsTable = t;
    }

    public DbmsTable getDbmsTable() {
        return this.dbmsTable;
    }

    public void addRow(ArrayList<Object> vals) {
        this.rows.add(vals);
    }

    public ArrayList<ArrayList<Object>> getRows() {
        return this.rows;
    }

    public void setColNames(ArrayList<Object> nms) {
        this.colNames = new ArrayList();
        for (Object o : nms) {
            String nm = (String)o;
            nm = nm.replaceFirst("\\(.*\\)", "");
            nm = nm.trim();
            this.colNames.add(nm);
        }
    }

    public ArrayList<String> getColNames() {
        return this.colNames;
    }
}

