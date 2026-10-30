package pe.bioleather.service;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import pe.bioleather.api.ApiInputs.ClientInput;
import pe.bioleather.dao.UsuarioDAO;
import static pe.bioleather.api.ApiValidation.*;

@Service
public class ClientService {
    private final UsuarioDAO dao;
    public ClientService(UsuarioDAO dao) {this.dao=dao;}
    public List<Map<String,Object>> listar() {return dao.listarClientes();}
    public Map<String,Object> editar(long id,ClientInput in){
        if(in==null)throw bad("Datos del cliente faltantes");
        if(in.activo()==null)throw bad("Indica si el cliente está activo");
        String nombre = required(in.nombre(),120);
        String correo = email(in.correo());
        try {
            if(dao.modificarCliente(id,nombre,correo,in.activo())==0)throw missing();
        } catch (DuplicateKeyException ex) {
            throw bad("El correo ya está registrado por otro usuario");
        }
        return Map.of("mensaje","Cliente actualizado");
    }
}
