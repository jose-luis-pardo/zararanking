package com.inditex.zboost.service;

import com.inditex.zboost.entity.ReportSummary;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReportSummaryServiceImpl implements ReportSummaryService {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private ProductService productService;

    public ReportSummaryServiceImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReportSummary reportSummary() {
        /**
         * TODO: EJERCICIO 3. Reporte sumarizado
         */

        String sql = """
                SELECT count(distinct p.id) as totalProducts, count(distinct o.id) as totalOrders, sum(r.quantity*p.price) as totalSales 
                FROM (orders o join order_items r on o.id = i.order_id) join products p on p.id = r.product_id
            """;

        ReportSummary reportSummary = jdbcTemplate.queryForObject(sql, Map.of(), new BeanPropertyRowMapper<>(ReportSummary.class));

        String totalProductsByCategorySql = "SELECT category, count(*) as count FROM products GROUP BY category";
        Map<String, Integer> totalProductsByCategory = new HashMap<>();
        jdbcTemplate.query(totalProductsByCategorySql, rs -> {
            totalProductsByCategory.put(rs.getString("category"), rs.getInt("count"));
        });

        reportSummary.setTotalProductsByCategory(totalProductsByCategory);
        return reportSummary;
    }
}
