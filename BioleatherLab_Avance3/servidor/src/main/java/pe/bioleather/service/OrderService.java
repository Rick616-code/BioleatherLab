package pe.bioleather.service;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.bioleather.api.ApiInputs.OrderInput;
import pe.bioleather.api.ApiInputs.Line;
import pe.bioleather.api.ApiInputs.StateInput;
import pe.bioleather.dao.PedidoDAO;
import pe.bioleather.dao.ProductoDAO;
import pe.bioleather.security.AuthSessionService.Session;
import static pe.bioleather.api.ApiValidation.*;

@Service
public class OrderService {
    private static final Logger log=LoggerFactory.getLogger(OrderService.class);
    private static final Set<String> ESTADOS=ImmutableSet.of("Recibido","En preparación","Cerrado","Cancelado");
    private final PedidoDAO pedidos;
    private final ProductoDAO productos;
    public OrderService(PedidoDAO pedidos, ProductoDAO productos){this.pedidos=pedidos;this.productos=productos;}
    private record Detail(long id,int quantity,BigDecimal price){}

    @Transactional
    public Map<String,Object> crear(Session s,OrderInput in){
        if(!"CLIENTE".equals(s.rol()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Usa una cuenta de cliente");
        if(in==null || in.lineas()==null || in.lineas().isEmpty() || in.lineas().size()>20)throw bad("Agrega de 1 a 20 líneas");
        List<Detail> details=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;
        for(Line line:in.lineas()){
            if(line==null || line.cantidad()<1 || line.cantidad()>99 || details.stream().anyMatch(d->d.id()==line.productoId()))throw bad("Cantidad o producto repetido inválido");
            var rows=productos.precioActivo(line.productoId());
            if(rows.isEmpty())throw bad("Producto no disponible");
            BigDecimal price=(BigDecimal)rows.get(0).get("precio");
            details.add(new Detail(line.productoId(),line.cantidad(),price));
            total=total.add(price.multiply(BigDecimal.valueOf(line.cantidad())));
        }
        long id=pedidos.insertarCabecera(s.id(),total);
        String code="PED-"+String.format("%06d",id);
        pedidos.asignarCodigo(id,code);
        for(Detail d:details)pedidos.insertarDetalle(id,d.id(),d.quantity(),d.price());
        pedidos.guardarHistorial(id,"Recibido",s.id());
        log.info("Pedido {} registrado con {} líneas",code,details.size());
        return Map.of("id",id,"codigo",code,"estado","Recibido","total",total);
    }
    private Map<String,Object> mapear(Map<String,Object> row){
        long id=((Number)row.get("id")).longValue();
        return Map.of("id",id,"codigo",row.get("codigo"),"estado",row.get("estado"),"total",row.get("total"),
            "creadoEn",row.get("creado_en").toString(),"cliente",row.get("nombre"),"lineas",pedidos.lineas(id));
    }
    public List<Map<String,Object>> mios(Session s){return pedidos.deUsuario(s.id()).stream().map(this::mapear).toList();}
    public Map<String,Object> porCodigo(Session s,String code){
        var rows=pedidos.deCodigo(code,s.id(),s.rol());
        if(rows.isEmpty())throw missing();
        return mapear(rows.get(0));
    }
    public List<Map<String,Object>> todos(){return pedidos.todos().stream().map(this::mapear).toList();}
    @Transactional
    public Map<String,Object> cambiarEstado(Session admin,long id,StateInput in){
        if(in==null || in.estado()==null || !ESTADOS.contains(in.estado()))throw bad("Estado inválido");
        if(pedidos.actualizarEstado(id,in.estado())==0)throw missing();
        pedidos.guardarHistorial(id,in.estado(),admin.id());
        log.info("Estado del pedido {} actualizado",id);
        return Map.of("mensaje","Estado actualizado");
    }
    public List<Map<String,Object>> historial(Session s,long id){
        if(!pedidos.tieneAcceso(id,s.id(),s.rol()))throw missing();
        return pedidos.historial(id);
    }
}
