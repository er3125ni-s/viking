package se.lu.ics.models;

public class AppModel {
    private CustomerRegister customerRegister;


    public AppModel() {
        customerRegister = new CustomerRegister();
    }

    public CustomerRegister getCustomerRegister() {
        return this.customerRegister;

    }
}
