package main.java.edu.ingsoft.colegio.gotitas.config;

//nunca exponer credenciales en github
public class Credentials {
    
    public static final String DATA_BASE = System.getenv("DATA_BASE");
    public static final String URL_DB = "jdbc:mysql://localhost:3306";
    public static final String PASS_DB = "MascotaMax17";
    public static final String USER_DB = "root";
    
}

