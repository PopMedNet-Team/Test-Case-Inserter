/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.poi.ss.usermodel.DateUtil
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.commoninf;

import com.commoninf.DataModel;
import com.commoninf.DbmsColumn;
import com.commoninf.DbmsTable;
import com.commoninf.OracleDatabase;
import com.commoninf.PgDatabase;
import com.commoninf.SSDatabase;
import com.commoninf.TCIException;
import com.commoninf.TestCaseInserter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);
    private String dbType;
    private DataModel dm;
    private List<DbmsTable> orderedTables;
    protected String[] connProps;
    protected static final String TCI_SEQ = "tci_seq";
    protected static final int XLEN = 800;
    protected static final String SSDB = "Sql Server";
    protected static final String PGDB = "Postgres";
    protected static final String ORACLEDB = "Oracle";
    public static final int URL_PROP = 0;
    public static final int USER_PROP = 1;
    public static final int PWD_PROP = 2;
    public static final int SCHEMA_PROP = 3;
    private HashMap<String, String> sqlForTable = new HashMap();
    private HashMap<String, HashMap<Object, String>> pkMap;
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

    private Database() {
    }

    public Database(String type) {
        this.dbType = type;
        this.dm = TestCaseInserter.getDataModelInstance();
        if (this.dm != null) {
            this.orderedTables = this.dm.getOrderedTables();
        }
    }

    public abstract String dataType(String var1);

    public abstract String dropOneTable(DbmsTable var1);

    public abstract String pkConstraint(DbmsTable var1, DbmsColumn var2);

    public abstract String uniqueConstraint(DbmsTable var1, DbmsColumn var2);

    public abstract String notNullConstraint(DbmsTable var1, DbmsColumn var2);

    public abstract String fkConstraint(DbmsTable var1, DbmsColumn var2);

    public abstract String getDatabasePropertyName();

    public abstract String getSequenceQuery();

    public abstract String createSequence();

    public abstract String dropSequence();

    public abstract String getDefaultDriver();

    public static Database[] getDbs() {
        String databases = TestCaseInserter.getInstance().getDatabases();
        Database[] dbs = new Database[databases.length()];
        int i = 0;
        while (i < dbs.length) {
            char dbType = databases.charAt(i);
            switch (dbType) {
                case 'O': 
                case 'o': {
                    dbs[i] = new OracleDatabase();
                    break;
                }
                case 'S': 
                case 's': {
                    dbs[i] = new SSDatabase();
                    break;
                }
                case 'P': 
                case 'p': {
                    dbs[i] = new PgDatabase();
                }
            }
            ++i;
        }
        return dbs;
    }

    public String dropSql() {
        StringBuilder sb = new StringBuilder();
        String dropSeq = this.dropSequence();
        sb.append(String.valueOf(dropSeq) + ";\n");
        int i = this.orderedTables.size() - 1;
        while (i >= 0) {
            DbmsTable t = this.orderedTables.get(i);
            String drop = this.dropOneTable(t);
            sb.append(String.valueOf(drop) + ";\n");
            --i;
        }
        return sb.toString();
    }

    public String createSql() {
        StringBuilder sb = new StringBuilder();
        String createSeq = this.createSequence();
        sb.append(String.valueOf(createSeq) + ";\n");
        for (DbmsTable t : this.orderedTables) {
            sb.append("create table " + t.getTableName() + " (\n");
            int sz = t.getColumns().size();
            int i = 0;
            while (i < sz) {
                DbmsColumn c = t.getColumns().get(i);
                sb.append("  ");
                sb.append(String.valueOf(c.getColumnName()) + " ");
                String dt = c.getDataType();
                dt = dt.replace("RDBMS ", "").toLowerCase();
                dt = this.dataType(dt);
                sb.append(dt);
                if (i < sz - 1) {
                    sb.append(",");
                }
                sb.append("\n");
                ++i;
            }
            sb.append(");\n\n");
        }
        return sb.toString();
    }

    public String createRelations() {
        List<DbmsColumn> cols;
        StringBuilder sb = new StringBuilder();
        sb.append("-- Primary Keys\n");
        block0 : for (DbmsTable t : this.orderedTables) {
            cols = t.getColumns();
            for (DbmsColumn c : cols) {
                if (!c.isPk()) continue;
                String pk = this.pkConstraint(t, c);
                sb.append(String.valueOf(pk) + "\n");
                continue block0;
            }
        }
        sb.append("\n-- Foreign Keys\n");
        block2 : for (DbmsTable t : this.orderedTables) {
            cols = t.getColumns();
            for (DbmsColumn c : cols) {
                if (c.getFkTable() == null) continue;
                String fk = this.fkConstraint(t, c);
                sb.append(String.valueOf(fk) + "\n");
                continue block2;
            }
        }
        return sb.toString();
    }

    public String createConstraints() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n-- Constraints\n");
        for (DbmsTable t : this.orderedTables) {
            List<DbmsColumn> cols = t.getColumns();
            for (DbmsColumn c : cols) {
                if (c.isPk() && !this.getDbType().equals("Sql Server")) continue;
                if (c.isNotNull() || c.isPk()) {
                    String nn = this.notNullConstraint(t, c);
                    sb.append(String.valueOf(nn) + "\n");
                }
                if (!c.isUnique()) continue;
                String un = this.uniqueConstraint(t, c);
                sb.append(String.valueOf(un) + "\n");
            }
        }
        return sb.toString();
    }

    public static boolean testConns() {
        String msg = "";
        Database[] dbs = Database.getDbs();
        boolean ok = true;
        Database[] arrdatabase = dbs;
        int n = arrdatabase.length;
        int n2 = 0;
        while (n2 < n) {
            Database db = arrdatabase[n2];
            if (!db.testConn()) {
                ok = false;
                msg = String.valueOf(msg) + db.getDbType() + " ";
            }
            ++n2;
        }
        TestCaseInserter tci = TestCaseInserter.getInstance();
        if (msg.length() == 0) {
            tci.encryptPasswords();
            tci.userVisibleMsg("Successfully tested database connections", true);
        } else {
            tci.userVisibleErrorMsg("Error connecting to " + msg);
        }
        return ok;
    }

    public String[] getConnectionProperties() {
        if (this.connProps == null) {
            String dbPropName = this.getDatabasePropertyName();
            this.connProps = new String[4];
            TestCaseInserter tci = TestCaseInserter.getInstance();
            Properties props = tci.getProperties();
            this.connProps[0] = props.getProperty("db." + dbPropName + ".url");
            this.connProps[1] = props.getProperty("db." + dbPropName + ".user");
            this.connProps[2] = props.getProperty("db." + dbPropName + ".password");
            this.connProps[3] = props.getProperty("db." + dbPropName + ".schema");
        }
        return this.connProps;
    }

    private String getDriver() {
        TestCaseInserter tci = TestCaseInserter.getInstance();
        Properties props = tci.getProperties();
        String driver = props.getProperty("db." + this.getDatabasePropertyName() + ".driver");
        if (driver == null) {
            driver = this.getDefaultDriver();
        }
        return driver;
    }

    public Connection getConnection() {
        this.getConnectionProperties();
        logger.trace("Connecting to " + this.getDbDebugInfo());
        String driver = null;
        String jver = System.getProperty("java.version");
        if (jver != null && jver.startsWith("1.7")) {
            driver = this.getDriver();
            logger.trace("Using driver " + driver);
        }
        Connection conn = null;
        try {
            if (driver != null) {
                Class.forName(driver);
            }
            conn = DriverManager.getConnection(this.connProps[0], this.connProps[1], this.connProps[2]);
        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        logger.trace(String.valueOf(conn == null ? "Failed to connect" : "Connected") + " to " + this.getDbType());
        return conn;
    }

    public String getDbDebugInfo() {
        this.getConnectionProperties();
        String msg = "";
        int i = this.connProps[0].indexOf("//");
        if (i >= 0) {
            msg = String.valueOf(this.connProps[0].substring(i)) + " ";
        }
        msg = String.valueOf(msg) + this.connProps[1];
        return msg;
    }

    public boolean testConn() {
        boolean ok = false;
        TestCaseInserter.getInstance().userVisibleMsg("Testing " + this.getDbType() + " " + this.getDbDebugInfo(), true);
        Connection conn = this.getConnection();
        if (conn != null) {
            ok = true;
            this.tryClose(conn);
        }
        return ok;
    }

    public String getDbType() {
        return this.dbType;
    }

    protected String getSql(DbmsTable table, ArrayList<String> colNames) {
        String tn = table.getTableName();
        String sql = this.sqlForTable.get(tn);
        if (sql == null) {
            StringBuilder stmt = new StringBuilder();
            StringBuilder vals = new StringBuilder();
            stmt.append("insert into ").append(this.connProps[3]).append(".").append(tn).append(" (");
            vals.append("\n  values(");
            int i = 0;
            while (i < colNames.size()) {
                if (i > 0) {
                    stmt.append(", ");
                    vals.append(", ");
                }
                stmt.append(colNames.get(i));
                vals.append("?");
                ++i;
            }
            stmt.append(") ");
            vals.append(")");
            stmt.append(vals);
            sql = stmt.toString();
            this.sqlForTable.put(tn, sql);
            if (TestCaseInserter.getInstance().getDebug()) {
                logger.trace(sql);
            }
        }
        return sql;
    }

    public void resetPkMap() {
        this.pkMap = new HashMap<K, V>();
    }

    private long getNextSeqVal(Connection conn) {
        String sql = this.getSequenceQuery();
        long seq = -1;
        Long res = this.getLong(conn, sql, null);
        if (res != null) {
            seq = res;
        }
        return seq;
    }

    protected String genPk(Connection conn, String tableName, Object surrogateVal) {
        String realPk = null;
        long seq = this.getNextSeqVal(conn);
        realPk = "PK_" + seq;
        HashMap tableMap = this.pkMap.get(tableName);
        if (tableMap == null) {
            tableMap = new HashMap<K, V>();
            this.pkMap.put(tableName, tableMap);
        }
        tableMap.put(surrogateVal, realPk);
        return realPk;
    }

    protected String lookupPk(String tableName, Object surrogateVal) {
        String pkVal = null;
        HashMap<Object, String> tableMap = this.pkMap.get(tableName);
        if (tableMap != null) {
            pkVal = tableMap.get(surrogateVal);
        }
        return pkVal;
    }

    protected Object[] getVals(Connection conn, DbmsTable table, ArrayList<String> colNames, ArrayList<Object> row) {
        String tn = table.getTableName();
        int maxIdx = row.size() > colNames.size() ? colNames.size() : row.size();
        Object[] vals = new Object[colNames.size()];
        int i = 0;
        while (i < maxIdx) {
            String colName = colNames.get(i);
            DbmsColumn col = table.findCol(colName);
            if (col == null) {
                throw new TCIException("Column lookup error:  " + tn + "." + colName);
            }
            Object val = row.get(i);
            if (col.isPk()) {
                if (val == null) {
                    throw new TCIException("Primary key value for " + tn + "." + colName + " cannot be null");
                }
                String realPk = this.lookupPk(tn, val);
                if (realPk != null) {
                    throw new TCIException("Duplicate value " + val + " for primary key column " + tn + "." + colName);
                }
                realPk = this.genPk(conn, tn, val);
                vals[i] = realPk;
            } else if (col.getFkTable() != null) {
                String fkTable = col.getFkTable().getTableName();
                String realFk = this.lookupPk(fkTable, val);
                if (realFk == null) {
                    String emsg = "Undefined primary key in table " + fkTable + " for surrogate value " + val.toString() + " in " + tn + "." + colName;
                    throw new TCIException(emsg);
                }
                vals[i] = realFk;
            } else {
                vals[i] = col.isDate() ? this.convertToDate(val) : (col.isTime() ? this.convertToTime(val) : val);
            }
            ++i;
        }
        return vals;
    }

    private java.util.Date convertToDate(Object val) {
        java.util.Date d = null;
        if (val != null) {
            if (val instanceof Double) {
                d = DateUtil.getJavaDate((double)((Double)val));
            } else if (val instanceof java.util.Date) {
                d = (java.util.Date)val;
            } else if (val instanceof String) {
                try {
                    dateFormatter.setLenient(false);
                    d = dateFormatter.parse((String)val);
                }
                catch (ParseException pe) {
                    throw new TCIException(pe.getMessage());
                }
            } else {
                throw new TCIException("Unexpected date value of type:  " + val.getClass().getName());
            }
        }
        if (d != null) {
            d = new Date(d.getTime());
        }
        return d;
    }

    private String convertToTime(Object val) {
        String time = null;
        if (val != null) {
            if (val instanceof Double) {
                double dval = (Double)val;
                int mins = (int)(dval * 1440.0);
                int hrs = mins / 60;
                time = String.valueOf(String.format("%02d", hrs)) + ":" + String.format("%02d", mins %= 60);
            } else if (val instanceof String) {
                time = (String)val;
            } else {
                throw new TCIException("Unexpected time value of type:  " + val.getClass().getName());
            }
        }
        return time;
    }

    public boolean insertRow(Connection conn, DbmsTable table, ArrayList<String> colNames, ArrayList<Object> row) {
        boolean ok;
        ok = true;
        String sql = this.getSql(table, colNames);
        PreparedStatement pstmt = null;
        try {
            try {
                Object[] vals = this.getVals(conn, table, colNames, row);
                pstmt = this.createAndBind(conn, sql, vals);
                int result = pstmt.executeUpdate();
                if (result != 1) {
                    throw new TCIException("Insert returned " + result + " instead of 1");
                }
            }
            catch (Exception e) {
                ok = false;
                TestCaseInserter.getInstance().userVisibleErrorMsg("Error inserting into " + table.getTableName() + " " + e.getMessage());
                this.tryClose(pstmt);
            }
        }
        finally {
            this.tryClose(pstmt);
        }
        return ok;
    }

    private Long getLong(Connection conn, String query, Object[] params) {
        Long res;
        res = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            try {
                Object o;
                pstmt = this.createAndBind(conn, query, params);
                rs = pstmt.executeQuery();
                if (rs.next() && (o = rs.getObject(1)) instanceof Number) {
                    res = ((Number)o).longValue();
                }
            }
            catch (SQLException se) {
                throw new TCIException(String.valueOf(se.getMessage()) + " " + query);
            }
        }
        finally {
            this.tryClose(rs);
            this.tryClose(pstmt);
        }
        if (TestCaseInserter.getInstance().getDebug()) {
            logger.trace(String.valueOf(query) + " returned " + (res == null ? "null" : res.toString()));
        }
        return res;
    }

    private String formatValue(Object val) {
        String v = "";
        try {
            if (val == null) {
                v = "NULL";
            } else if (val instanceof String) {
                v = (String)val;
            } else if (val instanceof java.util.Date) {
                java.util.Date d = (java.util.Date)val;
                v = dateFormatter.format(d);
            } else {
                v = val instanceof Number ? val.toString() : "unknown type";
            }
        }
        catch (Exception e) {
            v = "data format error";
        }
        return v;
    }

    protected String formatSql(PreparedStatement pstmt, String query, Object[] params) {
        String s = query;
        if (params != null) {
            int i = 0;
            while (i < params.length) {
                String val = this.formatValue(params[i]);
                s = s.replaceFirst("\\?", val);
                ++i;
            }
        }
        return s;
    }

    private void logSql(PreparedStatement pstmt, String query, Object[] params) {
        String formattedSql = this.formatSql(pstmt, query, params);
        logger.trace(formattedSql);
    }

    private PreparedStatement createAndBind(Connection conn, String query, Object[] params) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(query);
        if (params != null) {
            int i = 0;
            while (i < params.length) {
                Object param = params[i];
                pstmt.setObject(i + 1, param);
                ++i;
            }
        }
        if (TestCaseInserter.getInstance().getDebug()) {
            this.logSql(pstmt, query, params);
        }
        return pstmt;
    }

    private void tryClose(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            }
            catch (SQLException se) {
                logger.error("Error closing ResultSet:  " + se.getMessage());
            }
        }
    }

    private void tryClose(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            }
            catch (SQLException se) {
                logger.error("Error closing PreparedStatement:  " + se.getMessage());
            }
        }
    }

    public void tryClose(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException se) {
                logger.error("Error closing Connection:  " + se.getMessage());
            }
        }
    }
}

