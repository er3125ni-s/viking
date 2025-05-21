package se.lu.ics.models;

public class CreditCard {
    private String number;
    private String type;
    private Customer holder;
    public CreditCard(String number, String type) {
    this.number = number;
    this.type = type;
    }
    public String getNumber() {
    return number;
    }
    public void setNumber(String number) {
    this.number = number;
    }
    public String getType() {
    return type;
    }
    public void setType(String type) {
    this.type = type;
    }
    public Customer getHolder() {
    return this.holder;
    }
    public void setHolder(Customer customer) {
    this.holder = customer;
    }
}
