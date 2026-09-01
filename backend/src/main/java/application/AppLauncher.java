package application;


import application.views.InicioFXML;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppLauncher {

    public static void main(String[] args) {
        System.out.println("inicio**************************************************************************");
        InicioFXML.launch(InicioFXML.class, args);
    }

}