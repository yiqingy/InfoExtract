package com.sf.mlp.ie.test;

/**
 * Created by Joms on 5/4/2016.
 */
import com.sf.mlp.ie.InformationExtraction;

import java.sql.*;
import java.util.ArrayList;

public class ConnectTest
{
    public static void main (String[] args)
    {
        Connection conn = null;
        ResultSet rs = null;
        Statement s = null;

        try
        {
            String userName = "*";
            //String userName = "*";
            String password = "*";
            //String password = "*";
            String url = "jdbc:mysql://sf-prod-tier4-01.obisolution.com:1194";
            //String url = "jdbc:mysql://sf-dev-amc-01.obisolution.com:1194";

            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);

            System.out.println ("Database connection established");
            s = conn.createStatement ();
            s.executeQuery ("select * from smartfin.TestRun where run_id = 20160511000111 and score<0.65 order by score");
            //s.executeQuery ("select * from smartfin.TestRun");
            rs = s.getResultSet ();


            int count = 0, nullCount=0;
            InformationExtraction ie;
            ie = new InformationExtraction("THE MEATHOUSE", "", "", "", "");
            ie.extract_info();
            final long startTime = System.currentTimeMillis();
            while (rs.next ())
            {
                count++;
                String bankRecord = rs.getString("tx_bank_record");
                String resolvedName = rs.getString("tx_resolved_name");
                String address = rs.getString("tx_address");
                String city = rs.getString("tx_city");
                String state = rs.getString("tx_state");
                if (bankRecord==null || bankRecord.trim().equals("")) {
                    if (resolvedName == null || resolvedName.trim().equals(""))
                        bankRecord = "";
                    else
                        bankRecord = resolvedName;
                }
                if (bankRecord.equals("")) {
                    nullCount ++;
                    continue;
                }
                ie = new InformationExtraction(bankRecord, resolvedName, address, city, state);
                //System.out.println(bankRecord+","+resolvedName);
                //ArrayList<String> res = ie.extract_info();

            }

            final long endTime = System.currentTimeMillis();
            System.out.println("Total execution time: " + (endTime - startTime) );
            System.out.println("Average execution time: " + (double)(endTime - startTime)/(double)count );
            //rs.close ();
            //s.close ();
            System.out.println (count + " rows were retrieved");
            System.out.println (nullCount + " rows were null");
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
                    System.out.println ("Database connection terminated");
                }
                catch (Exception e) { /* ignore close errors */ }
            }

        }
    }
}
