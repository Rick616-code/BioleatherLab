package pe.bioleather.reports;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.bioleather.security.AuthSessionService;
@RestController
@RequestMapping("/api/admin/reports")
public class ReporteController {
    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final AuthSessionService auth;
    private final PedidoReporteService service;

    public ReporteController(AuthSessionService auth, PedidoReporteService service) {
        this.auth = auth;
        this.service = service;
    }

    @GetMapping("/orders.xlsx")
    public ResponseEntity<byte[]> pedidos(HttpServletRequest request) throws IOException {
        auth.requireAdmin(request);
        byte[] excel = service.generarExcel();
        log.info("Reporte Excel de pedidos generado");
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pedidos-bioleather.xlsx")
            .header("X-Content-Type-Options", "nosniff")
            .contentType(MediaType.parseMediaType(XLSX))
            .body(excel);
    }
}
