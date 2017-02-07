package com.wanpg.yauld.server.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class Dao {

	protected Connection ct=null;
	protected Statement sm=null;
	protected ResultSet rs=null;

	protected Connection getConnection() {
		return ConnDB.getConn();
	}

	protected void init(){

	}

	protected void closeConnection() {
		try {
			if(rs!=null){
				rs.close();
				rs=null;
			}
			if(sm!=null){
				sm.close();
				sm=null;
			}
			if(ct!=null){
				ct.close();
				ct=null;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
