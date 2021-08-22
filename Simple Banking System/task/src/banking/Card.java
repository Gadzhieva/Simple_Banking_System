package banking;

public class Card {

    private final String cardNumber;
    private final String cardPin;

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardPin() {
        return cardPin;
    }

    public Card(String number, String pin) {
        cardNumber = number;
        cardPin = pin;
    }
}
