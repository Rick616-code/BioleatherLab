package pe.bioleather.dao;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class UsuarioDAO {
    private final JdbcTemplate db;
    public UsuarioDAO(JdbcTemplate db) { this.db = db; }

    public Map<String, Object> identidad(long id) {
        return db.queryForMap("SELECT id,nombre,correo,rol FROM usuarios WHERE id=?", id);
    }
    public boolean existeCorreo(String correo) {
        return db.queryForObject("SELECT COUNT(*) FROM usuarios WHERE correo=?", Integer.class, correo) > 0;
    }
    public void registrarCliente(String nombre, String correo, String hash) {
        db.update("INSERT INTO usuarios(nombre,correo,clave_hash,rol,activo) VALUES(?,?,?,'CLIENTE',TRUE)", nombre, correo, hash);
    }
    public List<Map<String, Object>> buscarCredenciales(String correo) {
        return db.queryForList("SELECT id,rol,clave_hash,activo FROM usuarios WHERE correo=?", correo);
    }
    public List<Map<String, Object>> listarClientes() {
        return db.queryForList("SELECT u.id,u.nombre,u.correo,u.activo,COUNT(p.id) pedidos FROM usuarios u LEFT JOIN pedidos p ON p.usuario_id=u.id WHERE u.rol='CLIENTE' GROUP BY u.id,u.nombre,u.correo,u.activo ORDER BY u.id");
    }
    public int modificarCliente(long id, String nombre, String correo, boolean activo) {
        return db.update("UPDATE usuarios SET nombre=?,correo=?,activo=? WHERE id=? AND rol='CLIENTE'", nombre, correo, activo, id);
    }
}
