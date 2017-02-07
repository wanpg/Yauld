package com.wanpg.yauld.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class ConnDB {

	private static String URL="jdbc:sqlite:";
	
	public static String DATABASE_NAME = "HotFixServer/src/main/resources/db/my.db";



	public static Connection getConn(){
		Connection ct = null;
		try {
			Class.forName("org.sqlite.JDBC");
			ct=DriverManager.getConnection(URL + DATABASE_NAME);
			Statement statement1 = ct.createStatement();
			statement1.setQueryTimeout(30);  // set timeout to 30 sec.
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ct;
	}
}
