package com.zylman.twitter;

import java.sql.Statement;

public interface Table {
	void create(Statement s);
	String getName();
}
