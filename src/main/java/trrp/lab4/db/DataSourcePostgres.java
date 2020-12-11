package trrp.lab4.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import trrp.lab4.MainController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

@Service
public class DataSourcePostgres {

    Logger LOGGER = Logger.getLogger(DataSourcePostgres.class.getName());


    private String jdbc = "jdbc:postgresql://%s/egrul_info?currentSchema=public&charSet=utf8";
    private String generalHost;
    private String reserveHost;
    private String user;
    private String password;
    private boolean isGeneralDB = true;

    private Connection connection;

    public DataSourcePostgres(@Value("${data.source.postgres.host.general}") String generalHost,
                              @Value("${data.source.postgres.host.reserve}") String reserveHost,
                              @Value("${data.source.postgres.user}") String user,
                              @Value("${data.source.postgres.password}") String password) {
        this.generalHost = generalHost;
        this.reserveHost = reserveHost;
        this.user = user;
        this.password = password;

        try {
            String url = String.format(jdbc, generalHost);
            connection = DriverManager.getConnection(url, user, password);
            isGeneralDB = true;
            LOGGER.info("Используется основное подключение к БД: " + generalHost);
        } catch (SQLException ex) {
            ex.printStackTrace();
            switchConnection();
        }
    }

    private void switchConnection() {
        try {
            if (isGeneralDB) {
                String url = String.format(jdbc, reserveHost);
                connection = DriverManager.getConnection(url, user, password);
                isGeneralDB = false;
                LOGGER.info("Используется резервное подключение к БД: " + reserveHost);
            } else {
                String url = String.format(jdbc, generalHost);
                connection = DriverManager.getConnection(url, user, password);
                isGeneralDB = true;
                LOGGER.info("Используется основное подключение к БД: " + generalHost);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isValid(30)) {
                return connection;
            } else {
                switchConnection();
                return connection;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
