/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.commoninf;

import com.commoninf.DataModel;
import com.commoninf.Database;
import com.commoninf.TCIException;
import com.commoninf.TCIProperties;
import com.commoninf.TestData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseInserter {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseInserter.class);
    private static final int DEF_ERR_LIM = 10;
    private static final int NUM_OF_DBS = 3;
    private static final String DEF_CFG_FILE = "application.properties";
    private static final String DEF_DM_FILE = "pcornet_cdm_v3.xlsx";
    private static final String DEF_DBS = "OPS";
    public static final String DEF_DATE_FMT = "MM/dd/yyyy";
    private static final String PGM_NAME = "Test Case Inserter";
    private static final String VERSION = "0.6";
    private static final String BUILD_DATE = "April 12, 2016";
    private int errLim = 10;
    private String testDir;
    private boolean testConns;
    private boolean showHelp;
    private boolean debug;
    private boolean verbose;
    private boolean rollBack;
    private boolean scripts;
    private boolean runTests;
    private boolean template;
    private String tables;
    private String databases = "OPS";
    private String dmFile;
    private String configFile;
    private static TestCaseInserter tci;
    private static DataModel dm;
    private TCIProperties props;
    private ArrayList<String> dataFiles = new ArrayList();
    private static String[][] options;

    static {
        options = new String[][]{{"-cfg", "-configFile <file name>", "path to configuration file, default = application.properties"}, {"-dbg", "-debug", "log more debugging information"}, {"-dbs <arg>", "", "specify order of database processing and/or limit which ones to connect to"}, {"-dmfile", "-dataModelFile <file name>", "path to data model file, default = pcornet_cdm_v3.xlsx"}, {"-e", "-errorLimit <number>", "maximum number of insert errors before quitting, default = 10"}, {"-h", "-help", "print this message"}, {"-rb", "-alwaysRollBack", "always rollback inserts, even if there are no errors"}, {"-scr", "-scripts", "generate database creation sql scripts"}, {"-t", "-testConnections", "check that the database connections are working"}, {"-td", "-testDir <directory name>", "path to directory containing test files to import"}, {"-template", "", "generate input template file"}, {"-v", "-verbose", "print more messages"}};
    }

    private TestCaseInserter() {
    }

    public static TestCaseInserter getInstance() {
        return tci;
    }

    public static DataModel getDataModelInstance() {
        return dm;
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            try {
                tci = new TestCaseInserter();
                if (!tci.runProg(args)) {
                    exitCode = 1;
                }
            }
            catch (TCIException e) {
                tci.userVisibleErrorMsg(e.getMessage());
                exitCode = 2;
                TestCaseInserter.exitProg(exitCode);
            }
            catch (Throwable t) {
                tci.userVisibleErrorMsg(t.getMessage());
                exitCode = 3;
                TestCaseInserter.exitProg(exitCode);
            }
        }
        finally {
            TestCaseInserter.exitProg(exitCode);
        }
    }

    private static void exitProg(int exitCode) {
        logger.trace("Finished Test Case Inserter");
        System.exit(exitCode);
    }

    private void usage(int exitCode) {
        StringBuffer sb = new StringBuffer();
        String[][] arrstring = options;
        int n = arrstring.length;
        int n2 = 0;
        while (n2 < n) {
            String[] opt = arrstring[n2];
            sb.append(String.format("%-10s %-26s %s\n", opt[0], opt[1], opt[2]));
            ++n2;
        }
        System.out.println(sb.toString());
        TestCaseInserter.exitProg(exitCode);
    }

    private boolean runProg(String[] args) {
        this.userVisibleMsg("Test Case Inserter version 0.6 April 12, 2016", true);
        boolean ok = true;
        ok = this.processArgs(args);
        if (this.showHelp) {
            this.usage(0);
        }
        if (ok) {
            ok = this.validateOptions();
        }
        if (ok) {
            ok = this.loadProperties();
        }
        if (!ok) {
            this.usage(1);
        }
        if (this.runTests) {
            this.doTests();
        } else if (this.testConns) {
            ok = Database.testConns();
        } else {
            dm = new DataModel();
            dm.processDataModel();
            if (this.template || this.scripts) {
                if (this.template) {
                    dm.createTemplate();
                }
                if (this.scripts) {
                    dm.genScripts();
                }
            } else {
                TestData td = new TestData();
                ok = td.processTestData(this.dataFiles);
            }
        }
        return ok;
    }

    private boolean loadProperties() {
        boolean ok;
        ok = true;
        try {
            Throwable throwable = null;
            Object var3_5 = null;
            try {
                InputStream is = this.openConfig();
                try {
                    this.props = new TCIProperties();
                    this.props.load(is);
                    String dataModelFile = this.props.getProperty("dataModelFile");
                    if (dataModelFile != null) {
                        this.dmFile = dataModelFile;
                    }
                }
                finally {
                    if (is != null) {
                        is.close();
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
            ok = false;
            this.userVisibleErrorMsg(e.getMessage());
        }
        if (ok) {
            ok = this.validateProperties();
        }
        if (ok && this.configFile == null) {
            logger.trace("Not encrypting application.properties because it is a resource, not a file");
        }
        return ok;
    }

    public void encryptPasswords() {
        this.props.writeEncrypted(this.configFile);
    }

    public void userVisibleErrorMsg(String emsg) {
        logger.error(emsg);
    }

    public void userVisibleMsg(String emsg, boolean alwaysPrint) {
        if (alwaysPrint || this.verbose || this.debug) {
            System.out.println(emsg);
        }
        logger.trace(emsg);
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    private boolean processArgs(String[] args) {
        this.logArgs(args);
        ok = true;
        i = 0;
        while (i < args.length) {
            block63 : {
                block62 : {
                    block57 : {
                        block61 : {
                            block53 : {
                                block54 : {
                                    block60 : {
                                        block50 : {
                                            block52 : {
                                                block58 : {
                                                    block56 : {
                                                        block51 : {
                                                            block55 : {
                                                                block59 : {
                                                                    arg = args[i].toLowerCase();
                                                                    if (!arg.startsWith("-")) break;
                                                                    var5_5 = arg;
                                                                    switch (var5_5.hashCode()) {
                                                                        case -2101941834: {
                                                                            if (!var5_5.equals("-testconnections")) {
                                                                                ** break;
                                                                            }
                                                                            break block50;
                                                                        }
                                                                        case -1905530208: {
                                                                            if (!var5_5.equals("-errorlimit")) {
                                                                                ** break;
                                                                            }
                                                                            break block51;
                                                                        }
                                                                        case -1251398597: {
                                                                            if (!var5_5.equals("-scripts")) {
                                                                                ** break;
                                                                            }
                                                                            break block52;
                                                                        }
                                                                        case -1052618841: {
                                                                            if (!var5_5.equals("-template")) {
                                                                                ** break;
                                                                            }
                                                                            break block53;
                                                                        }
                                                                        case -305397266: {
                                                                            if (!var5_5.equals("-testdir")) {
                                                                                ** break;
                                                                            }
                                                                            break block54;
                                                                        }
                                                                        case -45285390: {
                                                                            if (!var5_5.equals("-dmfile")) {
                                                                                ** break;
                                                                            }
                                                                            break block55;
                                                                        }
                                                                        case 1496: {
                                                                            if (!var5_5.equals("-e")) {
                                                                                ** break;
                                                                            }
                                                                            break block51;
                                                                        }
                                                                        case 1499: {
                                                                            if (!var5_5.equals("-h")) {
                                                                                ** break;
                                                                            }
                                                                            break block56;
                                                                        }
                                                                        case 1511: {
                                                                            if (!var5_5.equals("-t")) {
                                                                                ** break;
                                                                            }
                                                                            break block50;
                                                                        }
                                                                        case 1513: {
                                                                            if (!var5_5.equals("-v")) {
                                                                                ** break;
                                                                            }
                                                                            break block57;
                                                                        }
                                                                        case 46877: {
                                                                            if (!var5_5.equals("-rb")) {
                                                                                ** break;
                                                                            }
                                                                            break block58;
                                                                        }
                                                                        case 46941: {
                                                                            if (!var5_5.equals("-td")) {
                                                                                ** break;
                                                                            }
                                                                            break block54;
                                                                        }
                                                                        case 1438999: {
                                                                            if (!var5_5.equals("-cfg")) {
                                                                                ** break;
                                                                            }
                                                                            ** GOTO lbl101
                                                                        }
                                                                        case 1439836: {
                                                                            if (var5_5.equals("-dbg")) break;
                                                                            ** break;
                                                                        }
                                                                        case 1439848: {
                                                                            if (!var5_5.equals("-dbs")) {
                                                                                ** break;
                                                                            }
                                                                            break block59;
                                                                        }
                                                                        case 1454293: {
                                                                            if (!var5_5.equals("-scr")) {
                                                                                ** break;
                                                                            }
                                                                            break block52;
                                                                        }
                                                                        case 1490784: {
                                                                            if (!var5_5.equals("-alwaysrollback")) {
                                                                                ** break;
                                                                            }
                                                                            break block58;
                                                                        }
                                                                        case 44757230: {
                                                                            if (!var5_5.equals("-help")) {
                                                                                ** break;
                                                                            }
                                                                            break block56;
                                                                        }
                                                                        case 401582290: {
                                                                            if (!var5_5.equals("-tables")) {
                                                                                ** break;
                                                                            }
                                                                            break block60;
                                                                        }
                                                                        case 1383770694: {
                                                                            if (var5_5.equals("-debug")) break;
                                                                            ** break;
                                                                        }
                                                                        case 1398563348: {
                                                                            if (!var5_5.equals("-tests")) {
                                                                                ** break;
                                                                            }
                                                                            break block61;
                                                                        }
                                                                        case 1468161205: {
                                                                            if (!var5_5.equals("-verbose")) {
                                                                                ** break;
                                                                            }
                                                                            break block57;
                                                                        }
                                                                        case 1495250094: {
                                                                            if (!var5_5.equals("-datamodelfile")) {
                                                                                ** break;
                                                                            }
                                                                            break block55;
                                                                        }
                                                                        case 1572661195: {
                                                                            if (!var5_5.equals("-configFile")) break block62;
lbl101: // 2 sources:
                                                                            this.configFile = this.getArg(args, i);
                                                                            ++i;
                                                                            break block63;
                                                                        }
                                                                    }
                                                                    this.debug = true;
                                                                    break block63;
                                                                }
                                                                this.databases = this.getArg(args, i);
                                                                ++i;
                                                                break block63;
                                                            }
                                                            this.dmFile = this.getArg(args, i);
                                                            ++i;
                                                            break block63;
                                                        }
                                                        lim = this.getArg(args, i);
                                                        ++i;
                                                        this.errLim = Integer.parseInt(lim);
                                                        break block63;
                                                    }
                                                    this.showHelp = true;
                                                    break block63;
                                                }
                                                this.rollBack = true;
                                                break block63;
                                            }
                                            this.scripts = true;
                                            break block63;
                                        }
                                        this.testConns = true;
                                        break block63;
                                    }
                                    this.tables = this.getArg(args, i);
                                    ++i;
                                    break block63;
                                }
                                this.testDir = this.getArg(args, i);
                                ++i;
                                break block63;
                            }
                            this.template = true;
                            break block63;
                        }
                        this.runTests = true;
                        break block63;
                    }
                    this.verbose = true;
                    break block63;
                }
                if (args[i].startsWith("-")) {
                    ok = false;
                    this.userVisibleErrorMsg("Unrecognized option:  " + args[i]);
                    return ok;
                }
            }
            ++i;
        }
        while (i < args.length) {
            this.dataFiles.add(args[i]);
            ++i;
        }
        return ok;
    }

    private void logArgs(String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args == null || args.length == 0) {
            sb.append("No arguments");
        } else {
            String[] arrstring = args;
            int n = arrstring.length;
            int n2 = 0;
            while (n2 < n) {
                String a = arrstring[n2];
                sb.append(a);
                sb.append(" ");
                ++n2;
            }
        }
        logger.trace(sb.toString());
    }

    private String getArg(String[] args, int i) {
        String arg = "";
        if (i == args.length - 1) {
            throw new TCIException("Missing value for " + args[i] + " argument");
        }
        arg = args[i + 1];
        if (arg.startsWith("-")) {
            throw new TCIException("Illegal value " + arg + " for " + args[i]);
        }
        return arg;
    }

    private boolean validateOptions() {
        boolean ok = true;
        StringBuilder errs = new StringBuilder();
        if (this.errLim < 0) {
            errs.append("Error limit must be >= 0\n");
        }
        if (this.databases != null) {
            if (this.databases.length() == 0 || this.databases.length() > 3) {
                errs.append("dbs must be a 1 to 3 character string\n");
            } else {
                String[] valid;
                String dbs = this.databases.toLowerCase();
                String[] arrstring = valid = new String[]{"o", "p", "s"};
                int n = arrstring.length;
                int n2 = 0;
                while (n2 < n) {
                    String v = arrstring[n2];
                    dbs = dbs.replaceFirst(v, "");
                    ++n2;
                }
                if (dbs.length() != 0) {
                    errs.append("dbs can only contain the letters O, P, and S and only one of each\n");
                }
            }
        }
        if (this.configFile != null) {
            errs.append(this.verifyFile(this.configFile));
        }
        if (this.dmFile != null) {
            errs.append(this.verifyFile(this.dmFile));
        }
        if (this.testDir != null) {
            errs.append(this.findFilesInTestDir());
        }
        for (String dfn : this.dataFiles) {
            errs.append(this.verifyFile(dfn));
        }
        if (errs.length() > 0) {
            String msgs = errs.toString();
            ok = false;
            this.userVisibleErrorMsg("Argument errors:\n" + msgs);
        }
        return ok;
    }

    private boolean validateProperties() {
        Database[] dbs;
        boolean ok = true;
        String configuredDbs = "";
        StringBuilder errs = new StringBuilder();
        Database[] arrdatabase = dbs = Database.getDbs();
        int n = arrdatabase.length;
        int n2 = 0;
        while (n2 < n) {
            Database db = arrdatabase[n2];
            String dbType = db.getDbType();
            String[] connProps = db.getConnectionProperties();
            int propCount = 0;
            String[] arrstring = connProps;
            int n3 = arrstring.length;
            int n4 = 0;
            while (n4 < n3) {
                String p = arrstring[n4];
                if (p != null && p.length() > 0) {
                    ++propCount;
                }
                ++n4;
            }
            if (propCount == connProps.length) {
                configuredDbs = String.valueOf(configuredDbs) + dbType.substring(0, 1);
            } else if (propCount == 0) {
                this.userVisibleMsg("Removing " + dbType + " from list because it is not configured", false);
            } else if (propCount != connProps.length) {
                errs.append("  " + db.getDbType() + " not fully configured\n");
                break;
            }
            ++n2;
        }
        if (errs.length() == 0) {
            this.databases = configuredDbs;
        } else {
            String msgs = errs.toString();
            ok = false;
            this.userVisibleErrorMsg("Database configuration errors:\n" + msgs);
        }
        return ok;
    }

    private String verifyFile(String fn) {
        String emsg = "";
        if (fn == null || fn.trim().length() == 0) {
            emsg = "missing filename";
        } else {
            File f = new File(fn);
            if (!f.exists()) {
                emsg = "File " + fn + " does not exist";
            } else if (!f.canRead()) {
                emsg = "File " + fn + " is not readable";
            }
        }
        if (emsg.length() > 0) {
            emsg = String.valueOf(emsg) + "\n";
        }
        return emsg;
    }

    public InputStream openConfig() throws FileNotFoundException {
        InputStream is = this.openFileOrResource(this.configFile, "application.properties");
        return is;
    }

    public InputStream openDataModel() throws FileNotFoundException {
        InputStream is = this.openFileOrResource(this.dmFile, "pcornet_cdm_v3.xlsx");
        return is;
    }

    private InputStream openFileOrResource(String fn, String rn) throws FileNotFoundException {
        InputStream is = null;
        if (fn != null) {
            logger.trace("Opening file " + fn);
            File f = new File(fn);
            if (f.exists()) {
                is = new FileInputStream(f);
            }
        } else {
            logger.trace("Opening resource " + rn);
            is = this.getClass().getResourceAsStream("/" + rn);
        }
        return is;
    }

    private String findFilesInTestDir() {
        String emsg = "";
        emsg = this.verifyFile(this.testDir);
        if (emsg.length() == 0) {
            File testDirFile = new File(this.testDir);
            if (testDirFile.isDirectory()) {
                File[] files = testDirFile.listFiles(new FilenameFilter(){

                    @Override
                    public boolean accept(File dir, String name) {
                        boolean ok = name.endsWith(".xlsx") && !name.startsWith("~");
                        return ok;
                    }
                });
                String canonDirPath = null;
                try {
                    canonDirPath = testDirFile.getCanonicalPath();
                }
                catch (IOException ioe) {
                    throw new TCIException("Error accessing " + this.testDir + " " + ioe.getMessage());
                }
                if (files == null) {
                    emsg = "Directory " + canonDirPath + " is not readable";
                } else if (files.length == 0) {
                    emsg = "Directory " + canonDirPath + " contains no xlsx files";
                } else {
                    File[] arrfile = files;
                    int n = arrfile.length;
                    int n2 = 0;
                    while (n2 < n) {
                        File dirFile = arrfile[n2];
                        String name = String.valueOf(canonDirPath) + "/" + dirFile.getName();
                        this.dataFiles.add(name);
                        ++n2;
                    }
                }
            } else {
                emsg = String.valueOf(testDirFile.getName()) + " is not a directory";
            }
        }
        if (emsg.length() > 0) {
            emsg = String.valueOf(emsg) + "\n";
        }
        return emsg;
    }

    private void doTests() {
        double d1 = 0.6;
        double d2 = 0.6000000000000001;
        boolean b = d1 == d2;
        this.userVisibleMsg("Equal:  " + b + " " + d1 + " " + d2, true);
        float f1 = 0.6f;
        float f2 = 0.6f;
        b = f1 == f2;
        this.userVisibleErrorMsg("Equal:  " + b + " " + f1 + " " + f2);
    }

    public int getErrorLimit() {
        return this.errLim;
    }

    public String getConfigFile() {
        return this.configFile;
    }

    public String getTables() {
        return this.tables;
    }

    public String getDatabases() {
        return this.databases;
    }

    public String getDataModelFile() {
        return this.dmFile;
    }

    public boolean getTestConns() {
        return this.testConns;
    }

    public boolean getDebug() {
        return this.debug;
    }

    public boolean getVerbose() {
        return this.verbose;
    }

    public boolean getRollBack() {
        return this.rollBack;
    }

    public boolean getScripts() {
        return this.scripts;
    }

    public boolean getRunTests() {
        return this.runTests;
    }

    public ArrayList<String> getDataFiles() {
        return this.dataFiles;
    }

    public Properties getProperties() {
        return this.props;
    }

}

