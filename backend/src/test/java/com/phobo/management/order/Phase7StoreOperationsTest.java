package com.phobo.management.order;

import com.phobo.management.common.enums.*;
import com.phobo.management.entity.*;
import com.phobo.management.exception.OrderException;
import com.phobo.management.kitchen.dto.KitchenQueueResponse;
import com.phobo.management.kitchen.service.KitchenQueueService;
import com.phobo.management.order.dto.OrderResponse;
import com.phobo.management.order.service.OrderAcceptanceService;
import com.phobo.management.order.service.OrderCompletionService;
import com.phobo.management.order.service.OrderStatusTransitionService;
import com.phobo.management.payment.service.PaymentService;
import com.phobo.management.pos.dto.PosOrderItemRequest;
import com.phobo.management.pos.dto.PosOrderRequest;
import com.phobo.management.pos.dto.PosOrderResponse;
import com.phobo.management.pos.dto.PosPaymentRequest;
import com.phobo.management.pos.service.PosOrderService;
import com.phobo.management.pos.service.PosPaymentService;
import com.phobo.management.repository.*;
import com.phobo.management.security.CustomUserPrincipal;
import com.phobo.management.security.EmployeeAuthorizationService;
import com.phobo.management.table.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class Phase7StoreOperationsTest {

    @Autowired
    private TableService tableService;
    @Autowired
    private PosOrderService posOrderService;
    @Autowired
    private PosPaymentService posPaymentService;
    @Autowired
    private KitchenQueueService kitchenQueueService;
    @Autowired
    private OrderAcceptanceService orderAcceptanceService;
    @Autowired
    private OrderCompletionService orderCompletionService;
    @Autowired
    private EmployeeAuthorizationService employeeAuthService;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;
    @Autowired
    private CustomerProfileRepository customerProfileRepository;
    @Autowired
    private RestaurantTableRepository tableRepository;
    @Autowired
    private TableSessionRepository tableSessionRepository;
    @Autowired
    private OrderEntityRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private KitchenQueueRepository kitchenQueueRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderStatusHistoryRepository historyRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoicePrintEventRepository invoicePrintEventRepository;
    @Autowired
    private com.phobo.management.payment.gateway.MockPaymentGateway mockPaymentGateway;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private RestaurantTable testTable;
    private Product testProduct;
    private CustomerProfile testCustomer;

    @BeforeEach
    public void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            // Clean up past test tables/sessions/orders to prevent unique constraint failures
            invoicePrintEventRepository.deleteAllInBatch();
            invoiceRepository.deleteAllInBatch();
            kitchenQueueRepository.deleteAllInBatch();
            paymentRepository.deleteAllInBatch();
            tableSessionRepository.deleteAllInBatch();
            historyRepository.deleteAllInBatch();
            orderRepository.deleteAllInBatch();
            tableRepository.deleteAllInBatch();

            // Set up restaurant table
            testTable = RestaurantTable.builder()
                    .id(UUID.randomUUID().toString())
                    .tableNumber("Table-" + UUID.randomUUID().toString().substring(0, 8))
                    .capacity(4)
                    .status(RestaurantTableStatus.AVAILABLE)
                    .build();
            testTable = tableRepository.save(testTable);

            // Set up product
            if (productRepository.findAll().isEmpty()) {
                testProduct = Product.builder()
                        .id(UUID.randomUUID().toString())
                        .productName("Phở Bò Tái Lăn")
                        .slug("pho-bo-tai-lan")
                        .basePrice(new BigDecimal("75000"))
                        .isAvailable(true)
                        .category(com.phobo.management.entity.Category.builder().id("1").categoryName("Pho").build())
                        .build();
                testProduct = productRepository.save(testProduct);
            } else {
                testProduct = productRepository.findAll().get(0);
            }

            // Set up customer profile
            testCustomer = customerProfileRepository.findAll().get(0);

            return null;
        });

        // Set default security context to employee with generic Staff position
        authenticateEmployee("legacystaff", "STAFF", "Staff");
    }

    private void authenticateEmployee(String username, String roleCode, String position) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        User user = transactionTemplate.execute(status -> {
            User u = userRepository.findByUsername(username).orElseGet(() -> {
                User newUser = User.builder()
                        .id(UUID.randomUUID().toString())
                        .username(username)
                        .email(username + "@example.com")
                        .phone("09" + String.format("%08d", Math.abs(username.hashCode()) % 100000000))
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                return userRepository.save(newUser);
            });

            EmployeeProfile ep = employeeProfileRepository.findByUserId(u.getId()).orElseGet(() -> {
                EmployeeProfile newEp = EmployeeProfile.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .fullName("Test Employee " + username)
                        .position(position)
                        .hireDate(LocalDateTime.now())
                        .build();
                return employeeProfileRepository.save(newEp);
            });
            // Update position in case it changed
            ep.setPosition(position);
            employeeProfileRepository.save(ep);

            return u;
        });

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testSessionPaymentPendingBlocksNewTableSession() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        
        // 1. Create a session on testTable with status PAYMENT_PENDING
        transactionTemplate.execute(status -> {
            tableService.openTable(testTable.getId());
            TableSession session = tableSessionRepository.findActiveSessionByTableId(testTable.getId()).orElseThrow();
            session.setStatus(TableSessionStatus.PAYMENT_PENDING);
            tableSessionRepository.save(session);
            
            RestaurantTable table = tableRepository.findById(testTable.getId()).orElseThrow();
            table.setStatus(RestaurantTableStatus.OCCUPIED);
            tableRepository.save(table);
            return null;
        });

        // 2. Try to open session on testTable again. Must throw ACTIVE_SESSION_EXISTS
        assertThrows(OrderException.class, () -> tableService.openTable(testTable.getId()));
    }

    @Test
    public void testTableSessionAlreadyHasOrderBlocksSecondPosOrder() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. Open table session
        transactionTemplate.execute(status -> {
            tableService.openTable(testTable.getId());
            return null;
        });

        // 2. Create POS order for the table
        PosOrderRequest orderReq = PosOrderRequest.builder()
                .tableId(testTable.getId())
                .paymentMethod(PaymentMethod.CASH)
                .discountAmount(BigDecimal.ZERO)
                .items(List.of(PosOrderItemRequest.builder()
                        .productId(testProduct.getId())
                        .quantity(1)
                        .build()))
                .build();
        String idempotencyKey1 = UUID.randomUUID().toString();
        PosOrderResponse orderRes = posOrderService.createOrder(orderReq, idempotencyKey1);
        assertNotNull(orderRes.getOrderId());

        // 3. Try to place a second order on the same active table session. Must throw ACTIVE_ORDER_EXISTS_ON_SESSION
        String idempotencyKey2 = UUID.randomUUID().toString();
        assertThrows(OrderException.class, () -> posOrderService.createOrder(orderReq, idempotencyKey2));
    }

    @Test
    public void testPaymentBeforeServeCompletesWhenServed() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. Place order
        transactionTemplate.execute(status -> {
            tableService.openTable(testTable.getId());
            return null;
        });

        PosOrderRequest orderReq = PosOrderRequest.builder()
                .tableId(testTable.getId())
                .paymentMethod(PaymentMethod.CASH)
                .discountAmount(BigDecimal.ZERO)
                .items(List.of(PosOrderItemRequest.builder()
                        .productId(testProduct.getId())
                        .quantity(1)
                        .build()))
                .build();
        PosOrderResponse orderRes = posOrderService.createOrder(orderReq, UUID.randomUUID().toString());

        // 2. Payment SUCCESS
        PosPaymentRequest payReq = PosPaymentRequest.builder()
                .cashReceived(new BigDecimal("100000"))
                .build();
        posPaymentService.processPosPayment(orderRes.getOrderId(), payReq, UUID.randomUUID().toString());

        OrderEntity order = orderRepository.findById(orderRes.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.DANG_CHE_BIEN, order.getStatus()); // remains DANG_CHE_BIEN (cooking)

        // 3. Cook all items
        transactionTemplate.execute(status -> {
            List<KitchenQueue> items = kitchenQueueRepository.findAll();
            for (KitchenQueue k : items) {
                kitchenQueueService.updateItemStatus(k.getId(), KitchenItemStatus.DANG_NAU, k.getVersion());
                kitchenQueueService.updateItemStatus(k.getId(), KitchenItemStatus.DA_XONG, k.getVersion() + 1);
            }
            return null;
        });

        // 4. Serve order
        orderAcceptanceService.serveOrder(orderRes.getOrderId());

        OrderEntity finishedOrder = orderRepository.findById(orderRes.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.HOAN_THANH, finishedOrder.getStatus()); // completes to HOAN_THANH
        
        RestaurantTable table = tableRepository.findById(testTable.getId()).orElseThrow();
        assertEquals(RestaurantTableStatus.CLEANING, table.getStatus()); // table is CLEANING
    }

    @Test
    public void testServeBeforePaymentCompletesWhenPaid() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. Place order
        transactionTemplate.execute(status -> {
            tableService.openTable(testTable.getId());
            return null;
        });

        PosOrderRequest orderReq = PosOrderRequest.builder()
                .tableId(testTable.getId())
                .paymentMethod(PaymentMethod.CASH)
                .discountAmount(BigDecimal.ZERO)
                .items(List.of(PosOrderItemRequest.builder()
                        .productId(testProduct.getId())
                        .quantity(1)
                        .build()))
                .build();
        PosOrderResponse orderRes = posOrderService.createOrder(orderReq, UUID.randomUUID().toString());

        // 2. Cook all items
        transactionTemplate.execute(status -> {
            List<KitchenQueue> items = kitchenQueueRepository.findAll();
            for (KitchenQueue k : items) {
                kitchenQueueService.updateItemStatus(k.getId(), KitchenItemStatus.DANG_NAU, k.getVersion());
                kitchenQueueService.updateItemStatus(k.getId(), KitchenItemStatus.DA_XONG, k.getVersion() + 1);
            }
            return null;
        });

        // 3. Serve order
        orderAcceptanceService.serveOrder(orderRes.getOrderId());

        OrderEntity order = orderRepository.findById(orderRes.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.DANG_PHUC_VU, order.getStatus()); // transitions to DANG_PHUC_VU

        // 4. Payment SUCCESS
        PosPaymentRequest payReq = PosPaymentRequest.builder()
                .cashReceived(new BigDecimal("100000"))
                .build();
        posPaymentService.processPosPayment(orderRes.getOrderId(), payReq, UUID.randomUUID().toString());

        OrderEntity finishedOrder = orderRepository.findById(orderRes.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.HOAN_THANH, finishedOrder.getStatus()); // completes to HOAN_THANH
    }

    @Test
    public void testPosOrderHasInitialHistoryDangCheBien() {
        PosOrderRequest orderReq = PosOrderRequest.builder()
                .paymentMethod(PaymentMethod.CASH)
                .discountAmount(BigDecimal.ZERO)
                .items(List.of(PosOrderItemRequest.builder()
                        .productId(testProduct.getId())
                        .quantity(1)
                        .build()))
                .build();
        PosOrderResponse orderRes = posOrderService.createOrder(orderReq, UUID.randomUUID().toString());

        List<OrderStatusHistory> history = historyRepository.findByOrderIdOrderByCreatedAtAsc(orderRes.getOrderId());
        assertFalse(history.isEmpty());
        
        OrderStatusHistory first = history.get(0);
        assertNull(first.getPreviousStatus());
        assertEquals(OrderStatus.DANG_CHE_BIEN, first.getNewStatus());
        assertEquals("STAFF", first.getChangeSource());
    }

    @Test
    public void testEmployeePositionSecurityRestrictions() {
        // Test KITCHEN role
        authenticateEmployee("kitchenstaff", "STAFF", "KITCHEN");
        assertTrue(employeeAuthService.isKitchen());
        assertFalse(employeeAuthService.isCashier());
        assertFalse(employeeAuthService.isWaiter());

        // Test CASHIER role
        authenticateEmployee("cashierstaff", "STAFF", "CASHIER");
        assertFalse(employeeAuthService.isKitchen());
        assertTrue(employeeAuthService.isCashier());
        assertFalse(employeeAuthService.isWaiter());

        // Test WAITER role
        authenticateEmployee("waiterstaff", "STAFF", "WAITER");
        assertFalse(employeeAuthService.isKitchen());
        assertFalse(employeeAuthService.isCashier());
        assertTrue(employeeAuthService.isWaiter());

        // Test Legacy Fallback "Staff" (should have all staff privileges for backward compatibility)
        authenticateEmployee("legacystaff", "STAFF", "Staff");
        assertTrue(employeeAuthService.isKitchen());
        assertTrue(employeeAuthService.isCashier());
        assertTrue(employeeAuthService.isWaiter());

        // Test Manager (has all operations)
        authenticateEmployee("manager", "MANAGER", "Store Manager");
        assertTrue(employeeAuthService.isKitchen());
        assertTrue(employeeAuthService.isCashier());
        assertTrue(employeeAuthService.isWaiter());
        assertTrue(employeeAuthService.isManager());

        // Test Admin (has no operational privileges by default)
        authenticateEmployee("admin", "ADMIN", "Administrator");
        assertFalse(employeeAuthService.isKitchen());
        assertFalse(employeeAuthService.isCashier());
        assertFalse(employeeAuthService.isWaiter());
        assertFalse(employeeAuthService.isManager());
    }

    @Test
    public void testOnlinePaymentSuccessTriggersKitchenQueueExactlyOnce() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. Create ONLINE gateway order waiting payment
        OrderEntity order = transactionTemplate.execute(status -> {
            OrderEntity o = OrderEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .customer(testCustomer)
                    .orderCode("TEST-ONP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                    .paymentMethod(PaymentMethod.ONLINE_GATEWAY)
                    .orderType(OrderType.ONLINE)
                    .totalAmount(new BigDecimal("75000"))
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(new BigDecimal("75000"))
                    .status(OrderStatus.CHO_XAC_NHAN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            OrderEntity saved = orderRepository.save(o);

            Payment payment = Payment.builder()
                    .id(UUID.randomUUID().toString())
                    .order(saved)
                    .paymentMethod(PaymentMethod.ONLINE_GATEWAY)
                    .paymentStatus(PaymentStatus.PENDING)
                    .amount(new BigDecimal("75000"))
                    .provider("MOCK")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);
            
            OrderItem item = OrderItem.builder()
                    .id(UUID.randomUUID().toString())
                    .order(saved)
                    .product(testProduct)
                    .productNameSnapshot(testProduct.getProductName())
                    .basePriceSnapshot(testProduct.getBasePrice())
                    .optionsPriceSnapshot(BigDecimal.ZERO)
                    .unitPriceSnapshot(testProduct.getBasePrice())
                    .priceAtOrder(testProduct.getBasePrice())
                    .lineTotal(testProduct.getBasePrice())
                    .quantity(1)
                    .build();
            saved.setItems(new ArrayList<>(List.of(item)));

            return orderRepository.save(saved);
        });

        // 2. Perform payment callback SUCCESS (first time)
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("paymentId", payment.getId());
        queryParams.put("orderId", order.getId());
        queryParams.put("amount", "75000.00");
        queryParams.put("status", "SUCCESS");
        String sig = mockPaymentGateway.generateSignature("75000.00", order.getId(), payment.getId(), "SUCCESS");
        queryParams.put("signature", sig);

        // Authenticate client
        authenticateEmployee("webhook", "STAFF", "Staff");
        paymentService.processCallback("MOCK", queryParams);

        long count1 = kitchenQueueRepository.countItemsInQueue(order.getId());
        assertEquals(1, count1); // kitchen queue item is generated

        // 3. Repeat callback (idempotent duplicate)
        paymentService.processCallback("MOCK", queryParams);

        long count2 = kitchenQueueRepository.countItemsInQueue(order.getId());
        assertEquals(1, count2); // remains exactly one kitchen queue item
    }
}
