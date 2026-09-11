package main.java.edu.ingsoft.colegio.gotitas.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import main.java.edu.ingsoft.colegio.gotitas.config.DataBaseConnection;
import main.java.edu.ingsoft.colegio.gotitas.dto.request.LoginRequest;
import main.java.edu.ingsoft.colegio.gotitas.dto.request.RegisterRequest;
import main.java.edu.ingsoft.colegio.gotitas.dto.response.LoginResponse;
import main.java.edu.ingsoft.colegio.gotitas.dto.response.RegisterResponse;

public class AuthRepository {

    //atributos
    private boolean sqlStatus = false;

    public LoginResponse findUserByEmail(LoginRequest loginRequest) throws Exception {
        String sql = "select d.nombre, d.apellido, u.contrasena_hash from usuarios as u"
                + " right join docentes as d"
                + " on d.id_docente = u.id_docente"
                + " where email = ? ";
        try (PreparedStatement pstm = DataBaseConnection.getConnectionDataBase().prepareStatement(sql)) {
            pstm.setString(1, loginRequest.getEmail());
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return new LoginResponse(rs.getString("nombre"), rs.getString("apellido"), rs.getString("contrasena_hash"));
            }
        } catch (SQLException e) {
            System.out.println("error al encontrar el EMAIL " + e.getMessage());
        }
        return null;
    }

    public RegisterResponse saveEstudiante(RegisterRequest registerRequest) throws Exception {
        String userSql = "insert into usuarios (id_usuario, email, contrasena_hash , id_rol) values (uuid(), ?, ?, ?)";
        String estudianteSql = "insert into estudiantes (id_estudiante, nombre, apellido, id_ciudad, correo_electronico) values (?, ?, ?, ?, ?)";

        java.sql.Connection conn = null;

        try {
            conn = DataBaseConnection.getConnectionDataBase();
            conn.setAutoCommit(false);

            // 1. Insertar en la tabla Usuarios (Convirtiendo el rol de int a String de forma segura)
            try (PreparedStatement pstmUser = conn.prepareStatement(userSql)) {
                pstmUser.setString(1, registerRequest.getEmail());
                pstmUser.setString(2, registerRequest.getContrasenaHashed());
                
                // Conversión correcta de int a String para evitar el error de compilación
                String rol = String.valueOf(registerRequest.getIdRol());
                pstmUser.setString(3, rol);
                
                pstmUser.executeUpdate();
            }

            // 2. Insertar en la tabla Estudiantes (5 parámetros exactos)
            try (PreparedStatement pstmEstudiante = conn.prepareStatement(estudianteSql)) {
                String idEstudianteUnico = "EST" + System.currentTimeMillis();
                
                pstmEstudiante.setString(1, idEstudianteUnico);                      // Parámetro 1: id_estudiante
                pstmEstudiante.setString(2, registerRequest.getNombre());            // Parámetro 2: nombre
                pstmEstudiante.setString(3, registerRequest.getApellido());          // Parámetro 3: apellido
                pstmEstudiante.setString(4, "C001");                                 // Parámetro 4: id_ciudad predeterminada
                pstmEstudiante.setString(5, registerRequest.getEmail());             // Parámetro 5: correo_electronico
                
                pstmEstudiante.executeUpdate();
            }

            // Si todo sale bien, confirmamos la transacción
            conn.commit();
            return new RegisterResponse(true, "Estudiante registrado exitosamente", registerRequest.getNombre());

        } catch (SQLException e) {
            // En caso de fallar, revertimos los cambios realizados
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Error en rollback: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Error en saveEstudiante: " + e.getMessage());
            return new RegisterResponse(false, "Error de SQL: " + e.getMessage());
        } finally {
            // Restauramos el comportamiento por defecto de la conexión
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    System.out.println("Error al restaurar autoCommit: " + ex.getMessage());
                }
            }
        }
    }
    
    //archivo actualizado
}