package pe.bioleather.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import pe.bioleather.api.ApiInputs.Credentials;
import pe.bioleather.api.ApiInputs.Registration;
import pe.bioleather.security.AuthSessionService;
import pe.bioleather.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final AuthSessionService sessions;
    public AuthController(AuthService service,AuthSessionService sessions){this.service=service;this.sessions=sessions;}
    @PostMapping("/register") public Map<String,Object> register(@RequestBody Registration in){return service.registrar(in);}
    @PostMapping("/login") public Map<String,Object> login(@RequestBody Credentials in){return service.ingresar(in);}
    @PostMapping("/logout") public Map<String,String> logout(HttpServletRequest request){
        sessions.logout(request);return Map.of("mensaje","Sesión cerrada");
    }
    @GetMapping("/me") public Map<String,Object> me(HttpServletRequest request){return service.perfil(sessions.requireUser(request));}
}
