package main.java.edu.ingsoft.colegio.gotitas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.java.edu.ingsoft.colegio.gotitas.dto.request.RegisterRequest;
import main.java.edu.ingsoft.colegio.gotitas.dto.response.RegisterResponse;

import main.java.edu.ingsoft.colegio.gotitas.service.AuthService;
import main.java.edu.ingsoft.colegio.gotitas.util.SceneManager;

public class RegisterController {

    SceneManager stage;
    AuthService userRepo;

    @FXML
    private TextField txtFieldNombre;
    @FXML
    private TextField txtFieldApellido;
    @FXML
    private TextField txtFieldEmail;
    @FXML
    private PasswordField txtFieldPass;

    public RegisterController(SceneManager stage, AuthService userRepo) {
        this.stage = stage;
        this.userRepo = userRepo;
    }

    @FXML
    private void handleRegister() {
        try {
            // Validar campos obligatorios básicos
            if (txtFieldNombre.getText().trim().isEmpty()
                    || txtFieldEmail.getText().trim().isEmpty()
                    || txtFieldPass.getText().trim().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Por favor completa al menos Nombre, Correo y Contraseña.");
                return;
            }

            // Recoger datos de la vista
            String nombre = txtFieldNombre.getText().trim();
            String apellido = txtFieldApellido.getText().trim();
            String email = txtFieldEmail.getText().trim();
            String contrasenaHashed = txtFieldPass.getText(); // Nota: Asegúrate de aplicar hashing si tu BD lo requiere

            // Construir el objeto Request (sin fecha de nacimiento)
            RegisterRequest request = new RegisterRequest(
                    null, // idDocente
                    email,
                    contrasenaHashed,
                    1, // idRol por defecto
                    null, // idEstudiante 
                    null, // idCiudad
                    nombre,
                    apellido,
                    null
            );

            // Simulación de llamada directa al guardado
            RegisterResponse response = invocarGuardadoEstudiante(request);

            if (response != null && response.isSuccess()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Exitoso", response.getMessage());
                limpiarCampos();
            } else {
                String mensajeError = response != null ? response.getMessage() : "Error desconocido al registrar.";
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Registro", mensajeError);
            }

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error en el sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverLogin() throws Exception {
        stage.showLoginView();
    }

    private RegisterResponse invocarGuardadoEstudiante(RegisterRequest request) throws Exception {
        return userRepo.saveEstudiante(request);
    }

    private void limpiarCampos() {
        txtFieldNombre.clear();
        txtFieldApellido.clear();
        txtFieldEmail.clear();
        txtFieldPass.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}