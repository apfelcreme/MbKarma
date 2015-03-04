package io.github.apfelcreme.MbKarmaBungee;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionManager {

	private static DatabaseConnectionManager instance = null;
	
	/**
	 * getInstance
	 * @return the classes only instance
	 */
	public static DatabaseConnectionManager getInstance() {
		if (instance == null) {
			instance = new DatabaseConnectionManager();
		}
		return instance;
	}
	
	/**
	 * initializes the database connection
	 * @param dbuser
	 * @param dbpassword
	 * @param database
	 * @param url
	 * @return
	 */
	public Connection initConnection(String dbuser, String dbpassword, String database, String url) {
		Connection connection;
		try {
			if (database.isEmpty() || database == null) {
				return null;
			} else {
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection("jdbc:mysql://" + url
						+ "/" + "", dbuser, dbpassword);
				if (connection != null) {
					try {
						connection.createStatement().execute(
								"CREATE DATABASE IF NOT EXISTS " + database);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				connection = DriverManager.getConnection("jdbc:mysql://" + url
						+ "/" + database, dbuser, dbpassword);
				createDatabase(database);
				return connection;
			}
		} catch(SQLException e) {
		} catch (ClassNotFoundException e1) {
		}
		return null;
	}
	
	/**
	 * returns the database connection to work with
	 * @return
	 */
	public Connection getConnection() {		
		try {
			return DriverManager.getConnection("jdbc:mysql://" + 
					MbKarmaBungee.getInstance().getConfig().getString("mysql.url")
					+ "/" + 
					MbKarmaBungee.getInstance().getConfig().getString("mysql.database"), 
					MbKarmaBungee.getInstance().getConfig().getString("mysql.dbuser"), 
					MbKarmaBungee.getInstance().getConfig().getString("mysql.dbpassword"));
		} catch (SQLException e) {
			MbKarmaBungee.getInstance().getLogger().severe("Database connection could not be built");
		}
		return null;
	}

	public void createDatabase(String database) {
		try {
			Connection connection = getConnection();
			connection.createStatement().execute(
					"CREATE DATABASE IF NOT EXISTS " + database);
			connection.createStatement().execute(
					"CREATE TABLE IF NOT EXISTS MbKarma_Player("
							+ "playerid BIGINT auto_increment not null,"
							+ "playername VARCHAR(50) UNIQUE NOT NULL,"
							+ "uuid VARCHAR(50) UNIQUE NOT NULL,"
							+ "currentAmount DOUBLE(8,5),"
							+ "seesParticles BOOLEAN,"
							+ "effect VARCHAR(50),"
							+ "PRIMARY KEY (playerid));");
			connection
					.createStatement()
					.execute(
							"CREATE TABLE IF NOT EXISTS MbKarma_Transactions("
									+ "transactionId BIGINT auto_increment NOT NULL,"
									+ "giverId BIGINT,"
									+ "targetId BIGINT, "
									+ "amount DOUBLE(8,5),"
									+ "FOREIGN KEY (giverId) REFERENCES MbKarma_Player(playerid),"
									+ "FOREIGN KEY (targetId) REFERENCES MbKarma_Player(playerid),"
									+ "PRIMARY KEY (transactionId));");
			connection
					.createStatement()
					.execute(
							"CREATE TABLE IF NOT EXISTS MbKarma_Relations("
									+ "playerid BIGINT not null,"
									+ "targetid BIGINT not null,"
									+ "relationRatio DOUBLE(8,5) not null,"
									+ "relationAmount DOUBLE(8,5),"
									+ "timesGiven BIGINT,"
									+ "timestamp BIGINT,"
									+ "FOREIGN KEY (playerid) REFERENCES MbKarma_Player(playerid),"
									+ "FOREIGN KEY (targetid) REFERENCES MbKarma_Player(playerid),"
									+ "PRIMARY KEY(playerid, targetid) )");
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
