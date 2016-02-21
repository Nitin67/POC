package com.quikr.platform.datastore.mysql;

import java.sql.SQLException;

public interface DBCommand {
	public Object execute(MySqlStore mysql) throws SQLException;
}
