package pe.bioleather.api;

import java.math.BigDecimal;
import java.util.List;
public final class ApiInputs {
    private ApiInputs() {}
    public record Credentials(String correo, String clave) {}
    public record Registration(String nombre, String correo, String clave) {}
    public record ProductInput(String nombre, String categoria, String descripcion, BigDecimal precio, String imagen, Boolean activo) {}
    public record Line(long productoId, int cantidad) {}
    public record OrderInput(List<Line> lineas) {}
    public record StateInput(String estado) {}
    public record ClientInput(String nombre, String correo, Boolean activo) {}
}
