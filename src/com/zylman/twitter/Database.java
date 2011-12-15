package com.zylman.twitter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Database {
	//database session properties
	private String host;
	private String databaseName;
	private String userName;
	private String password;
	private Connection connection;
	
	private Map<String, Boolean> tableStatus = new HashMap<String, Boolean>();
	Map<String, Table> tables;
	
	public Database(String host, String databaseName, String user, String pass, Map<String, Table> tables) throws DatabaseException {
		this.tables = tables;
		try {	
			establishConnectionToDatabase(host, databaseName, user, pass);

			for (Map.Entry<String, Table> table : tables.entrySet()) {
				tableStatus.put(table.getKey(), false);
			}
			
			assessTableStatus();
			for (Map.Entry<String, Boolean> table : tableStatus.entrySet()) {
				if (!table.getValue()) {
					tables.get(table.getKey()).create(getStatement(false));
					tableStatus.put(table.getKey(), true);
				}
			}
		} catch (Exception ex) {
			throw new DatabaseException(ex,"Could not initialize database.");
		}
	}
			
	private void establishConnectionToDatabase(String host, String databaseName, String user, String pass) throws DatabaseException {
		this.host = host;
		this.databaseName = databaseName;
		this.userName = user;
		this.password = pass;
		connection = getNewConnection();	
	}
	
	private Connection getNewConnection() throws DatabaseException {
		try {
			String url = "jdbc:mysql://" + host + ":3306/"+ databaseName;
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			Properties props = new Properties();
			props.setProperty("user", userName);
			props.setProperty("password", password);
			props.setProperty("characterEncoding", "utf-8");
			props.setProperty("connectionCollation", "utf8_unicode_ci");
			return connection = DriverManager.getConnection(url, props);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Check to see which tables have been created and set status indicators accordingly
	 */
	private void assessTableStatus() throws DatabaseException {
		System.out.println("Database.assessTableStatus(): Checking tables...");
		
		try {
			Statement s = this.getStatement(false);
			ResultSet r = s.executeQuery("SHOW TABLES");
			List<String> foundTables = new ArrayList<String>();

			while(r.next()) {
				String name = r.getString(1);
				foundTables.add(name);
				
				if (!tableStatus.containsKey(name)) {
					System.out.println("\tWARNING: unrecognized table found: " + name);
				} else {
					tableStatus.put(name, true);
					System.out.println("\tFound table '" + name + "'.");
				}
			}
			
			for (String table : tables.keySet()) {
				if (!tableStatus.get(table)) System.out.println("\tWARNING: Could not find table '" + table + "' in database.");
			}
			
			s.close();
		} catch (SQLException e) {
			throw new DatabaseException(e,"Database.assessTableStatus():  SQL error attempting to check tables");
		}
	}
	
	private Statement getStatement(boolean editable) throws DatabaseException
	{
		try 
		{
			if (!editable){
				return connection.createStatement();
			}else{
				return connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			}
			
		} catch (SQLException e) {
			throw new DatabaseException(e, "Could not create statement");
		}
	}
}
