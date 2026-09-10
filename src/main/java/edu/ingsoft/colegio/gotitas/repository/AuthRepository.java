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

    //constructor
    /*Los métodos son acciones especificas, son tareas
    individuales, algunos métodos solo realizan una 
    tarea, pero no retornan nada son "void", otros métodos,
    realizan tareas, y retornan un tipo de dato primitivo o 
    comúesto (Clase). Divide y venceras: un método debe ser
    encargado de reañizar únicamente una tarea especifica, el
    nombre de ese metodo debe ser modular, directo*/

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
        String userSql = "insert into Usuarios (id_usuario, email, contrasena_hash, id_rol) values (uuid(), ?, ?, ?)";
        String estudianteSql = "insert into Estudiantes (id_estudiante, nombre, apellido, fecha_nacimiento, id_ciudad, correo_electronico) values (?, ?, ?, ?, ?, ?, ?)";
        String countEstudents = "select count(*) from estudiantes";

        java.sql.Connection conn = null;

        try {
            conn = DataBaseConnection.getConnectionDataBase();
            conn.setAutoCommit(false);

            // 1. Insertar en la tabla Usuarios
            // Asumiendo el orden de los interrogantes según tu consulta: email, contrasena, rol, ciudad
            try (PreparedStatement pstmUser = conn.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmUser.setString(1, registerRequest.getEmail());
                pstmUser.setString(2, registerRequest.getContrasenaHashed());
                pstmUser.setInt(3, 1);
                pstmUser.executeUpdate();

                // Si necesitas recuperar el ID autogenerado del usuario (en caso de que la BD lo use para relacionar)
                // Puedes extraerlo aquí si tu tabla Usuarios usa auto-increment en lugar de uuid() puro.
            }

            // 2. Insertar en la tabla Estudiante
            try (PreparedStatement pstmEstudiante = conn.prepareStatement(estudianteSql)) {
                pstmEstudiante.setString(1, "concat(\"EST\", count(*))");
                pstmEstudiante.setString(2, registerRequest.getNombre());
                pstmEstudiante.setString(3, registerRequest.getApellido());

                // Convertir java.util.Date a java.sql.Date para la base de datos
                if (registerRequest.getFechaNacimiento() != null) {
                    pstmEstudiante.setDate(4, new java.sql.Date(registerRequest.getFechaNacimiento().getTime()));
                } else {
                    pstmEstudiante.setNull(4, java.sql.Types.DATE);
                }

                // Si el estudiante necesita relacionarse con el usuario recién creado, 
                // asegúrate de pasar el ID del usuario correspondiente en el último parámetro.
                pstmEstudiante.setString(5, registerRequest.getIdDocente()); // O el campo que relacione al usuario
                pstmEstudiante.setString(6, "concat(\"CIU0\", FLOOR(1 + RAND() * 10), FLOOR(1 + RAND() * 9))");
                pstmEstudiante.setString(7, registerRequest.getEmail());
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
}
