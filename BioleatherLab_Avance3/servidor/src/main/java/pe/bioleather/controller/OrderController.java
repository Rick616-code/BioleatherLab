package pe.bioleather.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import pe.bioleather.api.ApiInputs.OrderInput;
import pe.bioleather.api.ApiInputs.StateInput;
import pe.bioleather.security.AuthSessionService;
import pe.bioleather.service.OrderService;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService service;
    private final AuthSessionService sessions;
    public OrderController(OrderService service,AuthSessionService sessions){this.service=service;this.sessions=sessions;}
    @PostMapping("/orders") public Map<String,Object> createOrder(HttpServletRequest request,@RequestBody OrderInput input){return service.crear(sessions.requireUser(request),input);}
    @GetMapping("/orders/mine") public List<Map<String,Object>> mine(HttpServletRequest request){return service.mios(sessions.requireUser(request));}
    @GetMapping("/orders/code/{code}") public Map<String,Object> byCode(HttpServletRequest request,@PathVariable String code){return service.porCodigo(sessions.requireUser(request),code);}
    @GetMapping("/admin/orders") public List<Map<String,Object>> allOrders(HttpServletRequest request){sessions.requireAdmin(request);return service.todos();}
    @PutMapping("/admin/orders/{id}/state") public Map<String,Object> setState(HttpServletRequest request,@PathVariable long id,@RequestBody StateInput input){return service.cambiarEstado(sessions.requireAdmin(request),id,input);}
    @GetMapping("/orders/{id}/history") public List<Map<String,Object>> history(HttpServletRequest request,@PathVariable long id){return service.historial(sessions.requireUser(request),id);}
}
