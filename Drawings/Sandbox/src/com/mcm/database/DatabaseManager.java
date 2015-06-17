package com.mcm.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class DatabaseManager {
	private final static Logger log = LogManager.getLogger(DatabaseManager.class);
	private BoneCP pool = null;

	private static DatabaseManager dm = null;

	private DatabaseManager(){
		try {
			Class.forName("com.mysql.jdbc.Driver");

			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl("jdbc:mysql://localhost/mcm2");
			config.setUsername("mcm2"); 
			config.setPassword("mcm@2015");
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(10);
			config.setPartitionCount(1);
			pool = new BoneCP(config);
		} catch (ClassNotFoundException e) {
			log.error("Error getting driver", e);
		} catch (SQLException e) {
			log.error("Error initializing pool", e);
		}
	}

	public static DatabaseManager getInstance(){
		if(dm == null){
			dm = new DatabaseManager();
		}
		return dm;
	}

	public void shutdown(){
		pool.shutdown();
	}

	public Connection openConnection(){
		try {
			return pool.getConnection();
		} catch (SQLException e) {
			log.error("Error getting conn", e);
		}
		return null;
	}

	public void closeConnection(Connection conn){
		try {
			conn.close();
		} catch (SQLException e) {
			log.error("Error closing conn", e);
		}
	}

	public void closeStatement(PreparedStatement pst){
		try {
			pst.close();
		} catch (SQLException e) {
			log.error("Error closing statement", e);
		}
	}
	
	public void closeStatement(Statement st){
		try {
			st.close();
		} catch (SQLException e) {
			log.error("Error closing statement", e);
		}
	}
	
	public void closeResultSet(ResultSet rs){
		try {
			rs.close();
		} catch (SQLException e) {
			log.error("Error closing resultset", e);
		}		
	}
}
