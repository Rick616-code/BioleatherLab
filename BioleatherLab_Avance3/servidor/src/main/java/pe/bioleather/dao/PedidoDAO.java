package pe.bioleather.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
@Repository
public class PedidoDAO {
    private final JdbcTemplate db;
    public PedidoDAO(JdbcTemplate db) { this.db = db; }
    public long insertarCabecera(long usuarioId, BigDecimal total) {
        KeyHolder holder = new GeneratedKeyHolder();
        db.update(con -> {
            var ps=con.prepareStatement("INSERT INTO pedidos(usuario_id,estado,total) VALUES(?,'Recibido',?)",new String[]{"id"});
            ps.setLong(1,usuarioId); ps.setBigDecimal(2,total); return ps;
        }, holder);
        return holder.getKey().longValue();
    }
    public void asignarCodigo(long id, String code) { db.update("UPDATE pedidos SET codigo=? WHERE id=?",code,id); }
    public void insertarDetalle(long pedido, long producto, int cantidad, BigDecimal precio) {
        db.update("INSERT INTO pedido_detalles(pedido_id,producto_id,cantidad,precio_unitario) VALUES(?,?,?,?)",pedido,producto,cantidad,precio);
    }
    public void guardarHistorial(long pedido, String estado, long responsable) {
        db.update("INSERT INTO historial_estados(pedido_id,estado,responsable_id) VALUES(?,?,?)",pedido,estado,responsable);
    }
    public List<Map<String,Object>> lineas(long id) {
        return db.queryForList("SELECT d.producto_id,p.nombre,d.cantidad,d.precio_unitario FROM pedido_detalles d JOIN productos p ON p.id=d.producto_id WHERE d.pedido_id=?",id);
    }
    public List<Map<String,Object>> deUsuario(long id) {
        return db.queryForList("SELECT p.*,u.nombre FROM pedidos p JOIN usuarios u ON u.id=p.usuario_id WHERE p.usuario_id=? ORDER BY p.id DESC",id);
    }
    public List<Map<String,Object>> deCodigo(String codigo,long id,String rol) {
        return db.queryForList("SELECT p.*,u.nombre FROM pedidos p JOIN usuarios u ON u.id=p.usuario_id WHERE p.codigo=? AND (p.usuario_id=? OR ?='ADMIN')",codigo,id,rol);
    }
    public List<Map<String,Object>> todos() {
        return db.queryForList("SELECT p.*,u.nombre FROM pedidos p JOIN usuarios u ON u.id=p.usuario_id ORDER BY p.id DESC");
    }
    public int actualizarEstado(long id,String estado) {
        return db.update("UPDATE pedidos SET estado=? WHERE id=?",estado,id);
    }
    public boolean tieneAcceso(long pedidoId,long usuarioId,String rol) {
        return db.queryForObject("SELECT COUNT(*) FROM pedidos WHERE id=? AND (usuario_id=? OR ?='ADMIN')",Integer.class,pedidoId,usuarioId,rol)>0;
    }
    public List<Map<String,Object>> historial(long id) {
        return db.queryForList("SELECT h.estado,h.fecha,u.nombre responsable FROM historial_estados h JOIN usuarios u ON u.id=h.responsable_id WHERE h.pedido_id=? ORDER BY h.id",id);
    }
}
