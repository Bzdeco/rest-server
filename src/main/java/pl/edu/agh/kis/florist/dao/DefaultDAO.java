package pl.edu.agh.kis.florist.dao;

/**
 * Created by bzdeco on 23.01.17.
 */
public class DefaultDAO {
    protected final String DB_URL;

    protected DefaultDAO() {
        this.DB_URL = "jdbc:sqlite:test.db";
    }

    protected DefaultDAO(String dbUrl) {
        this.DB_URL = dbUrl;
    }
}
