package com.zylman.twitter;

import java.sql.SQLException;
import java.sql.Statement;

public interface Table {
	void create(Statement s) throws SQLException;
	String getName();
}
