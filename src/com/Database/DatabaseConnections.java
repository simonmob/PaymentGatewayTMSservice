package com.Database;

import com.MainFiles.ClassImportantValues;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import javax.swing.JOptionPane;

public class DatabaseConnections {

    public Connection con = null;
    public ResultSet rst = null;
    Statement st = null;
    ClassImportantValues cl = new ClassImportantValues();

    public ArrayList ExecuteQueryStringValue(String Server, String sql, String pass, String user, String db, String rslt, ArrayList index) {
        ArrayList value = new ArrayList();
        try {
            String url = "jdbc:oracle:thin:@" + Server + ":1521:" + db;
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(url, user, pass);

            if (!con.isClosed()) {
                st = con.createStatement();
                rst = st.executeQuery(sql);
                while (rst.next()) {
                    for (int i = 0; i < index.size(); i++) {
                        rslt = rst.getString(index.get(i).toString());
                        value.add(i, rslt);
                    }
                }
            }
        } catch (Exception e) {
            String ex = Thread.currentThread().getStackTrace()[2].getMethodName();
            cl.logs(ex, sql + "\n" + e.getMessage());
        }
        close();
        return value;
    }

    public String ExecuteQueryStringValue(String Server, String sql, String pass, String user, String db, String rslt, String index) {
        try {
            String url = "jdbc:oracle:thin:@" + Server + ":1521:" + db; //The database connection is here oracle
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(url, user, pass);
            if (!con.isClosed()) {
                st = con.createStatement();
                rst = st.executeQuery(sql);
                while (rst.next()) {
                    rslt = rst.getString(index);
                }
            }
        } catch (Exception e) {
            String ex = Thread.currentThread().getStackTrace()[2].getMethodName();
            cl.logs(ex, e.getMessage());
        }
        close();
        return rslt;
    }

    public boolean ExecuteUpdate(String Server, String sql, String pass, String user, String db) {
        boolean result = false;
        try {
            String url = "jdbc:oracle:thin:@" + Server + ":1521:" + db; //The database connection is here oracle
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(url, user, pass);   //Initiate a connecteeion to the database
            if (!con.isClosed()) {
                st = con.createStatement();
                if (st.executeUpdate(sql) >= 1) {
                    result = true;
                }
            }
        } catch (Exception e) {
            String ex = Thread.currentThread().getStackTrace()[2].getMethodName();
            cl.logs(ex, e.getMessage());
            result = false;
        }
        close();
        return result;
    }

    public ResultSet ExecuteQueryReturnString(String Server, String sql, String pass, String user, String db) {
        try {
            String url = "jdbc:oracle:thin:@" + Server + ":1521:" + db;
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(url, user, pass);
            if (!con.isClosed()) {
                st = con.createStatement();  //instaniate an object that is used to eecute sql statements
                rst = st.executeQuery(sql);
            }
        } catch (Exception e) {
            String ex = Thread.currentThread().getStackTrace()[2].getMethodName();
            cl.logs(ex, e.getMessage());
        }
        return rst;
    }

    public String Updatestring(String table, ArrayList whereval, ArrayList whereColumns, ArrayList Columnstoupdate) {
        String sql = "";
        PreparedStatement updateQuery = null;
        try {
            String where = whereval.toString().replace("]", "");
            where = where.replace("[", "");

            //Columns to update      
            String wherecols = whereColumns.toString().replace("]", "");
            wherecols = wherecols.replace("[", "");
            wherecols = wherecols.replace(", ", " = '%s' and ");
            //where clause 
            String Cols = Columnstoupdate.toString().replace("]", "");
            Cols = Cols.replace("[", "");
            Cols = Cols.replace(", ", " = '%s',");

            if (whereval == null | whereval.equals("")) {
                sql = "UPDATE " + table + " SET " + Cols + " = '%s'" + "";
                sql = String.format(sql, where);
            } else {
                sql = "UPDATE " + table + " SET " + Cols + " = '%s' where " + wherecols + "= '%s'";    //Fixed Issue)
                String values[] = where.split(",");
                //We assume a maximum of five columns updated and a max of columns on where clause(good staff baiks)
                if (values.length == 1) {
                    sql = String.format(sql, values[0].trim());
                }
                if (values.length == 2) {
                    sql = String.format(sql, values[0].trim(), values[1].trim());
                }
                if (values.length == 3) {
                    sql = String.format(sql, values[0].trim(), values[1].trim(), values[2].trim());
                }
                if (values.length == 4) {
                    sql = String.format(sql, values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim());
                }
                if (values.length == 5) {
                    sql = String.format(sql, values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(), values[4].trim());
                }
                if (values.length == 6) {
                    sql = String.format(sql, values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(), values[4].trim(), values[5].trim());
                }
                if (values.length == 7) {
                    sql = String.format(sql, values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(), values[4].trim(), values[5].trim(), values[6].trim());
                }
                if (values.length == 8) {
                    sql = String.format(sql, values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(), values[4].trim(), values[5].trim(), values[6].trim(), values[7].trim());
                }
                if (values.length == 9) {
                    sql = String.format(sql, values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(), values[4].trim(), values[5].trim(), values[6].trim(), values[7].trim(), values[8].trim());
                }
                if (values.length == 10) {
                    sql = String.format(sql, values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(), values[4].trim(), values[5].trim(), values[6].trim(), values[7].trim(), values[8].trim(), values[9].trim());
                }
            }
        } catch (Exception e) {
            String ex = Thread.currentThread().getStackTrace()[2].getMethodName();
            cl.logs(ex, e.getMessage());
        }

        return sql;
    }

