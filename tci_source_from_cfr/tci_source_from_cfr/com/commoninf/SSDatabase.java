/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.commoninf;

import com.commoninf.Database;
import com.commoninf.DbmsColumn;
import com.commoninf.DbmsTable;
import com.commoninf.TCIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSDatabase
extends Database {
    private static final Logger logger = LoggerFactory.getLogger(SSDatabase.class);
    private static String pkTempl = "alter table TABLE add constraint pkTABLE primary key (COL);";
    private static String fkTempl = "alter table FKTABLE add constraint fk_FKTABLE_PKTABLE foreign key (FKCOL) references PKTABLE (PKCOL);";
    private static String uniqueTempl = "alter table TABLE add constraint uc_TABLE_COL unique (COL);";
    private static String notNullTempl = "alter table TABLE alter column COL DATATYPE not null;";

    public SSDatabase() {
        super("Sql Server");
    }

    @Override
    public String dropOneTable(DbmsTable tab) {
        StringBuilder sb = new StringBuilder();
        sb.append("drop table " + tab.getTableName());
        return sb.toString();
    }

    @Override
    public String dataType(String dt) {
        if ((dt = dt.replace("RDBMS ", "").toLowerCase()).equals("date")) {
            dt = "date";
        } else if (dt.contains("number")) {
            dt = "float";
        } else if (dt.contains("text(x)")) {
            dt = "varchar(800)";
        } else if (dt.contains("text")) {
            dt = dt.replace("text", "varchar");
        } else {
            throw new TCIException("unknown data type " + dt);
        }
        return dt;
    }

    @Override
    public String pkConstraint(DbmsTable table, DbmsColumn col) {
        String pk = pkTempl;
        pk = pk.replaceAll("TABLE", table.getTableName());
        pk = pk.replaceAll("COL", col.getColumnName());
        return pk;
    }

    @Override
    public String fkConstraint(DbmsTable table, DbmsColumn col) {
        String fk = fkTempl;
        fk = fk.replaceAll("FKTABLE", table.getTableName());
        fk = fk.replaceAll("PKTABLE", col.getFkTable().getTableName());
        fk = fk.replaceAll("FKCOL", col.getColumnName());
        fk = fk.replaceAll("PKCOL", col.getFkColumn().getColumnName());
        return fk;
    }

    @Override
    public String uniqueConstraint(DbmsTable table, DbmsColumn col) {
        String un = uniqueTempl;
        un = un.replaceAll("TABLE", table.getTableName());
        un = un.replaceAll("COL", col.getColumnName());
        return un;
    }

    @Override
    public String notNullConstraint(DbmsTable table, DbmsColumn col) {
        String nn = notNullTempl;
        nn = nn.replaceAll("TABLE", table.getTableName());
        nn = nn.replaceAll("COL", col.getColumnName());
        String dt = this.dataType(col.getDataType());
        nn = nn.replaceAll("DATATYPE", dt);
        return nn;
    }

    @Override
    public String getDatabasePropertyName() {
        return "sqlserver";
    }

    @Override
    public String getDefaultDriver() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    public String createSequence() {
        return "create sequence tci_seq start with 1";
    }

    @Override
    public String dropSequence() {
        return "drop sequence tci_seq";
    }

    @Override
    public String getSequenceQuery() {
        String sql = "select next value for " + this.connProps[3] + "." + "tci_seq";
        return sql;
    }
}

