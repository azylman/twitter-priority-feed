package com.zylman.twitter;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TwitterDatabase {	
	private static Database database;
	
	//database properties
	private static final String STATUS_TABLE_NAME = "statuses";
	private static final String WORDCOUNT_TABLE_NAME = "wordcounts";
	private static final String INTERACTORS_TABLE_NAME = "interactors";
	
	private static final int STATUS_LENGTH = 400;
	
	@SuppressWarnings("serial")
	private static final Map<String, Map<String, String>> tableFields = new HashMap<String, Map<String, String>>() {{
		put(STATUS_TABLE_NAME, new LinkedHashMap<String, String>() {{
			put("id", "BIGINT");
			put("retweet", "BOOLEAN");
			put("text", "VARCHAR(" + STATUS_LENGTH + ")");
		}});
		
		put(WORDCOUNT_TABLE_NAME, new LinkedHashMap<String, String>() {{
			put("word", "VARCHAR(" + STATUS_LENGTH + ")");
			put("count", "INT");
		}});

		put(INTERACTORS_TABLE_NAME, new LinkedHashMap<String, String>() {{
			put("user", "VARCHAR(" + STATUS_LENGTH + ")");
			put("count", "INT");
		}});
	}};
	
	@SuppressWarnings("serial")
	public Map<String, Table> tables = new HashMap<String, Table>() {{
		put(STATUS_TABLE_NAME, new Table() {
			@Override public void create(Statement s) throws SQLException {
				s.execute(getCreationStatement(STATUS_TABLE_NAME));
				s.execute("ALTER TABLE " + STATUS_TABLE_NAME + " ADD PRIMARY KEY (id)");
				s.close();
				System.out.println("TwitterDatabase.create(): successfully created status table.");
			}

			@Override public String getName() {
				return STATUS_TABLE_NAME;
			}
		});
		
		put(WORDCOUNT_TABLE_NAME, new Table() {
			@Override public void create(Statement s) throws SQLException {
				s.execute(getCreationStatement(WORDCOUNT_TABLE_NAME));
				s.execute("ALTER TABLE " + WORDCOUNT_TABLE_NAME + " ADD INDEX (word)");
				s.close();
				System.out.println("TwitterDatabase.create(): successfully created wordcount table.");
			}

			@Override public String getName() {
				return WORDCOUNT_TABLE_NAME;
			}
		});
		
		put(INTERACTORS_TABLE_NAME, new Table() {
			@Override public void create(Statement s) throws SQLException {
				s.execute(getCreationStatement(INTERACTORS_TABLE_NAME));
				s.execute("ALTER TABLE " + INTERACTORS_TABLE_NAME + " ADD INDEX (user)");
				s.close();
				System.out.println("TwitterDatabase.create(): successfully created interactors table.");
			}

			@Override public String getName() {
				return INTERACTORS_TABLE_NAME;
			}
		});
		
	}};
	
	public TwitterDatabase(String host, String databaseName, String user, String pass) throws TwitterDatabaseException {
		try {	
			database = new Database(host, databaseName, user, pass, tables);
		} catch (Exception ex) {
			throw new TwitterDatabaseException(ex,"Could not initialize database.");
		}
	}
	
	private String getCreationStatement(String tableName) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE ");
		result.append(tableName);
		result.append("(");
		result.append(convertFieldMapToString(tableFields.get(tableName)));
		result.append(")");
		return result.toString();
	}
	
	private String convertFieldMapToString(Map<String, String> tableFields) {
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String> field : tableFields.entrySet()) {
			result.append(field.getKey());
			result.append(" ");
			result.append(field.getValue());
			result.append(", ");
		}
		
		return result.substring(0, result.length() - 2);
	}
}