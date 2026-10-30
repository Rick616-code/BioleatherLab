package pe.bioleather.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import pe.bioleather.api.ApiInputs.ClientInput;
import pe.bioleather.security.AuthSessionService;
import pe.bioleather.service.ClientService;

@RestController
@RequestMapping("/api/admin/clients")
public class ClientController {
    private final ClientService service;
    private final AuthSessionService sessions;
    public ClientController(ClientService service,AuthSessionService sessions){this.service=service;this.sessions=sessions;}
    @GetMapping public List<Map<String,Object>> clients(HttpServletRequest request){sessions.requireAdmin(request);return service.listar();}
    @PutMapping("/{id}") public Map<String,Object> editClient(HttpServletRequest request,@PathVariable long id,@RequestBody ClientInput input){sessions.requireAdmin(request);return service.editar(id,input);}
}
