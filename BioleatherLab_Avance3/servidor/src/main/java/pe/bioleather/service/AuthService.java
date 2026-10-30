package pe.bioleather.service;

import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.bioleather.api.ApiInputs.Credentials;
import pe.bioleather.api.ApiInputs.Registration;
import pe.bioleather.dao.UsuarioDAO;
import pe.bioleather.security.AuthSessionService;
import pe.bioleather.security.AuthSessionService.Session;
import static pe.bioleather.api.ApiValidation.*;

@Service
public class AuthService {
    private final UsuarioDAO dao;
    private final AuthSessionService sessions;
    private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
    public AuthService(UsuarioDAO dao,AuthSessionService sessions) {this.dao=dao;this.sessions=sessions;}

    @Transactional
    public Map<String,Object> registrar(Registration in) {
        if(in==null)throw bad("Datos de registro faltantes");
        String nombre=required(in.nombre(),120), correo=email(in.correo());
        if(in.clave()==null || in.clave().length()<8 || in.clave().length()>72)throw bad("La contraseña requiere entre 8 y 72 caracteres");
        if(dao.existeCorreo(correo))throw bad("Correo ya registrado");
        try {dao.registrarCliente(nombre,correo,encoder.encode(in.clave()));}
        catch(DuplicateKeyException ex){throw bad("Correo ya registrado");}
        return Map.of("mensaje","Cuenta creada. Ya puedes iniciar sesión.");
    }
    public Map<String,Object> ingresar(Credentials in) {
        if(in==null)throw bad("Credenciales faltantes");
        var rows=dao.buscarCredenciales(email(in.correo()));
        if(rows.isEmpty() || in.clave()==null || !Boolean.TRUE.equals(rows.get(0).get("activo")) ||
            !encoder.matches(in.clave(),(String)rows.get(0).get("clave_hash")))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales incorrectas");
        var row=rows.get(0);
        var issued=sessions.open(((Number)row.get("id")).longValue(),(String)row.get("rol"));
        return Map.of("token",issued.token(),"usuario",dao.identidad(issued.session().id()));
    }
    public Map<String,Object> perfil(Session s) {return dao.identidad(s.id());}
}
