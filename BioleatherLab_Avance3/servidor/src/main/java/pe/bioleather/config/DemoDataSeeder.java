package pe.bioleather.config;

import java.math.BigDecimal;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class DemoDataSeeder implements CommandLineRunner {
    private static final Logger log=LoggerFactory.getLogger(DemoDataSeeder.class);
    private final JdbcTemplate db;
    @Value("${ADMIN_EMAIL:}") private String adminEmail;
    @Value("${ADMIN_PASSWORD:}") private String adminPassword;
    public DemoDataSeeder(JdbcTemplate db){this.db=db;}
    @Override public void run(String... args){
        if(db.queryForObject("SELECT COUNT(*) FROM productos",Integer.class)==0){
            db.update("INSERT INTO productos(nombre,categoria,descripcion,precio,imagen,activo) VALUES(?,?,?,?,?,TRUE)","Cartera Aura","Cartera","Cartera de diseño conceptual en biocuero vegetal, acabado oliva.",new BigDecimal("149.00"),"cartera.png");
            db.update("INSERT INTO productos(nombre,categoria,descripcion,precio,imagen,activo) VALUES(?,?,?,?,?,TRUE)","Billetera Nativa","Billetera","Billetera compacta de diseño conceptual, verde bosque.",new BigDecimal("79.00"),"billetera.png");
            db.update("INSERT INTO productos(nombre,categoria,descripcion,precio,imagen,activo) VALUES(?,?,?,?,?,TRUE)","Correa Raíz","Correa","Correa de diseño conceptual en tono tierra.",new BigDecimal("59.00"),"correa.png");
            log.info("Catálogo conceptual creado");
        }
        if(!adminEmail.isBlank() && !adminPassword.isBlank() && adminPassword.length()>=8){
            String email=adminEmail.toLowerCase(Locale.ROOT);
            if(db.queryForObject("SELECT COUNT(*) FROM usuarios WHERE correo=?",Integer.class,email)==0){
                db.update("INSERT INTO usuarios(nombre,correo,clave_hash,rol,activo) VALUES(?,?,?,'ADMIN',TRUE)","Administrador",email,new BCryptPasswordEncoder().encode(adminPassword));
                log.info("Administrador de prueba habilitado");
            }
        }
    }
}
