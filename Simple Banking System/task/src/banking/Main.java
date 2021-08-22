package banking;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        String fileName = "default.db";
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i];
            if ("-fileName".equals(arg)) {
                fileName = args[i + 1];
                break;
            }
        }

        Bank bank = new Bank(fileName);
        bank.runBankApp();
    }
}