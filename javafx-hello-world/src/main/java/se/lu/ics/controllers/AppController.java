package se.lu.ics.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.lu.ics.models.AppModel;
import se.lu.ics.models.Customer;
import se.lu.ics.models.CreditCard;

public class AppController {
    private final Stage primaryStage;
    private final AppModel appModel;
    
    public AppController(Stage primaryStage, AppModel appModel) {
        this.primaryStage = primaryStage;
        this.appModel = appModel;
    }

    public void showMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            MainViewController mainController = loader.getController();
            mainController.setAppController(this);
            mainController.setAppModel(appModel);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Business logic methods
    public String addCustomer(String customerId, String name, boolean hasCard, String cardNumber, String cardType) {
        Customer customer = new Customer(customerId, name);
        appModel.getCustomerRegister().addCustomer(customer);
        
        if (hasCard) {
            CreditCard creditCard = new CreditCard(cardNumber, cardType);
            customer.setCreditCard(creditCard);
            creditCard.setHolder(customer);
            return "Customer added: " + customer.getName() + " with credit card: " + creditCard.getNumber();
        }
        return "Customer added: " + customer.getName();
    }

    public Customer findCustomer(String customerId) {
        return appModel.getCustomerRegister().findCustomer(customerId);
    }

    public String deleteCustomer(String customerId) {
        appModel.getCustomerRegister().removeCustomer(customerId);
        return "Customer deleted";
    }

    public String updateCustomerName(String customerId, String newName) {
        appModel.getCustomerRegister().setCustomerName(customerId, newName);
        return "Customer name changed";
    }
} 