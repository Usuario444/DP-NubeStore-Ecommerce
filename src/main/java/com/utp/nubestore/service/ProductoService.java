package com.utp.nubestore.service;

import com.utp.nubestore.dao.ProductoDAO;
import com.utp.nubestore.dao.factory.DAOFactory;
import com.utp.nubestore.dto.request.FiltroProductoRequest;
import com.utp.nubestore.dto.request.PublicarProductoRequest;
import com.utp.nubestore.dto.response.ProductoResponse;
import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.model.Producto;
import com.utp.nubestore.util.Validador;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lógica de negocio de productos: publicación (Vendedor) y búsqueda (Cliente).
 * Depende de la interfaz ProductoDAO, obtenida mediante DAOFactory.
 */
@Service
public class ProductoService {

    private static final int TAMANIO_POR_DEFECTO = 20;
    private static final int TAMANIO_MAXIMO = 100;
    private static final int PAGINA_MAXIMA = 10_000;
    private static final int STOCK_MAXIMO = 1_000_000;

    private final ProductoDAO productoDAO;

    /** Constructor usado por Spring: obtiene el DAO desde la fábrica. */
    public ProductoService() {
        this(DAOFactory.getProductoDAO());
    }

    /** Constructor alternativo (por ejemplo, para pruebas con un DAO simulado). */
    public ProductoService(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    public ProductoResponse publicar(PublicarProductoRequest req) {
        if (req == null) {
            throw ApiException.badRequest("El cuerpo de la solicitud es obligatorio");
        }
        Producto producto = new Producto();
        producto.setIdVendedor(Validador.enteroPositivo(req.idVendedor(), "idVendedor"));
        producto.setNombre(Validador.texto(req.nombre(), "nombre", 150));
        producto.setDescripcion(Validador.textoOpcional(req.descripcion(), "descripcion", 2000));
        producto.setCategoria(Validador.texto(req.categoria(), "categoria", 80));
        producto.setPrecio(Validador.monto(req.precio(), "precio"));
        producto.setStock(Validador.entero(req.stock(), "stock", 0, STOCK_MAXIMO));
        producto.setImagenUrl(Validador.urlOpcional(req.imagenUrl(), "imagenUrl"));
        producto.setActivo(true);

        return ProductoResponse.desde(productoDAO.insertar(producto));
    }

    public List<ProductoResponse> buscar(FiltroProductoRequest filtro) {
        FiltroProductoRequest f = filtro != null
                ? filtro
                : new FiltroProductoRequest(null, null, null, null, null, null);

        String texto = Validador.textoOpcional(f.texto(), "texto", 100);
        String categoria = Validador.textoOpcional(f.categoria(), "categoria", 80);

        BigDecimal precioMin = f.precioMin();
        BigDecimal precioMax = f.precioMax();
        if (precioMin != null && precioMin.signum() < 0) {
            throw ApiException.badRequest("El campo 'precioMin' no puede ser negativo");
        }
        if (precioMax != null && precioMax.signum() < 0) {
            throw ApiException.badRequest("El campo 'precioMax' no puede ser negativo");
        }
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw ApiException.badRequest("'precioMin' no puede ser mayor que 'precioMax'");
        }

        int pagina = f.pagina() == null ? 1 : Validador.entero(f.pagina(), "pagina", 1, PAGINA_MAXIMA);
        int tamanio = f.tamanio() == null
                ? TAMANIO_POR_DEFECTO
                : Validador.entero(f.tamanio(), "tamanio", 1, TAMANIO_MAXIMO);
        int offset = (pagina - 1) * tamanio;

        return productoDAO.buscar(texto, categoria, precioMin, precioMax, tamanio, offset)
                .stream()
                .map(ProductoResponse::desde)
                .toList();
    }

    public ProductoResponse obtenerPorId(int idProducto) {
        Producto producto = productoDAO.buscarPorId(idProducto)
                .filter(Producto::isActivo)
                .orElseThrow(() -> ApiException.notFound("El producto no existe"));
        return ProductoResponse.desde(producto);
    }
}
