package pe.bioleather.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import pe.bioleather.api.ApiInputs.ProductInput;
import pe.bioleather.security.AuthSessionService;
import pe.bioleather.service.ProductService;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService service;
    private final AuthSessionService sessions;
    public ProductController(ProductService service,AuthSessionService sessions){this.service=service;this.sessions=sessions;}
    @GetMapping("/products") public List<Map<String,Object>> products(@RequestParam(required=false)String q,@RequestParam(required=false)String categoria){return service.catalogo(q,categoria);}
    @GetMapping("/products/{id}") public Map<String,Object> product(@PathVariable long id){return service.detalle(id);}
    @GetMapping("/admin/products") public List<Map<String,Object>> allProducts(HttpServletRequest request){sessions.requireAdmin(request);return service.todos();}
    @PostMapping("/admin/products") public Map<String,Object> addProduct(HttpServletRequest request,@RequestBody ProductInput input){sessions.requireAdmin(request);return service.crear(input);}
    @PutMapping("/admin/products/{id}") public Map<String,Object> editProduct(HttpServletRequest request,@PathVariable long id,@RequestBody ProductInput input){sessions.requireAdmin(request);return service.editar(id,input);}
}
