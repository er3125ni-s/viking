package se.lu.ics.models;

import java.util.ArrayList;

public class CustomerRegister {
    private ArrayList<Customer> customers;
    public CustomerRegister() {
    this.customers = new ArrayList<Customer>();
    }
    /*
    * Adds the given customer to the register.
    * @param customer The customer to add.
    */
    public void addCustomer(Customer customer) {
    this.customers.add(customer);
    }
    /*
    * Returns the customer with the given customerId, or null if no such customer exists.
    * @param customerId The id of the customer to find.
    * @return The customer with the given customerId, or null if no such customer exists.
    */
    public Customer findCustomer(String customerId) {
    for (Customer customer : this.customers) {
    if (customer.getCustomerId().equals(customerId)) {
    return customer;
    }
    }
    return null;
    }
    /*
    * Removes the customer with the given customerId, if such a customer exists.
    * @param customerId The id of the customer to remove.
    */
    public void removeCustomer(String customerId) {
    Customer customer = this.findCustomer(customerId);
    if (customer != null) {
    this.customers.remove(customer);
    }
    }
    /*
    * Sets the name of the customer with the given customerId, if such a customer exists.
    * @param customerId The id of the customer to set the name of.
    * @param name The new name of the customer.
    */
    public void setCustomerName(String customerId, String name) {
    Customer customer = this.findCustomer(customerId);
    if (customer != null) {
    customer.setName(name);
    }
    }
    /*
    * Sets the credit card of the customer with the given customerId, if such a customer exists.
    * And sets the holder of the credit card to the customer.
    * @param customerId The id of the customer to set the credit card of.
    * @param creditCard The new credit card of the customer.
    */
    public void setCreditCard(String customerId, CreditCard creditCard) {
    Customer customer = this.findCustomer(customerId);
    if (customer != null) {
    customer.setCreditCard(creditCard);
    creditCard.setHolder(customer);
    }
    }
    public ArrayList<Customer> getCustomers() {
    return this.customers;
    }
    
}
