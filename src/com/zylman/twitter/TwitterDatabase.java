package com.zylman.twitter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import twitter4j.Status;

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
				System.out.println("TwitterDatabase.create(): successfully created status table.");
			}

			@Override public String getName() {
				return STATUS_TABLE_NAME;
			}
		});
		
		put(WORDCOUNT_TABLE_NAME, new Table() {
			@Override public void create(Statement s) throws SQLException {
				s.execute(getCreationStatement(WORDCOUNT_TABLE_NAME));
				s.execute("ALTER TABLE " + WORDCOUNT_TABLE_NAME + " ADD PRIMARY KEY (word)");
				System.out.println("TwitterDatabase.create(): successfully created wordcount table.");
			}

			@Override public String getName() {
				return WORDCOUNT_TABLE_NAME;
			}
		});
		
		put(INTERACTORS_TABLE_NAME, new Table() {
			@Override public void create(Statement s) throws SQLException {
				s.execute(getCreationStatement(INTERACTORS_TABLE_NAME));
				s.execute("ALTER TABLE " + INTERACTORS_TABLE_NAME + " ADD PRIMARY KEY (user)");
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
	
	public void addStatuses(List<Status> statuses) throws TwitterDatabaseException {
		for (Status status : statuses) {
			addStatus(status);
		}
	}
	
	public void addStatus(Status status) throws TwitterDatabaseException {
		try {
			ResultSet r = database.executeQuery("SELECT COUNT(*) FROM " + STATUS_TABLE_NAME + " WHERE id=" + status.getId());
			r.first();
			int count = r.getInt(1);
			if (count == 0) {
				database.insert(STATUS_TABLE_NAME,
						Long.toString(status.getId()),
						status.isRetweet() ? "1" : "0",
						status.getText());
				
				List<String> words = Arrays.asList(status.getText().split(" "));
				for (String word : words) {
					if (word.indexOf("@") != 0)
						database.execute(getAddOrIncrementQuery(WORDCOUNT_TABLE_NAME, word));
				}
				
				Set<String> mentions = getMentions(status);
				for (String mention : mentions) {
					database.execute(getAddOrIncrementQuery(INTERACTORS_TABLE_NAME, mention));
				}
			}
		} catch (SQLException e) {
			System.out.println("SQLException adding status" + e.getMessage());
			throw new TwitterDatabaseException(e, "Failed to add status");
		} catch (Exception e) {
			System.out.println("Exception adding status" + e.getMessage());
			throw new TwitterDatabaseException(e, "Failed to add status");
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
	
	private String getAddOrIncrementQuery(String table, String value) {
		String encodedValue;
		try {
			encodedValue = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			encodedValue = value;
		}
		return "INSERT INTO " + table + " VALUES ('" + encodedValue + "',1)" + "ON DUPLICATE KEY UPDATE count=count+1";
	}
	
	private static Set<String> getMentions(Status status) {
		Set<String> result = new HashSet<String>();
		result.add(filterMention(status.getInReplyToScreenName()));
		List<String> words = Arrays.asList(status.getText().split(" "));
		for (String word : words) {
			if (word.indexOf('@') == 0) {
				result.add(filterMention(word));
			}
		}
		result.remove(null);
		return result;
	}
	
	/**
	 * Remove any extraneous characters that aren't part of the user name (leading @, trailing :).
	 */
	private static String filterMention(String string) {
		if (string == null) {
			return null;
		}
		return string.replaceAll("[@:]", "");
	}
}