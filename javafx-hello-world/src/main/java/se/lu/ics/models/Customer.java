package se.lu.ics.models;

public class Customer {

    private String customerId;
    private String name;
    private CreditCard creditCard;
    public Customer(String customerId, String name) {
    this.customerId = customerId;
    this.name = name;
    }
    public String getCustomerId() {
    return this.customerId;
    }
    public void setCustomerId(String id) {
    this.customerId = id;
    }
    public String getName() {
    return this.name;
    }
    public void setName(String name) {
    this.name = name;
    }
    public CreditCard getCreditCard() {
    return this.creditCard;
    }
    public void setCreditCard(CreditCard creditCard) {
    this.creditCard = creditCard;
    }
}
