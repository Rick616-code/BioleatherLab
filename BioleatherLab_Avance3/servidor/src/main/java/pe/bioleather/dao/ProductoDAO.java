package pe.bioleather.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.bioleather.api.ApiInputs.ProductInput;
@Repository
public class ProductoDAO {
    private final JdbcTemplate db;
    public ProductoDAO(JdbcTemplate db) { this.db = db; }
    public List<Map<String, Object>> catalogo(String q, String categoria) {
        return db.queryForList("SELECT id,nombre,categoria,descripcion,precio,imagen FROM productos WHERE activo=TRUE AND LOWER(nombre) LIKE ? AND LOWER(categoria) LIKE ? ORDER BY id", "%"+q+"%", "%"+categoria+"%");
    }
    public List<Map<String, Object>> detalleActivo(long id) {
        return db.queryForList("SELECT id,nombre,categoria,descripcion,precio,imagen FROM productos WHERE id=? AND activo=TRUE", id);
    }
    public List<Map<String, Object>> listarTodos() {
        return db.queryForList("SELECT * FROM productos ORDER BY id");
    }
    public void crear(ProductInput p) {
        db.update("INSERT INTO productos(nombre,categoria,descripcion,precio,imagen,activo) VALUES(?,?,?,?,?,TRUE)", p.nombre(),p.categoria(),p.descripcion(),p.precio(),p.imagen());
    }
    public int actualizar(long id, ProductInput p) {
        return db.update("UPDATE productos SET nombre=?,categoria=?,descripcion=?,precio=?,imagen=?,activo=? WHERE id=?", p.nombre(),p.categoria(),p.descripcion(),p.precio(),p.imagen(),Boolean.TRUE.equals(p.activo()),id);
    }
    public List<Map<String, Object>> precioActivo(long id) {
        return db.queryForList("SELECT precio FROM productos WHERE id=? AND activo=TRUE",id);
    }
}
