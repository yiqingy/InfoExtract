package com.sf.mlp.ie.test;

import java.sql.*;

/**
 * Created by Joms on 5/10/2016.
 */
public class TestResultCopier {
    public static void main (String[] args)
    {
        Connection conn = null;
        Connection connInsert = null;
        ResultSet rs = null;
        Statement s = null;
        PreparedStatement insert = null;

        try
        {
            String userName = "*";
            String password = "*";
            String url = "jdbc:mysql://sf-prod-tier4-01.obisolution.com:1194";

            String userNameInsert = "*";
            String passwordInsert = "*";
            String urlInsert = "jdbc:mysql://sf-an1.smartfinancellc.biz:1194/smartfin?rewriteBatchedStatements=true";

            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            connInsert = DriverManager.getConnection (urlInsert, userNameInsert, passwordInsert);

            System.out.println ("Database connection established");
            s = conn.createStatement ();
            s.executeQuery ("select * from smartfin.TestResult");
            rs = s.getResultSet ();

            insert  = connInsert.prepareStatement("INSERT INTO TestResult (st_hash, tx_hash, result) VALUES (?,?,?)");

            while (rs.next ())
            {
                insert.setLong(1, rs.getLong("st_hash"));
                insert.setLong(2, rs.getLong("tx_hash"));
                insert.setString(3, rs.getString("result"));
                insert.addBatch();
            }
            insert.executeBatch();
        }
        catch (Exception e)
        {
            System.err.println ("Cannot connect to database server");
            e.printStackTrace();
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    if (rs!=null) rs.close();
                    if (s!=null) s.close();
                    conn.close ();
                    System.out.println("Closed everything");
                    System.out.println ("Database connection terminated");
                }
                catch (Exception e) { /* ignore close errors */ }
            }
            if (connInsert != null)
            {
                try
                {
                    if (insert!=null) insert.close();
                    connInsert.close ();
                    System.out.println("Closed everything");
                    System.out.println ("Database connection terminated");
                }
                catch (Exception e) { /* ignore close errors */ }
            }
        }
    }
}
