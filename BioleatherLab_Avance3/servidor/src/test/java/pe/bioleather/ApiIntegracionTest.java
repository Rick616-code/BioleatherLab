package pe.bioleather;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.io.ByteArrayInputStream;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:bioleather_tests;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
    "spring.sql.init.mode=always",
    "ADMIN_EMAIL=admin-prueba@ejemplo.test",
    "ADMIN_PASSWORD=DemoPruebaUnica2026!"
})
class ApiIntegracionTest {
    @Autowired MockMvc mvc;

    @Test void catalogoPublicoTieneProductos() throws Exception {
        mvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").isNotEmpty());
    }

    @Test void requiereSesionParaPedidosYExportacion() throws Exception {
        mvc.perform(get("/api/orders/mine")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/reports/orders.xlsx")).andExpect(status().isUnauthorized());
    }

    @Test void registraClienteYNoLeDaAccesoAlReporte() throws Exception {
        String mail = "prueba-clientes@ejemplo.test";
        mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Cliente Prueba\",\"correo\":\"" + mail + "\",\"clave\":\"Segura123!\"}"))
            .andExpect(status().isOk());
        String loginJson = mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"correo\":\"" + mail + "\",\"clave\":\"Segura123!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn().getResponse().getContentAsString();
        String token = new com.fasterxml.jackson.databind.ObjectMapper().readTree(loginJson).get("token").asText();
        mvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content("{\"lineas\":[{\"productoId\":1,\"cantidad\":2}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").isNotEmpty());
        mvc.perform(get("/api/orders/mine").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].lineas[0].cantidad").value(2));
        mvc.perform(get("/api/admin/reports/orders.xlsx").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test void administradorPuedeDescargarExcel() throws Exception {
        String result = mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"correo\":\"admin-prueba@ejemplo.test\",\"clave\":\"DemoPruebaUnica2026!\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String token = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result).get("token").asText();
        byte[] excel = mvc.perform(get("/api/admin/reports/orders.xlsx").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andReturn().getResponse().getContentAsByteArray();
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(excel))) {
            org.junit.jupiter.api.Assertions.assertEquals("Código", workbook.getSheet("Pedidos").getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test void impideRegistroConCorreoInvalidoYClaveCorta() throws Exception {
        mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Persona Prueba\",\"correo\":\"no-es-correo\",\"clave\":\"Segura123!\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Persona Prueba\",\"correo\":\"prueba-clave@ejemplo.test\",\"clave\":\"123\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test void noPermiteAdministracionSinSesion() throws Exception {
        mvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/clients")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/orders")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/admin/orders/1/state")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"estado\":\"Cerrado\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test void clienteNoPuedeCambiarProductosNiEstadosDePedido() throws Exception {
        String mail="cliente-seguridad@ejemplo.test";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Cliente Seguridad\",\"correo\":\""+mail+"\",\"clave\":\"Segura123!\"}"))
            .andExpect(status().isOk());
        String result=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"correo\":\""+mail+"\",\"clave\":\"Segura123!\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token=new com.fasterxml.jackson.databind.ObjectMapper().readTree(result).get("token").asText();
        mvc.perform(get("/api/admin/products").header("Authorization","Bearer "+token))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/orders/1/state").header("Authorization","Bearer "+token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"estado\":\"Cerrado\"}"))
            .andExpect(status().isForbidden());
    }
    private String crearClienteYEntrar(String correo) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Cliente de prueba\",\"correo\":\"" + correo + "\",\"clave\":\"Segura123!\"}"))
            .andExpect(status().isOk());
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"correo\":\"" + correo + "\",\"clave\":\"Segura123!\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new com.fasterxml.jackson.databind.ObjectMapper().readTree(json).get("token").asText();
    }

    private String entrarAdministrador() throws Exception {
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"correo\":\"admin-prueba@ejemplo.test\",\"clave\":\"DemoPruebaUnica2026!\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new com.fasterxml.jackson.databind.ObjectMapper().readTree(json).get("token").asText();
    }

    @Test void cerrarSesionInvalidaElToken() throws Exception {
        String token = crearClienteYEntrar("logout-fase3@ejemplo.test");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test void unClienteNoPuedeVerPedidosDeOtro() throws Exception {
        String propio = crearClienteYEntrar("propietario-fase3@ejemplo.test");
        String otro = crearClienteYEntrar("segundo-fase3@ejemplo.test");
        String json = mvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + propio)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"lineas\":[{\"productoId\":1,\"cantidad\":1}]}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var pedido = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        String codigo = pedido.get("codigo").asText();
        long id = pedido.get("id").asLong();
        mvc.perform(get("/api/orders/code/{code}", codigo).header("Authorization", "Bearer " + otro))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/{id}/history", id).header("Authorization", "Bearer " + otro))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/{id}/history", id).header("Authorization", "Bearer " + propio))
            .andExpect(status().isOk());
    }

    @Test void desactivarUnClienteBloqueaSuSesionActual() throws Exception {
        String correo = "inactivo-fase3@ejemplo.test";
        String cliente = crearClienteYEntrar(correo);
        String info = mvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + cliente)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(info).get("id").asLong();
        String admin = entrarAdministrador();
        mvc.perform(put("/api/admin/clients/{id}", id)
            .header("Authorization", "Bearer " + admin)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Cliente inactivo\",\"correo\":\"" + correo + "\",\"activo\":false}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + cliente))
            .andExpect(status().isForbidden());
    }

    @Test void editarClienteConCorreoYaUsadoDaErrorDeValidacion() throws Exception {
        String correo1 = "correo1-fase3@ejemplo.test";
        String correo2 = "correo2-fase3@ejemplo.test";
        crearClienteYEntrar(correo1);
        String cliente2 = crearClienteYEntrar(correo2);
        String info = mvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + cliente2)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(info).get("id").asLong();
        String admin = entrarAdministrador();
        mvc.perform(put("/api/admin/clients/{id}", id)
            .header("Authorization", "Bearer " + admin)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Otro nombre\",\"correo\":\"" + correo1 + "\",\"activo\":true}"))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/api/admin/clients/{id}", id)
            .header("Authorization", "Bearer " + admin)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Otro nombre\",\"correo\":\"" + correo2 + "\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test void apiEnviaCabecerasBasicasDeSeguridad() throws Exception {
        mvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("X-Content-Type-Options", "nosniff"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("X-Frame-Options", "DENY"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("Cache-Control", "no-store"));
    }
}
