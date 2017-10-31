/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.poi.ss.usermodel.Cell
 *  org.apache.poi.ss.usermodel.Row
 *  org.apache.poi.xssf.usermodel.XSSFSheet
 *  org.apache.poi.xssf.usermodel.XSSFWorkbook
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.commoninf;

import com.commoninf.DataTable;
import com.commoninf.Database;
import com.commoninf.DbmsTable;
import com.commoninf.TCIException;
import com.commoninf.TestCaseInserter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestData {
    private static final Logger logger = LoggerFactory.getLogger(TestData.class);
    private ArrayList<DataTable> dataTables;
    private int errCount;

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    public boolean processTestData(List<String> fileNames) {
        ok = true;
        if (fileNames.size() == 0) {
            throw new TCIException("No data files specified");
        }
        TestData.logger.trace("Processing " + fileNames.size() + " data file(s)");
        dbs = Database.getDbs();
        conns = new Connection[dbs.length];
        try {
            try {
                i = 0;
                do {
                    block12 : {
                        if (i < dbs.length) break block12;
                        db = fileNames.iterator();
                        ** GOTO lbl36
                    }
                    db = dbs[i];
                    conns[i] = db.getConnection();
                    if (conns[i] == null) {
                        throw new TCIException("Failed to get connection for " + db.getDbType());
                    }
                    ++i;
                } while (true);
            }
            catch (Exception ex) {
                TestData.logger.error(ex.getMessage());
                ok = false;
                i = 0;
                ** GOTO lbl41
            }
        }
        catch (Throwable var7_12) {
            i = 0;
            ** GOTO lbl45
        }
lbl-1000: // 1 sources:
        {
            fn = (String)db.next();
            this.errCount = 0;
            if (this.readDataFromFile(fn)) {
                if (this.insertOneFileIntoDbs(dbs, conns)) continue;
                ok = false;
                continue;
            }
            ok = false;
lbl36: // 4 sources:
            ** while (db.hasNext())
        }
lbl37: // 1 sources:
        i = 0;
        ** GOTO lbl49
lbl-1000: // 1 sources:
        {
            dbs[i].tryClose(conns[i]);
            ++i;
lbl41: // 2 sources:
            ** while (i < dbs.length)
        }
lbl42: // 1 sources:
        return ok;
lbl-1000: // 1 sources:
        {
            dbs[i].tryClose(conns[i]);
            ++i;
lbl45: // 2 sources:
            ** while (i < dbs.length)
        }
lbl46: // 1 sources:
        throw var7_12;
lbl-1000: // 1 sources:
        {
            dbs[i].tryClose(conns[i]);
            ++i;
lbl49: // 2 sources:
            ** while (i < dbs.length)
        }
lbl50: // 1 sources:
        return ok;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    private boolean readDataFromFile(String fn) {
        block34 : {
            block35 : {
                block39 : {
                    block37 : {
                        block38 : {
                            ok = true;
                            workbook = null;
                            this.dataTables = new ArrayList<E>();
                            tci = TestCaseInserter.getInstance();
                            try {
                                dataFile = new File(fn);
                                tci.userVisibleMsg("Reading " + dataFile.getCanonicalPath(), true);
                                fis = new FileInputStream(dataFile);
                                workbook = new XSSFWorkbook((InputStream)fis);
                                i = 0;
lbl11: // 2 sources:
                                if (i >= workbook.getNumberOfSheets()) break block34;
                                sheet = workbook.getSheetAt(i);
                                sn = sheet.getSheetName();
                                table = TestCaseInserter.getDataModelInstance().findTable(sn);
                                if (table == null) {
                                    TestData.logger.error("Cannot find table " + sn + " in the metadata");
                                    return false;
                                }
                                dt = new DataTable(table);
                                this.dataTables.add(dt);
                                rowIterator = sheet.iterator();
                                rowNum = 0;
                                nullRowCnt = 0;
                                firstNullRow = -1;
lbl25: // 5 sources:
                                if (!rowIterator.hasNext()) break block35;
                                ++rowNum;
                                vals = new ArrayList<Object>();
                                row = (Row)rowIterator.next();
                                cellIterator = row.cellIterator();
                                block24 : do {
                                    block36 : {
                                        if (cellIterator.hasNext()) break block36;
                                        allNulls = true;
                                        someNulls = false;
                                        colIdx = vals.iterator();
                                        ** GOTO lbl88
                                    }
                                    cell = (Cell)cellIterator.next();
                                    val = null;
                                    badExcelType = null;
                                    switch (cell.getCellType()) {
                                        case 0: {
                                            val = cell.getNumericCellValue();
                                            ** break;
                                        }
                                        case 1: {
                                            val = cell.getStringCellValue();
                                            ** break;
                                        }
                                        case 3: {
                                            ** break;
                                        }
                                        case 4: {
                                            badExcelType = "boolean";
                                            ** break;
                                        }
                                        case 5: {
                                            badExcelType = "error";
                                            ** break;
                                        }
                                        case 2: {
                                            badExcelType = "formula";
                                            ** break;
                                        }
                                    }
                                    badExcelType = "unknown";
lbl59: // 7 sources:
                                    if (badExcelType != null) {
                                        ok = false;
                                        tci.userVisibleErrorMsg("Unexpected Excel type " + (String)badExcelType + " in table " + sn + " row " + rowNum + " column " + (cell.getColumnIndex() + 1));
                                        break block35;
                                    }
                                    colIdx = cell.getColumnIndex();
                                    cursz = vals.size();
                                    j = 0;
                                    do {
                                        if (j >= colIdx - cursz) {
                                            vals.add(val);
                                            continue block24;
                                        }
                                        vals.add(null);
                                        ++j;
                                    } while (true);
                                    break;
                                } while (true);
                            }
                            catch (Exception e) {
                                throw new TCIException(e.getMessage());
                            }
                            finally {
                                if (workbook != null) {
                                    try {
                                        workbook.close();
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
lbl-1000: // 1 sources:
                            {
                                o = colIdx.next();
                                if (o == null) {
                                    someNulls = true;
                                    continue;
                                }
                                allNulls = false;
lbl88: // 3 sources:
                                ** while (colIdx.hasNext())
                            }
lbl89: // 1 sources:
                            if (!allNulls) break block37;
                            if (rowNum != 1) break block38;
                            tci.userVisibleMsg("Skiping table " + sn + " because all column headers are empty", false);
                            break block35;
                        }
                        ++nullRowCnt;
                        if (firstNullRow != -1) ** GOTO lbl25
                        firstNullRow = rowNum;
                        ** GOTO lbl25
                    }
                    if (someNulls && rowNum == 1) {
                        throw new TCIException("Some, but not all, column headers for table " + sn + " are empty");
                    }
                    if (rowNum != 1) break block39;
                    dt.setColNames(vals);
                    ** GOTO lbl25
                }
                dt.addRow(vals);
                ** GOTO lbl25
            }
            if (nullRowCnt > 0 && nullRowCnt != rowNum - 1) {
                tci.userVisibleMsg("Skipping " + nullRowCnt + " row(s) in table " + sn + " because all values are empty.  First empty row = " + firstNullRow, false);
            }
            ++i;
            ** GOTO lbl11
        }
        TestData.logger.trace("Finished reading " + dataFile.getCanonicalPath());
        return ok;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    private boolean insertOneFileIntoDbs(Database[] dbs, Connection[] conns) {
        block29 : {
            ok = true;
            emsg = new StringBuilder();
            tci = TestCaseInserter.getInstance();
            if (tci.getDebug()) {
                for (DataTable dt : this.dataTables) {
                    colNames = dt.getColNames();
                    rows = dt.getRows();
                    TestData.logger.trace(String.valueOf(dt.getDbmsTable().getTableName()) + " contains " + colNames.size() + " column(s) and " + rows.size() + " row(s) of data ");
                }
            }
            db = null;
            try {
                for (i = 0; i < dbs.length; ++i) {
                    conns[i].setAutoCommit(false);
                }
            }
            catch (SQLException se) {
                ok = false;
                TestData.logger.error(se.getMessage());
                throw new TCIException("Failed to set auto commit mode to false for " + db.getDbType());
            }
            errLim = tci.getErrorLimit();
            dt = null;
            orderedTables = TestCaseInserter.getDataModelInstance().getOrderedTables();
            try {
                for (i = 0; i < dbs.length; ++i) {
                    block28 : {
                        db = dbs[i];
                        tci.userVisibleMsg("Inserting into " + db.getDbType(), true);
                        db.resetPkMap();
                        totalRowsInserted = 0;
                        var13_17 = orderedTables.iterator();
                        do lbl-1000: // 3 sources:
                        {
                            if (!var13_17.hasNext()) {
                                if (this.errCount <= 0) break block28;
                                ok = false;
                                tci.userVisibleErrorMsg(String.valueOf(this.errCount) + " error(s) inserting into " + db.getDbType());
                                break block29;
                            }
                            dbmsTable = var13_17.next();
                            dt = this.findDataTable(dbmsTable);
                            if (dt == null) ** GOTO lbl-1000
                            rowsInserted = this.insertIntoOneTable(db, conns[i], dt);
                            if (rowsInserted == -1) {
                                ok = false;
                                continue;
                            }
                            totalRowsInserted += rowsInserted;
                        } while (this.errCount < errLim || errLim <= 0);
                        tci.userVisibleErrorMsg("Error limit exceeded");
                        break;
                    }
                    tci.userVisibleMsg("  Inserted " + totalRowsInserted + " row(s) into " + db.getDbType(), true);
                }
            }
            catch (Exception e) {
                ok = false;
                emsg.append(String.valueOf(e.getMessage()) + "\n");
                emsg.append("Error inserting into " + dt.getDbmsTable().getTableName() + " for database " + db.getDbType() + "\n");
            }
        }
        alwaysRollBack = tci.getRollBack();
        if (ok && !alwaysRollBack) {
            tci.userVisibleMsg("Committing all inserts", true);
        } else {
            tci.userVisibleMsg("Rolling back all inserts", true);
        }
        i = 0;
        while (i < conns.length) {
            block30 : {
                try {
                    block32 : {
                        block31 : {
                            try {
                                if (ok && !alwaysRollBack) {
                                    tci.userVisibleMsg("Committing " + dbs[i].getDbType(), false);
                                    conns[i].commit();
                                } else {
                                    tci.userVisibleMsg("Rolling back " + dbs[i].getDbType(), false);
                                    conns[i].rollback();
                                }
                                break block30;
                            }
                            catch (Exception e) {
                                if (!ok) break block31;
                                emsg.append("Error committing database ");
                                j = 0;
                                ** GOTO lbl82
                            }
                        }
                        emsg.append("Error rolling back database ");
                        break block32;
lbl-1000: // 1 sources:
                        {
                            emsg.append("after successfully committing " + dbs[j].getDbType() + " ");
                            ++j;
lbl82: // 2 sources:
                            ** while (j < i)
                        }
                    }
                    emsg.append(dbs[i].getDbType());
                    ok = false;
                }
                finally {
                    try {
                        conns[i].setAutoCommit(true);
                    }
                    catch (SQLException e) {
                        TestData.logger.error("Error setting autocommit to true for " + dbs[i].getDbType());
                    }
                }
            }
            ++i;
        }
        if (emsg.length() <= 0) return ok;
        throw new TCIException(emsg.toString());
    }

    private DataTable findDataTable(DbmsTable dbmsTable) {
        DataTable match = null;
        for (DataTable dt : this.dataTables) {
            if (!dt.getDbmsTable().getTableName().equals(dbmsTable.getTableName())) continue;
            match = dt;
            break;
        }
        return match;
    }

    private int insertIntoOneTable(Database db, Connection conn, DataTable dt) {
        int rowsInserted = 0;
        int errLim = TestCaseInserter.getInstance().getErrorLimit();
        ArrayList<ArrayList<Object>> rows = dt.getRows();
        int i = 0;
        while (i < rows.size()) {
            ArrayList<Object> row = rows.get(i);
            boolean ok = db.insertRow(conn, dt.getDbmsTable(), dt.getColNames(), row);
            if (!ok) {
                rowsInserted = -1;
                TestCaseInserter.getInstance().userVisibleErrorMsg("Error inserting row " + (i + 2) + " into " + dt.getDbmsTable().getTableName());
                if (++this.errCount >= errLim && errLim > 0) {
                    break;
                }
            } else if (rowsInserted >= 0) {
                ++rowsInserted;
            }
            ++i;
        }
        if (rowsInserted > 0) {
            TestCaseInserter.getInstance().userVisibleMsg("  " + dt.getDbmsTable().getTableName() + "  inserted " + rowsInserted + " row(s)", true);
        }
        return rowsInserted;
    }
}

