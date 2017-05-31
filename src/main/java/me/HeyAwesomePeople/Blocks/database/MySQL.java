package me.HeyAwesomePeople.Blocks.database;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.HeyAwesomePeople.Blocks.Blocks;

import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;

public class MySQL {

    private HikariDataSource hikari;

    public MySQL(Blocks fp) {
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        props.setProperty("dataSource.serverName", fp.getConfig().getString("mysql.host"));
        props.setProperty("dataSource.port", fp.getConfig().getString("mysql.port"));
        props.setProperty("dataSource.databaseName", fp.getConfig().getString("mysql.databaseName"));
        props.setProperty("dataSource.user", fp.getConfig().getString("mysql.user"));
        props.setProperty("dataSource.password", fp.getConfig().getString("mysql.password"));
        props.setProperty("dataSource.useServerPrepStmts", "true");
        props.setProperty("dataSource.cachePrepStmts", "true");
        props.setProperty("dataSource.prepStmtCacheSize", "250");
        props.setProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        props.put("dataSource.logWriter", new PrintWriter(System.out));

        HikariConfig config = new HikariConfig(props);
        hikari = new HikariDataSource(config);

        try {
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HikariDataSource getHikari() {
        return this.hikari;
    }

    private void createTables() throws SQLException {
        Connection connection = getHikari().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS blocks_playerdata" +
                " (id VARCHAR(36) UNIQUE, blocks BIGINT(20), cubes BIGINT(20))");
        connection.close();
    }

    public Integer[] retrieveData(UUID id) throws SQLException {
        Integer blocks, cubes, contains;
        Connection connection = getHikari().getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM blocks_playerdata WHERE id=? LIMIT 1");
        statement.setString(1, id.toString());
        ResultSet result = statement.executeQuery();

        if (result.next()) {
            blocks = result.getInt(2);
            cubes = result.getInt(3);
            contains = 1;
        } else {
            blocks = -1;
            cubes = -1;
            contains = 0;
        }

        connection.close();
        return new Integer[]{blocks, cubes, contains};
    }

    public void uploadData(UUID id, Integer blocks, Integer cubes) throws SQLException {
        Connection connection = getHikari().getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO blocks_playerdata (id, blocks, cubes) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id=?, blocks=?, cubes=?");

        statement.setString(1, id.toString());
        statement.setInt(2, blocks);
        statement.setInt(3, cubes);
        statement.setString(4, id.toString());
        statement.setInt(5, blocks);
        statement.setInt(5, cubes);
        statement.execute();

        connection.close();
    }


}
