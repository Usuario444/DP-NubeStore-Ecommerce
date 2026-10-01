package com.utp.nubestore.controller;

import com.utp.nubestore.dto.request.FiltroProductoRequest;
import com.utp.nubestore.dto.request.PublicarProductoRequest;
import com.utp.nubestore.dto.response.ProductoResponse;
import com.utp.nubestore.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * GRASP Controlador para el catálogo de productos.
 * Cliente: buscar y consultar. Vendedor: publicar (único caso de uso del Avance 1).
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /** GET /api/productos?texto=&categoria=&precioMin=&precioMax=&pagina=&tamanio= */
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> buscar(
            @RequestParam(name = "texto", required = false) String texto,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "precioMin", required = false) BigDecimal precioMin,
            @RequestParam(name = "precioMax", required = false) BigDecimal precioMax,
            @RequestParam(name = "pagina", required = false) Integer pagina,
            @RequestParam(name = "tamanio", required = false) Integer tamanio) {

        FiltroProductoRequest filtro =
                new FiltroProductoRequest(texto, categoria, precioMin, precioMax, pagina, tamanio);
        return ResponseEntity.ok(productoService.buscar(filtro));
    }

    /** GET /api/productos/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable("id") int id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    /** POST /api/productos -> 201 Created (publicación por un Vendedor) */
    @PostMapping
    public ResponseEntity<ProductoResponse> publicar(@RequestBody PublicarProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.publicar(request));
    }
}
