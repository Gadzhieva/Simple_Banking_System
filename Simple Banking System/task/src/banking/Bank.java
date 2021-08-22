package banking;

import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class Bank {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();
    private static final String BIC = "400000";

    private State state;
    private final Database dataBase;

    public Bank(String fileName) {
        state = State.LOG_IN;
        dataBase = new Database(fileName);
    }

    public void runBankApp() throws SQLException {
        while (state == State.LOG_IN) {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    createAccount();
                    break;
                case "2":
                    logIntoAccount();
                    break;
                case "0":
                    System.out.println();
                    System.out.println("Bye!");
                    state = State.EXIT;
                    return;
                default:
                    System.out.println("Invalid input");
                    break;
            }
        }
    }

    private void createAccount() throws SQLException {
        Card card = new Card(generateCardNumber(), generatePin());
        dataBase.createCard(card.getCardNumber(), card.getCardPin());
        System.out.println();
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(card.getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(card.getCardPin());
    }

    public String generateCardNumber() throws SQLException {
        while (true) {
            StringBuilder builder = new StringBuilder(BIC);
            for (int i = 0; i < 9; i++) {
                builder.append(random.nextInt(10));
            }
            builder.append(generateCardLastDigit(builder.toString()));
            String result = builder.toString();
            if (!dataBase.cardExists(result)) {
                return result;
            }
        }
    }

    private String generatePin() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private void logIntoAccount() throws SQLException {
        System.out.println("Enter your card number:");
        String number = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String pin = scanner.nextLine();
        Integer id = dataBase.logIntoAccount(number, pin);
        if (id != null) {
            state = State.AUTHORISED;
            System.out.println();
            System.out.println("You have successfully logged in!");
            System.out.println();
            manageAccount(id);
        } else {
            System.out.println();
            System.out.println("Wrong card number or PIN!");
            System.out.println();
        }
    }

    public void manageAccount(Integer id) throws SQLException {
        while (state == State.AUTHORISED) {
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    System.out.println();
                    System.out.println("Balance: " + dataBase.getCardBalance(id));
                    System.out.println();
                    break;
                case "2":
                    addIncome(id);
                    break;
                case "3":
                    transfer(id);
                    break;
                case "4":
                    dataBase.closeAccount(id);
                    break;
                case "5":
                    state = State.LOG_IN;
                    System.out.println();
                    System.out.println("You have successfully logged out!");
                    System.out.println();
                    break;
                case "0":
                    System.out.println();
                    System.out.println("Bye!");
                    state = State.EXIT;
                    break;
                default:
                    System.out.println("Invalid input");
                    break;
            }
        }
    }

    private static int generateCardLastDigit(String cardNumber) {
        int[] array = new int[cardNumber.length()];
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            array[i] = Integer.parseInt(String.valueOf(cardNumber.charAt(i)));
        }
        for (int i = 0; i < array.length; i++) {
            if (i % 2 == 0) {
                array[i] = array[i] * 2;
                if (array[i] > 9) {
                    array[i] = array[i] / 10 + array[i] % 10;
                }
            }
            sum += array[i];
        }
        if (sum % 10 == 0) {
            return 0;
        } else {
            return 10 - sum % 10;
        }
    }

    private boolean isValid(String number) {
        int lastNumber = Integer.parseInt(String.valueOf(number.charAt(number.length() - 1)));
        return lastNumber == generateCardLastDigit(number.substring(0, number.length() - 1));
    }

    private Integer getCardIdIfExists(String number) throws SQLException {
        if (!isValid(number)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return null;
        } else {
            Integer cardId = dataBase.getCardIdByNumber(number);
            if (cardId == null) {
                System.out.println("Such a card does not exist.");
            }
            return cardId;
        }
    }

    private void transfer(int id) throws SQLException {
        System.out.println();
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String cardNumberToTransfer = scanner.nextLine();
        Integer cardIdToTransfer = getCardIdIfExists(cardNumberToTransfer);
        if (cardIdToTransfer != null) {
            if (id == cardIdToTransfer) {
                System.out.println("You can't transfer money to the same account!");
            } else {
                System.out.println("Enter how much money you want to transfer:");
                int amount = scanner.nextInt();
                if (amount > dataBase.getCardBalance(id)) {
                    System.out.println("Not enough money!");
                } else {
                    dataBase.updateCardBalance(cardIdToTransfer, amount, id);
                    System.out.println("Success!");
                }
            }
        }
    }

    private void addIncome(int id) throws SQLException {
        System.out.println();
        System.out.println("Enter income:");
        int income = scanner.nextInt();
        scanner.nextLine();
        dataBase.updateCardBalance(id, income);
        System.out.println("Income was added!");
        System.out.println();
    }

}
