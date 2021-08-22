package banking;

import java.sql.*;
import java.util.Scanner;

public class Database {

    private final String url;
    private static final Scanner scanner = new Scanner(System.in);

    public Database(String fileName) {
        this.url = "jdbc:sqlite:" + fileName;
        checkDatabase();
    }

    public void checkDatabase() {
        justExecute("CREATE TABLE IF NOT EXISTS card(" +
                "id INTEGER PRIMARY KEY," +
                "number TEXT NOT NULL," +
                "pin TEXT NOT NULL," +
                "balance INTEGER DEFAULT 0)");
    }

    public void createCard(String cardNumber, String cardPin) {
        justExecute("INSERT INTO card (number, pin, balance) " +
                "VALUES ('" + cardNumber + "','" + cardPin + "', 0);");
    }

    public boolean cardExists(String cardNumber) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT" +
                     "(EXISTS" +
                     "(SELECT 1 FROM card " +
                     "WHERE number = '" + cardNumber + "'));")) {
            return resultSet.getBoolean(1);
        }
    }

    public Integer getCardIdByNumber(String cardNumber) throws SQLException {
        if (cardExists(cardNumber)) {
            try (Connection connection = DriverManager.getConnection(url);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT id FROM card " +
                         "WHERE number = '" + cardNumber + "';")) {
                return resultSet.getInt("id");
            }
        } else {
            return null;
        }
    }

    public Integer logIntoAccount(String cardNumber, String cardPin) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM card " +
                     "WHERE number = '" + cardNumber + "' " +
                     "AND pin = '" + cardPin + "';")) {
            if (!resultSet.next()) {
                return null;
            } else {
                return resultSet.getInt("id");
            }
        }
    }

    public int getCardBalance(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM card " +
                     "WHERE id = " + id + ";")) {
            return resultSet.getInt("balance");
        }
    }

    public void updateCardBalance(int id, int sum) throws SQLException {
        String query = "UPDATE card SET balance = balance + ? " +
                "WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, sum);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void updateCardBalance(int idTo, int sum, int idFrom) throws SQLException {
        String query = "UPDATE card SET balance = balance + ? " +
                "WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url)) {
            connection.setAutoCommit(false);

            Savepoint savepoint = connection.setSavepoint();

            try {
                PreparedStatement statementWithdraw = connection.prepareStatement(query);
                PreparedStatement statementPut = connection.prepareStatement(query);

                statementWithdraw.setInt(1, -sum);
                statementWithdraw.setInt(2, idFrom);
                statementPut.setInt(1, sum);
                statementPut.setInt(2, idTo);

                statementWithdraw.executeUpdate();
                statementPut.executeUpdate();

                connection.commit();
            } catch (Exception e) {
                connection.rollback(savepoint);
            }
        }
    }

    private void justExecute(String query) {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeAccount(int id) throws SQLException {
        String query = "DELETE FROM card " +
                "WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("The account has been closed!");
        }
    }
}
