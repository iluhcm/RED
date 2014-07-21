package com.dw.publicaffairs;

import java.io.*;


public class Utilities {

    protected FileInputStream inputStream;
    protected File file = null;
    protected String line;
    private StringBuffer s2 = new StringBuffer();
    private StringBuffer s3 = new StringBuffer();
    private StringBuffer dnaTable = new StringBuffer();
    private StringBuffer dnaValue = new StringBuffer();

    public StringBuffer getS2() {
        return s2;
    }

    public void setS2(StringBuffer s2) {
        this.s2 = s2;
    }

    public StringBuffer getdnaTable() {
        return dnaTable;
    }

    public void setdnaTable(StringBuffer dnaTable) {
        this.dnaTable = dnaTable;
    }

    public StringBuffer getdnaValue() {
        return dnaValue;
    }

    public void setdnaValue(StringBuffer dnaValue) {
        this.dnaValue = dnaValue;
    }


    public StringBuffer getS3() {
        return s3;
    }

    public void setS3(StringBuffer s3) {
        this.s3 = s3;
    }


    protected String[] col = new String[40];
    protected int count_t = 1;

    private static Utilities instance = null;

    private Utilities() {

    }

    public static Utilities getInstance() {
        if (instance == null) {
            instance = new Utilities();
        }
        return instance;
    }

    public boolean createCalTable(String dir) {
        boolean createSuccess = false;
        try {
            inputStream = new FileInputStream(dir);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        BufferedReader rin = new BufferedReader(new InputStreamReader(
                inputStream));
        try {
            while ((line = rin.readLine()) != null) {
                String[] section = line.split("\\t");
                if (line.startsWith("##"))
                    continue;
                if (line.startsWith("#")) {
                    s2.append(section[1] + " " + "bigint");
                    s2.append("," + section[2] + " " + "varchar(30)");
                    s2.append("," + section[3] + " " + "varchar(3)");
                    s2.append("," + section[4] + " " + "varchar(5)");
                    s2.append("," + section[5] + " " + "float(8,2)");
                    s2.append("," + section[6] + " " + "text");
                    s2.append("," + section[7] + " " + "text");
                    s3.append("chrome");
                    for (int i = 1; i < 8; i++)
                        s3.append("," + section[i]);
                    continue;
                }
                for (int i = 0; i < section.length; i++) {
                    col[i] = section[i];
                }
                if (count_t > 0) {
                    for (int i = 0; i < col[8].split(":").length; i++) {
                        s2.append("," + col[8].split(":")[i] + " " + "text");
                        s3.append("," + col[8].split(":")[i]);
                    }
                }
                break;
            }
            createSuccess = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            createSuccess = false;
            e.printStackTrace();
        }
        return createSuccess;
    }

}
