package com.sf.mlp.ie.test;

/**
 * Created by Joms on 5/4/2016.
 */
import com.sf.mlp.ie.InformationExtraction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Connect
{
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
            //String userName = "*";
            String password = "*";
            //String password = "*";
            String url = "jdbc:mysql://sf-prod-tier4-01.obisolution.com:1194";
            //String url = "jdbc:mysql://sf-dev-amc-01.obisolution.com:1194";

            String userNameInsert = "root";
            String passwordInsert = "ding1bat";
            String urlInsert = "jdbc:mysql://sf-an1.smartfinancellc.biz:1194/smartfin?rewriteBatchedStatements=true";

            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            connInsert = DriverManager.getConnection (urlInsert, userNameInsert, passwordInsert);

            System.out.println ("Database connection established");
            s = conn.createStatement ();
            s.executeQuery ("select * from smartfin.TestRun where run_id = 20160511000111 and score<0.65 order by score desc");
            rs = s.getResultSet ();
            System.out.println("Got result set!");

            ResultSetMetaData meta = rs.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++)
                columns.add(meta.getColumnName(i));

            insert  = connInsert.prepareStatement("INSERT INTO InfoExtractTx (type, bank_record, resolved_name, category_id, category_name, address, city, state, postal_code, tx_type, account_type, plaid_resolved_name, old_score, old_tx_hash) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");



            int count = 0, nullCount=0;
            InformationExtraction ie;
            ie = new InformationExtraction("THE MEATHOUSE", "", "", "", "");
            ie.extract_info();
            final long startTime = System.currentTimeMillis();
            int checkCount = 0;
            while (rs.next())
            {
                //Hack for unicode error:
                /*
                java.sql.BatchUpdateException: Incorrect string value: '\xEF\xBF\xBD BO...' for column 'bank_record' at row 52160
	            at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	            at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	            at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	            at java.lang.reflect.Constructor.newInstance(Constructor.java:408)
                 */
                //if (checkCount == 52159 || checkCount == 52160 || checkCount == 52161) rs.next();
                //System.out.println(count);


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
                if (bankRecord.indexOf("TST* MILK STREET CAF") > 0) {
                    continue;
                }

                ie = new InformationExtraction(bankRecord, resolvedName, address, city, state);
                //System.out.println(bankRecord+","+resolvedName);
                ArrayList<String> res = new ArrayList<String>();
                res.add( ie.extract_info().getName());
                res.add( ie.extract_info().getCity());
                res.add( ie.extract_info().getState());
                insert.setString(1, "plaid");
                //insert.setString(2, rs.getString("tx_id"));
                insert.setString(2, rs.getString("tx_bank_record"));
                insert.setString(3, res.get(0));
                insert.setString(4, rs.getString("tx_category"));
                insert.setString(5, "");
                insert.setString(6, rs.getString("tx_address"));
                insert.setString(7, rs.getString("tx_city"));
                insert.setString(8, rs.getString("tx_state"));
                insert.setString(9, rs.getString("tx_postal_code"));
                insert.setString(10, rs.getString("tx_type"));
                insert.setString(11, rs.getString("tx_account_type"));
                insert.setString(12, rs.getString("tx_resolved_name"));
                insert.setDouble(13, rs.getDouble("score"));
                insert.setLong(14, rs.getLong("tx_hash"));

                String plaid_city = rs.getString("tx_city");
                String plaid_state = rs.getString("tx_state");
                if (plaid_city == null || plaid_city.trim().equals(""))
                    insert.setString(7, res.get(1));

                if (plaid_state == null || plaid_state.trim().equals(""))
                    insert.setString(8, res.get(2));

                insert.addBatch();
                count++;
                checkCount++;
            }

            final long endTime = System.currentTimeMillis();
            System.out.println("Total execution time: " + (endTime - startTime) );
            System.out.println("Average execution time: " + (double)(endTime - startTime)/(double)count );
            //rs.close ();
            //s.close ();
            System.out.println (count + " rows were retrieved");
            System.out.println (nullCount + " rows were null");
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
