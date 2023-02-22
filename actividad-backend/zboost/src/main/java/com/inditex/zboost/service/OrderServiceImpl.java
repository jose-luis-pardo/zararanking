package com.inditex.zboost.service;

import com.inditex.zboost.entity.Order;
import com.inditex.zboost.entity.OrderDetail;
import com.inditex.zboost.entity.ProductOrderItem;
import com.inditex.zboost.exception.InvalidParameterException;
import com.inditex.zboost.exception.NotFoundException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public OrderServiceImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Order> findOrders(int limit) {
        /**
         * TODO: EJERCICIO 2.a) Recupera un listado de los ultimos N pedidos (recuerda ordenar por fecha)
         */

        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        if(limit<1 || limit>100)
            throw new InvalidParameterException("limit", "El parámetro debe estar entre 1 y 100");
        String sql = "Select id, date, status,  FROM orders ORDER BY date DESC LIMIT :limit ";

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Order.class));
    }

    @Override
    public List<Order> findOrdersBetweenDates(Date fromDate, Date toDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", new java.sql.Date(fromDate.getTime()));
        params.put("toDate", new java.sql.Date(toDate.getTime()));
        String sql = """
                SELECT id, date, status
                FROM Orders 
                WHERE date BETWEEN :startDate AND :toDate
                """;

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Order.class));
    }

    @Override
    public OrderDetail findOrderDetail(long orderId) {
        /**
         * TODO: EJERCICIO 2.b) Recupera los detalles de un pedido dado su ID
         *
         * Recuerda que, si un pedido no es encontrado por su ID, debes notificarlo debidamente como se recoge en el contrato
         * que estas implementando (codigo de estado HTTP 404 Not Found). Para ello puedes usar la excepcion {@link com.inditex.zboost.exception.NotFoundException}
         *
         */

        // Escribe la query para recuperar la entidad OrderDetail por ID
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        OrderDetail orderDetail = new OrderDetail();
        String orderSQL = "SELECT * FROM ORDERS where id=:orderId";
        List <Order> orders = jdbcTemplate.query(orderSQL, params, new BeanPropertyRowMapper<>(Order.class));
        if(orders.size()<1)
            throw new NotFoundException("orderId", "El pedido no existe.");

        Order order = orders.get(0);


        // Una vez has conseguido recuperar los detalles del pedido, faltaria recuperar los productos que forman parte de el...
        String productOrdersSql = "select p.id, p.name, p.price, p.category, p.image_url, oit.quantity FROM PRODUCTS p " +
                "join order_items oit on p.id=oit.product_id where oit.order_id=:orderId";
        List<ProductOrderItem> products = jdbcTemplate.query(productOrdersSql, params, new BeanPropertyRowMapper<>(ProductOrderItem.class));
        orderDetail.setItemsCount(products.size());
        orderDetail.setDate(order.getDate());
        orderDetail.setStatus(order.getStatus());
        orderDetail.setId(order.getId());
        Double totalPrice = 0d;
        for(ProductOrderItem poi : products)
            totalPrice+=poi.getPrice()*poi.getQuantity();
        orderDetail.setTotalPrice(totalPrice);
        orderDetail.setProducts(products);
        return orderDetail;
    }
}
