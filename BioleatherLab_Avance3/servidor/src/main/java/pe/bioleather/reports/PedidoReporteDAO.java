package pe.bioleather.reports;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class PedidoReporteDAO {
    private final JdbcTemplate jdbc;
    public PedidoReporteDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> listarDetallePedidos() {
        return jdbc.queryForList("""
            SELECT p.codigo, p.estado, p.creado_en, u.nombre AS cliente,
                   pr.nombre AS producto, d.cantidad, d.precio_unitario, p.total
            FROM pedidos p
            JOIN usuarios u ON u.id = p.usuario_id
            JOIN pedido_detalles d ON d.pedido_id = p.id
            JOIN productos pr ON pr.id = d.producto_id
            ORDER BY p.id DESC, d.id
            """);
    }
}
