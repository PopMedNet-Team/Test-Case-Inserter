/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.poi.ss.usermodel.Cell
 *  org.apache.poi.ss.usermodel.CellStyle
 *  org.apache.poi.ss.usermodel.IndexedColors
 *  org.apache.poi.ss.usermodel.Row
 *  org.apache.poi.xssf.usermodel.XSSFCell
 *  org.apache.poi.xssf.usermodel.XSSFCellStyle
 *  org.apache.poi.xssf.usermodel.XSSFDataFormat
 *  org.apache.poi.xssf.usermodel.XSSFRow
 *  org.apache.poi.xssf.usermodel.XSSFSheet
 *  org.apache.poi.xssf.usermodel.XSSFWorkbook
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.commoninf;

import com.commoninf.DbmsColumn;
import com.commoninf.DbmsTable;
import com.commoninf.TCIException;
import com.commoninf.TestCaseInserter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataModel {
    private static final Logger logger = LoggerFactory.getLogger(DataModel.class);
    List<DbmsTable> tables;
    private static final String TEMPLATE_FILE = "tciTemplate.xlsx";
    private List<DbmsTable> orderedTables;
    private TreeMap<String, DbmsTable> processed;

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    public void processDataModel() {
        workbook = null;
        TestCaseInserter.getInstance().userVisibleMsg("Processing data model", true);
        try {
            try {
                var2_2 = null;
                var3_6 = null;
                try {
                    is = TestCaseInserter.getInstance().openDataModel();
                    try {
                        processedSheets = 0;
                        workbook = new XSSFWorkbook(is);
                        i = 0;
                        do {
                            block29 : {
                                block28 : {
                                    if (i >= workbook.getNumberOfSheets()) {
                                        if (processedSheets == 3) break;
                                        throw new TCIException("Data model file did not contain all expected worksheets - FIELDS, RELATIONAL and CONSTRAINTS");
                                    }
                                    sheet = workbook.getSheetAt(i);
                                    var8_12 = sheet.getSheetName();
                                    switch (var8_12.hashCode()) {
                                        case 247948071: {
                                            if (var8_12.equals("RELATIONAL")) break;
                                            ** break;
                                        }
                                        case 546257430: {
                                            if (!var8_12.equals("CONSTRAINTS")) {
                                                ** break;
                                            }
                                            break block28;
                                        }
                                        case 2073588409: {
                                            if (var8_12.equals("FIELDS")) {
                                                this.processFields(sheet);
                                                ++processedSheets;
                                                ** break;
                                            }
                                            break block29;
                                        }
                                    }
                                    this.processRelations(sheet);
                                    ++processedSheets;
                                    ** break;
                                }
                                this.processConstraints(sheet);
                                ++processedSheets;
                            }
                            ++i;
                        } while (true);
                    }
                    finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
                catch (Throwable var3_7) {
                    if (var2_2 == null) {
                        var2_2 = var3_7;
                        throw var2_2;
                    }
                    if (var2_2 == var3_7) throw var2_2;
                    var2_2.addSuppressed(var3_7);
                    throw var2_2;
                }
            }
            catch (TCIException tcie) {
                throw tcie;
            }
            catch (Exception e) {
                throw new TCIException(e.getMessage());
            }
        }
        finally {
            if (workbook != null) {
                try {
                    workbook.close();
                }
                catch (IOException e) {
                    DataModel.logger.error(e.getMessage());
                }
            }
        }
        this.orderTables();
        DataModel.logger.trace("Finished processing data model");
    }

    public void createTemplate() {
        XSSFWorkbook workbook = null;
        TestCaseInserter.getInstance().userVisibleMsg("Creating template tciTemplate.xlsx", true);
        try {
            try {
                Throwable throwable = null;
                Object var3_5 = null;
                try {
                    FileOutputStream fos = new FileOutputStream("tciTemplate.xlsx");
                    try {
                        workbook = new XSSFWorkbook();
                        CellStyle pkStyle = null;
                        CellStyle fkStyle = null;
                        CellStyle nnStyle = null;
                        CellStyle genericStyle = null;
                        CellStyle textStyle = this.textCellStyle(workbook);
                        for (DbmsTable t : this.tables) {
                            workbook.createSheet(t.getTableName());
                            XSSFSheet sheet = workbook.getSheet(t.getTableName());
                            XSSFRow row = sheet.createRow(0);
                            int i = 0;
                            while (i < t.getColumns().size()) {
                                DbmsColumn c = t.getColumns().get(i);
                                XSSFCell cell = row.createCell(i);
                                String nm = c.getColumnName();
                                if (c.isText()) {
                                    String dt = c.getDataType();
                                    int lparen = dt.indexOf("(");
                                    if (lparen >= 0) {
                                        nm = String.valueOf(nm) + " " + dt.substring(lparen);
                                    }
                                    sheet.setDefaultColumnStyle(i, textStyle);
                                }
                                cell.setCellValue(nm);
                                sheet.setColumnWidth(i, 2 * sheet.getColumnWidth(i));
                                CellStyle cs = cell.getCellStyle();
                                if (c.isPk()) {
                                    if (pkStyle == null) {
                                        pkStyle = this.cstyle(workbook, cs, IndexedColors.LIGHT_YELLOW);
                                    }
                                    cell.setCellStyle(pkStyle);
                                } else if (c.getFkTable() != null) {
                                    if (fkStyle == null) {
                                        fkStyle = this.cstyle(workbook, cs, IndexedColors.LIGHT_CORNFLOWER_BLUE);
                                    }
                                    cell.setCellStyle(fkStyle);
                                } else if (c.isNotNull()) {
                                    if (nnStyle == null) {
                                        nnStyle = this.cstyle(workbook, cs, IndexedColors.LIGHT_GREEN);
                                    }
                                    cell.setCellStyle(nnStyle);
                                } else {
                                    if (genericStyle == null) {
                                        genericStyle = this.cstyle(workbook, cs, null);
                                    }
                                    cell.setCellStyle(genericStyle);
                                }
                                ++i;
                            }
                            sheet.createFreezePane(0, 1);
                        }
                        workbook.write((OutputStream)fos);
                    }
                    finally {
                        if (fos != null) {
                            fos.close();
                        }
                    }
                }
                catch (Throwable throwable2) {
                    if (throwable == null) {
                        throwable = throwable2;
                    } else if (throwable != throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
            }
            catch (Exception e) {
                throw new TCIException(e.getMessage());
            }
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
    }

    private CellStyle textCellStyle(XSSFWorkbook workbook) {
        XSSFDataFormat fmt = workbook.createDataFormat();
        XSSFCellStyle textStyle = workbook.createCellStyle();
        textStyle.setDataFormat(fmt.getFormat("@"));
        return textStyle;
    }

    private CellStyle cstyle(XSSFWorkbook workbook, CellStyle cs, IndexedColors color) {
        XSSFCellStyle ncs = workbook.createCellStyle();
        cs.cloneStyleFrom((CellStyle)ncs);
        if (color != null) {
            short idx = color.getIndex();
            ncs.setFillPattern(1);
            ncs.setFillForegroundColor(idx);
        }
        ncs.setDataFormat(0);
        return ncs;
    }

    private void writeFile(String fn, String contents) {
        try {
            Throwable throwable = null;
            Object var4_6 = null;
            try {
                FileOutputStream fos = new FileOutputStream(fn);
                try {
                    fos.write(contents.getBytes());
                    fos.close();
                }
                finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
            catch (Throwable throwable2) {
                if (throwable == null) {
                    throwable = throwable2;
                } else if (throwable != throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        }
        catch (Exception e) {
            throw new TCIException("Error writing to " + fn + " " + e.getMessage());
        }
    }

    /*
     * Exception decompiling
     */
    public void genScripts() {
        // This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
        // org.benf.cfr.reader.util.CannotPerformDecode: reachable test BLOCK was exited and re-entered.
        // org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.Misc.getFarthestReachableInRange(Misc.java:143)
        // org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.examineSwitchContiguity(SwitchReplacer.java:385)
        // org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.SwitchReplacer.replaceRawSwitches(SwitchReplacer.java:65)
        // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:423)
        // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:217)
        // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:162)
        // org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:95)
        // org.benf.cfr.reader.entities.Method.analyse(Method.java:357)
        // org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:769)
        // org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:701)
        // org.benf.cfr.reader.Main.doJar(Main.java:134)
        // org.benf.cfr.reader.Main.main(Main.java:189)
        throw new IllegalStateException("Decompilation failed");
    }

    private void processFields(XSSFSheet sheet) {
        if (TestCaseInserter.getInstance().getDebug()) {
            logger.trace("processFields sheet " + sheet.getSheetName());
        }
        this.tables = new ArrayList<DbmsTable>();
        Iterator rowIterator = sheet.iterator();
        Row row = (Row)rowIterator.next();
        DbmsTable table = null;
        while (rowIterator.hasNext()) {
            row = (Row)rowIterator.next();
            DbmsColumn col = null;
            Iterator cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = (Cell)cellIterator.next();
                switch (cell.getColumnIndex()) {
                    case 0: {
                        String tableName = cell.getStringCellValue().trim();
                        if (table == null || !tableName.equals(table.getTableName())) {
                            if (table != null) {
                                this.tables.add(table);
                            }
                            table = new DbmsTable(tableName);
                        }
                        col = null;
                        break;
                    }
                    case 1: {
                        String colName = cell.getStringCellValue();
                        col = new DbmsColumn(colName);
                        table.addColumn(col);
                        break;
                    }
                    case 2: {
                        String dataType = cell.getStringCellValue();
                        col.setDataType(dataType);
                        break;
                    }
                }
            }
        }
        this.tables.add(table);
        if (TestCaseInserter.getInstance().getDebug()) {
            int totalCols = 0;
            for (DbmsTable t : this.tables) {
                List<DbmsColumn> cols = t.getColumns();
                totalCols += cols.size();
                logger.trace(String.valueOf(t.getTableName()) + " has " + cols.size() + " columns");
            }
            logger.trace("Total tables = " + this.tables.size() + ", total columns = " + totalCols);
        }
    }

    private void processRelations(XSSFSheet sheet) {
        if (TestCaseInserter.getInstance().getDebug()) {
            logger.trace("processRelations sheet " + sheet.getSheetName());
        }
        Iterator rowIterator = sheet.iterator();
        Row row = (Row)rowIterator.next();
        DbmsTable table = null;
        block5 : while (rowIterator.hasNext()) {
            row = (Row)rowIterator.next();
            Iterator cellIterator = row.cellIterator();
            String relation = null;
            while (cellIterator.hasNext()) {
                Cell cell = (Cell)cellIterator.next();
                switch (cell.getColumnIndex()) {
                    case 0: {
                        String tableName = cell.getStringCellValue();
                        table = this.findTable(tableName);
                        break;
                    }
                    case 1: {
                        relation = cell.getStringCellValue();
                        break;
                    }
                    case 2: {
                        String details = cell.getStringCellValue();
                        if (relation.equals("PK")) {
                            DbmsColumn pkcol = table.findCol(details);
                            pkcol.setPk(true);
                            break;
                        }
                        if (relation.indexOf("FK") >= 0) {
                            this.handleFk(table, details);
                            break;
                        }
                        if (relation.equals("Composite Key")) {
                            table.setCompositeKey(details);
                            break;
                        }
                        throw new TCIException("Unknown relation:  " + relation + ", table = " + table.getTableName());
                    }
                    default: {
                        table = null;
                        continue block5;
                    }
                }
            }
        }
    }

    private void processConstraints(XSSFSheet sheet) {
        if (TestCaseInserter.getInstance().getDebug()) {
            logger.trace("processConstraints sheet " + sheet.getSheetName());
        }
        Iterator rowIterator = sheet.iterator();
        Row row = (Row)rowIterator.next();
        DbmsTable table = null;
        block5 : while (rowIterator.hasNext()) {
            row = (Row)rowIterator.next();
            Iterator cellIterator = row.cellIterator();
            DbmsColumn column = null;
            String colName = null;
            block6 : while (cellIterator.hasNext()) {
                Cell cell = (Cell)cellIterator.next();
                switch (cell.getColumnIndex()) {
                    case 0: {
                        String tableName = cell.getStringCellValue();
                        table = this.findTable(tableName);
                        break;
                    }
                    case 1: {
                        colName = cell.getStringCellValue();
                        column = table.findCol(colName);
                        break;
                    }
                    case 2: {
                        String constraint = cell.getStringCellValue();
                        if (column == null) {
                            if (!TestCaseInserter.getInstance().getDebug()) continue block6;
                            logger.trace("Skipping " + constraint + ":  table = " + table.getTableName() + ", column = " + colName);
                            break;
                        }
                        if (constraint.equals("unique")) {
                            column.setUnique(true);
                            break;
                        }
                        if (constraint.indexOf("not null") >= 0) {
                            column.setNotNull(true);
                            break;
                        }
                        throw new TCIException("Unknown constraint:  " + constraint + ", table = " + table.getTableName() + ", column = " + colName);
                    }
                    default: {
                        table = null;
                        continue block5;
                    }
                }
            }
        }
    }

    public DbmsTable findTable(String name) {
        DbmsTable t = null;
        for (DbmsTable tab : this.tables) {
            if (!tab.getTableName().equals(name)) continue;
            t = tab;
            break;
        }
        return t;
    }

    private void handleFk(DbmsTable table, String details) {
        details = details.trim();
        String[] tabcols = details.split(" *references *");
        String[] fk = tabcols[0].split("\\.");
        String[] pk = tabcols[1].split("\\.");
        DbmsColumn fkcol = table.findCol(fk[1]);
        DbmsTable pktab = this.findTable(pk[0]);
        DbmsColumn pkcol = pktab.findCol(pk[1]);
        fkcol.setFkTable(pktab);
        fkcol.setFkColumn(pkcol);
    }

    public List<DbmsTable> getOrderedTables() {
        return this.orderedTables;
    }

    private void orderTables() {
        this.processed = new TreeMap();
        this.orderedTables = new ArrayList<DbmsTable>();
        for (DbmsTable t : this.tables) {
            this.procOneTable(t, 0);
        }
    }

    private void procOneTable(DbmsTable t, int level) {
        if (level > 10) {
            throw new TCIException("Circular dependency?");
        }
        if (!this.processed.containsKey(t.getTableName())) {
            List<DbmsColumn> cols = t.getColumns();
            for (DbmsColumn c : cols) {
                DbmsTable referencedTable = c.getFkTable();
                if (referencedTable == null) continue;
                this.procOneTable(referencedTable, level + 1);
            }
            this.processed.put(t.getTableName(), t);
            this.orderedTables.add(t);
        }
    }
}

