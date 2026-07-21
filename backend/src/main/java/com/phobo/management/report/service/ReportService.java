package com.phobo.management.report.service;

import com.phobo.management.exception.AppException;
import com.phobo.management.report.dto.RevenueOverviewResponse;
import com.phobo.management.report.dto.RevenueSeriesPoint;
import com.phobo.management.report.dto.TopProductReport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public RevenueOverviewResponse getRevenueOverview(LocalDateTime from, LocalDateTime to, String grouping) {
        if (from == null) from = LocalDateTime.now().minusDays(30);
        if (to == null) to = LocalDateTime.now();

        if (from.isAfter(to)) {
            throw new AppException("Thời gian từ ngày phải trước đến ngày", "INVALID_REPORT_DATE_RANGE");
        }

        // Aggregate totals for Payment SUCCESS and order NOT HUY_BO / CANCELED
        String totalsSql = "SELECT " +
                "COALESCE(SUM(o.total_amount), 0) as gross, " +
                "COALESCE(SUM(o.discount_amount), 0) as discount, " +
                "COALESCE(SUM(o.final_amount), 0) as net, " +
                "COUNT(o.id) as orderCount " +
                "FROM orders o " +
                "JOIN payments p ON p.order_id = o.id " +
                "WHERE p.payment_status = 'SUCCESS' AND o.status NOT IN ('DA_HUY', 'HUY_BO') " +
                "AND o.created_at >= :from AND o.created_at <= :to";

        Query totalsQuery = entityManager.createNativeQuery(totalsSql);
        totalsQuery.setParameter("from", from);
        totalsQuery.setParameter("to", to);

        Object[] totalsRow = (Object[]) totalsQuery.getSingleResult();
        BigDecimal grossRevenue = BigDecimal.valueOf(((Number) totalsRow[0]).doubleValue());
        BigDecimal discountAmount = BigDecimal.valueOf(((Number) totalsRow[1]).doubleValue());
        BigDecimal netRevenue = BigDecimal.valueOf(((Number) totalsRow[2]).doubleValue());
        Long orderCount = ((Number) totalsRow[3]).longValue();

        BigDecimal averageOrderValue = orderCount > 0
                ? netRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Series grouped by period
        String dateFormatPattern = "%Y-%m-%d";
        if ("MONTH".equalsIgnoreCase(grouping)) {
            dateFormatPattern = "%Y-%m";
        } else if ("WEEK".equalsIgnoreCase(grouping)) {
            dateFormatPattern = "%x-%v";
        }

        String seriesSql = "SELECT " +
                "DATE_FORMAT(o.created_at, '" + dateFormatPattern + "') as period, " +
                "COALESCE(SUM(o.final_amount), 0) as revenue, " +
                "COUNT(o.id) as orderCount " +
                "FROM orders o " +
                "JOIN payments p ON p.order_id = o.id " +
                "WHERE p.payment_status = 'SUCCESS' AND o.status NOT IN ('DA_HUY', 'HUY_BO') " +
                "AND o.created_at >= :from AND o.created_at <= :to " +
                "GROUP BY 1 ORDER BY 1 ASC";

        Query seriesQuery = entityManager.createNativeQuery(seriesSql);
        seriesQuery.setParameter("from", from);
        seriesQuery.setParameter("to", to);

        List<?> seriesRows = seriesQuery.getResultList();
        List<RevenueSeriesPoint> series = new ArrayList<>();
        for (Object rowObj : seriesRows) {
            Object[] row = (Object[]) rowObj;
            series.add(RevenueSeriesPoint.builder()
                    .period((String) row[0])
                    .revenue(BigDecimal.valueOf(((Number) row[1]).doubleValue()))
                    .orderCount(((Number) row[2]).longValue())
                    .build());
        }

        // Top Selling Products
        String topProductsSql = "SELECT " +
                "pr.id, pr.product_name, " +
                "COALESCE(SUM(oi.quantity), 0) as qty, " +
                "COALESCE(SUM(oi.quantity * oi.price_at_order), 0) as totalRev " +
                "FROM order_items oi " +
                "JOIN products pr ON oi.product_id = pr.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "JOIN payments p ON p.order_id = o.id " +
                "WHERE p.payment_status = 'SUCCESS' AND o.status NOT IN ('DA_HUY', 'HUY_BO') " +
                "AND o.created_at >= :from AND o.created_at <= :to " +
                "GROUP BY pr.id, pr.product_name ORDER BY qty DESC LIMIT 10";

        Query topProductsQuery = entityManager.createNativeQuery(topProductsSql);
        topProductsQuery.setParameter("from", from);
        topProductsQuery.setParameter("to", to);

        List<?> topRows = topProductsQuery.getResultList();
        List<TopProductReport> topProducts = new ArrayList<>();
        for (Object rowObj : topRows) {
            Object[] row = (Object[]) rowObj;
            topProducts.add(TopProductReport.builder()
                    .productId((String) row[0])
                    .productName((String) row[1])
                    .totalQuantity(((Number) row[2]).longValue())
                    .totalRevenue(BigDecimal.valueOf(((Number) row[3]).doubleValue()))
                    .build());
        }

        return RevenueOverviewResponse.builder()
                .grossRevenue(grossRevenue)
                .discountAmount(discountAmount)
                .netRevenue(netRevenue)
                .orderCount(orderCount)
                .averageOrderValue(averageOrderValue)
                .series(series)
                .topProducts(topProducts)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportRevenueCsv(LocalDateTime from, LocalDateTime to) {
        RevenueOverviewResponse data = getRevenueOverview(from, to, "DAY");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            // Write UTF-8 BOM for Excel compatibility with Vietnamese text
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            writer.write("BAO CAO DOANH THU CUA HANG PHO BO\n");
            writer.write("Tu ngay: " + (from != null ? from.format(DateTimeFormatter.ISO_LOCAL_DATE) : "") + "\n");
            writer.write("Den ngay: " + (to != null ? to.format(DateTimeFormatter.ISO_LOCAL_DATE) : "") + "\n\n");

            writer.write("Tong doanh thu gop," + escapeCsv(data.getGrossRevenue().toString()) + "\n");
            writer.write("Tong giam gia," + escapeCsv(data.getDiscountAmount().toString()) + "\n");
            writer.write("Doanh thu thuc te," + escapeCsv(data.getNetRevenue().toString()) + "\n");
            writer.write("Tong so don," + escapeCsv(data.getOrderCount().toString()) + "\n");
            writer.write("Gia tri don trung binh," + escapeCsv(data.getAverageOrderValue().toString()) + "\n\n");

            writer.write("Ngay/Ky,Doanh Thu,So Don\n");
            for (RevenueSeriesPoint point : data.getSeries()) {
                writer.write(escapeCsv(point.getPeriod()) + "," +
                        escapeCsv(point.getRevenue().toString()) + "," +
                        escapeCsv(point.getOrderCount().toString()) + "\n");
            }

            writer.write("\nTop San Pham Ban Chay,So Luong,Tong Doanh Thu\n");
            for (TopProductReport prod : data.getTopProducts()) {
                writer.write(escapeCsv(prod.getProductName()) + "," +
                        escapeCsv(prod.getTotalQuantity().toString()) + "," +
                        escapeCsv(prod.getTotalRevenue().toString()) + "\n");
            }
            writer.flush();
        } catch (Exception e) {
            log.error("Loi khi xuat CSV: {}", e.getMessage(), e);
            throw new AppException("Loi khi xuat bao cao CSV", "CSV_EXPORT_ERROR");
        }

        return out.toByteArray();
    }

    /**
     * Prevents CSV Formula Injection by prefixing values starting with =, +, -, @ with a single quote.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        String str = value.trim();
        if (str.startsWith("=") || str.startsWith("+") || str.startsWith("-") || str.startsWith("@")) {
            str = "'" + str;
        }
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            str = "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}
