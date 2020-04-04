package xyz.derkades.derkutils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseHandler {

	private final boolean debug;
	private final Connection databaseConnection;

	private final String host;
	private final int port;
	private final String database;

	public DatabaseHandler(final String host, final int port, final String database, final String user, final String password, final boolean debug) throws SQLException {
		this.host = host;
		this.port = port;
		this.database = database;

		this.debug = debug;

		final Properties properties = new Properties();
		properties.setProperty("user", user);
		properties.setProperty("password", password);
		properties.setProperty("useSSL", "false");
		properties.setProperty("autoReconnect", "true");

		this.databaseConnection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, properties);
	}

	public DatabaseHandler(final String host, final int port, final String database, final String user, final String password) throws SQLException {
		this(host, port, database, user, password, false);
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String getDatabase() {
		return this.database;
	}

	public PreparedStatement prepareStatement(final String sql, final Object... parameters) throws SQLException {
		if (this.debug) {
			System.out.println("Making query: " + sql);
			for (final Object parameter : parameters) {
				System.out.println("with parameter: " + parameter);
			}
		}

		final PreparedStatement statement = this.databaseConnection.prepareStatement(sql);

		int i = 1;
		for (final Object parameter : parameters) {
			if (parameter instanceof String) {
				statement.setString(i, (String) parameter);
			} else if (parameter instanceof Integer) {
				statement.setInt(i, (int) parameter);
			} else if (parameter instanceof Double) {
				statement.setDouble(i, (double) parameter);
			} else {
				statement.setString(i, parameter.toString());
			}

			i++;
		}

		return statement;
	}

	public Connection getConnection() {
		return this.databaseConnection;
	}

	public void createTableIfNonexistent(final String tableName, final String sql) throws SQLException {
		final DatabaseMetaData meta = this.getConnection().getMetaData();
		final ResultSet result = meta.getTables(null, null, tableName, null);

		if (result != null && result.next()) {
			return; // Table exists
		}

		result.close();

		final PreparedStatement statement = this.prepareStatement(sql);
		statement.execute();
		statement.close();
	}

}
