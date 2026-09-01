package application.controllers;

import application.config.SpringContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.IOException;


@Controller // Garante que o Spring gerencie este controlador
public class InicioController {
    @FXML
    private Button btUser, btColaboradores, btCarros, btDespesas, btPagamentos;


    // Construtor Padrão Necessário para JavaFX (Deve estar presente)
    public InicioController() {

    }

    @FXML
    protected void onclickOnUser() {
        try {
            System.out.println("Click");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/users.fxml"));
            // **Configura o Spring para gerenciar o controlador**
            fxmlLoader.setControllerFactory(SpringContext::getBean);

            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Utilizadores");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onclickOnColaborador() {
        try {
            System.out.println("Click");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/colaborador.fxml"));
            // **Configura o Spring para gerenciar o controlador**
            fxmlLoader.setControllerFactory(SpringContext::getBean);

            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Colaborador");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onclickOnCarro() {
        try {
            System.out.println("Click");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/carro.fxml"));
            // **Configura o Spring para gerenciar o controlador**
            fxmlLoader.setControllerFactory(SpringContext::getBean);

            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Clientes");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onclickOnDespesas() {
        try {
            System.out.println("Click");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/despesa.fxml"));
            // **Configura o Spring para gerenciar o controlador**
            fxmlLoader.setControllerFactory(SpringContext::getBean);

            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Produtos");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onclickOnCartoes() {
        try {
            System.out.println("Click");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/cartao.fxml"));
            // **Configura o Spring para gerenciar o controlador**
            fxmlLoader.setControllerFactory(SpringContext::getBean);

            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cartões");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onclickOnPagamento() {
        try {
            System.out.println("Click");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/pagamento.fxml"));
            // **Configura o Spring para gerenciar o controlador**
            fxmlLoader.setControllerFactory(SpringContext::getBean);

            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Pagamentos");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    protected void onclickOnListagemPagamentos() {
        try {
            System.out.println("Click");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/listagem_pagamentos.fxml"));
            // **Configura o Spring para gerenciar o controlador**
            fxmlLoader.setControllerFactory(SpringContext::getBean);

            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Pagamentos");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
