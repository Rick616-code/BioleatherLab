package pe.bioleather.service;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import pe.bioleather.api.ApiInputs.ProductInput;
import pe.bioleather.dao.ProductoDAO;
import static pe.bioleather.api.ApiValidation.*;

@Service
public class ProductService {
    private static final Set<String> CATEGORIAS=ImmutableSet.of("Cartera","Billetera","Correa");
    private static final Set<String> IMAGENES=ImmutableSet.of("cartera.png","billetera.png","correa.png");
    private final ProductoDAO dao;
    public ProductService(ProductoDAO dao){this.dao=dao;}
    public List<Map<String,Object>> catalogo(String q,String cat){
        return dao.catalogo(q==null?"":q.strip().toLowerCase(Locale.ROOT),cat==null?"":cat.strip().toLowerCase(Locale.ROOT));
    }
    public Map<String,Object> detalle(long id){var rows=dao.detalleActivo(id);if(rows.isEmpty())throw missing();return rows.get(0);}
    public List<Map<String,Object>> todos(){return dao.listarTodos();}
    public Map<String,Object> crear(ProductInput in){dao.crear(validar(in));return Map.of("mensaje","Producto creado");}
    public Map<String,Object> editar(long id,ProductInput in){
        if(dao.actualizar(id,validar(in))==0)throw missing();
        return Map.of("mensaje","Producto actualizado");
    }
    private ProductInput validar(ProductInput p){
        if(p==null)throw bad("Datos del producto faltantes");
        String name=required(p.nombre(),120),cat=required(p.categoria(),30),desc=required(p.descripcion(),500);
        if(!CATEGORIAS.contains(cat))throw bad("Categoría inválida");
        if(p.precio()==null || p.precio().signum()<0 || p.precio().scale()>2)throw bad("Precio inválido");
        String img=required(p.imagen(),100);
        if(!IMAGENES.contains(img))throw bad("Imagen no permitida");
        return new ProductInput(name,cat,desc,p.precio(),img,p.activo());
    }
}