    public boolean ExecuteInsert(String Server, String pass, String user, String db, String table, ArrayList insertvalues, ArrayList Columns) {
        String sql = "";
        boolean success = false;
        try {
            String url = "jdbc:oracle:thin:@" + Server + ":1521:" + db; //The database connection is here oracle
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(url, user, pass);   //Initiate a connecteeion to the database
            insertvalues.size();
            int i = 0;
            Object Vals = null;
            List vals = null;

            String Values = insertvalues.toString().replace("]", "'");
            Values = Values.replace(",", "','");
            Values = Values.replace("[", "'");
            Values = Values.replace("' ", "'");

            String Cols = Columns.toString().replace("]", "");
            Cols = Cols.replace("[", "");
            Cols = Cols.replace(", ", ",");

            if (Columns.equals("") | Columns == null) {
                sql = "insert into " + table + " values(" + Values + ")";
            } else {
                sql = "insert into  " + table + " (" + Cols + ") values(" + Values + ")";
            }
            if (!con.isClosed()) {
                st = con.createStatement();  //instaniate an object that is used to execute sql statements
                if ((st.executeUpdate(sql)) >= 1) {
                    success = true;
                }
            }
        } catch (Exception e) {
            String ex = Thread.currentThread().getStackTrace()[2].getMethodName();
            cl.logs(ex, e.getMessage());
        }
        close();
        return success;
    }

    public String DeleteString(String Server, String pass, String user, String db, String table, ArrayList wherevalues, ArrayList Columns) {  //(Cool Done)
        String sql = "";
        boolean success = false;
        try {
            String url = "jdbc:oracle:thin:@" + Server + ":1521:" + db; //The database connection is here oracle
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(url, user, pass);  //Initiate a connection to the database

            String Values = wherevalues.toString().replace("[", "'");
            Values = Values.replaceAll(",", "','");
            Values = Values.replaceAll("]", "'");
            Values = Values.replace("' ", "\"");
            Values = Values.replace("'", "\"");

            String varFormat = "";
            if (Columns.equals("") || Columns == null || Columns.isEmpty()) {          //null and "" does not work thus use isEmpty for arraystring
                sql = "delete from " + table;
            }//This is done
            else {
                //Columns to delete 
                String Cols = Columns.toString().replace("]", "=%s");
                Cols = Cols.replace("[", "");
                Cols = Cols.replace(", ", "=%s and ");

                varFormat = "delete from " + table + " where " + Cols;
                String formated = Values;
                String formatted1 = Values;

                if (formated.contains(",")) {
                    String[] stringsplit = formated.split(",");
                    if (stringsplit.length == 2) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1]);
                    }
                    if (stringsplit.length == 3) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1], stringsplit[2]);
                    }
                    if (stringsplit.length == 4) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1], stringsplit[2], stringsplit[3]);
                    }
                    if (stringsplit.length == 5) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1], stringsplit[2], stringsplit[3], stringsplit[4]);
                    }
                    if (stringsplit.length == 6) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1], stringsplit[2], stringsplit[3], stringsplit[4], stringsplit[5]);
                    }
                    if (stringsplit.length == 7) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1], stringsplit[2], stringsplit[3], stringsplit[4], stringsplit[5], stringsplit[6]);
                    }
                    if (stringsplit.length == 8) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1], stringsplit[2], stringsplit[3], stringsplit[4], stringsplit[5], stringsplit[6], stringsplit[7]);
                    }
                    if (stringsplit.length == 9) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1], stringsplit[2], stringsplit[3], stringsplit[4], stringsplit[5], stringsplit[6], stringsplit[7], stringsplit[8]);
                    }
                    if (stringsplit.length == 10) {
                        sql = String.format(varFormat, stringsplit[0], stringsplit[1], stringsplit[2], stringsplit[3], stringsplit[4], stringsplit[5], stringsplit[6], stringsplit[7], stringsplit[8], stringsplit[9]);
                    }
                } else {
                    String[] stringsplit = formated.split(",");
                    sql = String.format(varFormat, stringsplit[0]);
                }
            }

        } catch (Exception e) {
            String ex = Thread.currentThread().getStackTrace()[2].getMethodName();
            cl.logs(ex, e.getMessage());
        }
        sql = sql.replace("\"", "'");                                              //Remove "" enclosing string at where caluse eg. where name="paul" to where name='paul'
        close();
        return sql;
    }
   
    public Connection getDBConnection(String Server, String db, String user, String pass) {

        Connection dbConnection = null;
        try {
            String url = "jdbc:oracle:thin:@" + Server + ":1521:" + db; //The database connection is here oracle
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            con = DriverManager.getConnection(url, user, pass);   //Initiate a connecteeion to the database
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return con;
    }

    public void close() {
        try {
            //Close JDBC objects as soon as possible
            if (st != null) {
                st.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            cl.logs("error", e.getMessage());
        }
    }
}
