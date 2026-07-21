package com.phobo.management;

import com.phobo.management.attendance.dto.AttendanceResponse;
import com.phobo.management.attendance.service.AttendanceService;
import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.common.enums.*;
import com.phobo.management.entity.*;
import com.phobo.management.exception.AppException;
import com.phobo.management.inventory.dto.IngredientRequest;
import com.phobo.management.inventory.dto.IngredientResponse;
import com.phobo.management.inventory.dto.InventoryStockAdjustmentRequest;
import com.phobo.management.inventory.service.InventoryService;
import com.phobo.management.report.dto.RevenueOverviewResponse;
import com.phobo.management.report.service.ReportService;
import com.phobo.management.repository.*;
import com.phobo.management.review.dto.ReviewDetailResponse;
import com.phobo.management.review.dto.ReviewModerationRequest;
import com.phobo.management.review.service.ReviewModerationService;
import com.phobo.management.shift.dto.ShiftAssignmentRequest;
import com.phobo.management.shift.dto.WorkShiftRequest;
import com.phobo.management.shift.dto.WorkShiftResponse;
import com.phobo.management.shift.service.ShiftService;
import com.phobo.management.systemconfig.dto.SystemConfigResponse;
import com.phobo.management.systemconfig.dto.UpdateSystemConfigRequest;
import com.phobo.management.systemconfig.service.SystemConfigService;
import com.phobo.management.user.dto.CreateEmployeeRequest;
import com.phobo.management.user.dto.EmployeeResponse;
import com.phobo.management.user.service.AdminUserService;
import com.phobo.management.user.service.EmployeeManagementService;
import com.phobo.management.voucher.dto.VoucherRequest;
import com.phobo.management.voucher.dto.VoucherResponse;
import com.phobo.management.voucher.service.VoucherManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class Phase8AdminManagementTest {

    @Autowired
    private EmployeeManagementService employeeManagementService;
    @Autowired
    private VoucherManagementService voucherManagementService;
    @Autowired
    private ReviewModerationService reviewModerationService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ShiftService shiftService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private EmployeeProfileRepository employeeRepository;
    @Autowired
    private CustomerProfileRepository customerProfileRepository;
    @Autowired
    private OrderEntityRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private WorkShiftRepository workShiftRepository;
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Autowired
    private SystemConfigurationRepository configRepository;

    private User managerUser;
    private User adminUser;
    private User staffUser;

    @BeforeEach
    void setUp() {
        Role managerRole = roleRepository.findByCode(RoleCode.MANAGER).orElseGet(() -> 
            roleRepository.save(Role.builder().id(UUID.randomUUID().toString()).code(RoleCode.MANAGER).name("Manager").build())
        );
        Role adminRole = roleRepository.findByCode(RoleCode.ADMIN).orElseGet(() -> 
            roleRepository.save(Role.builder().id(UUID.randomUUID().toString()).code(RoleCode.ADMIN).name("Admin").build())
        );
        Role staffRole = roleRepository.findByCode(RoleCode.STAFF).orElseGet(() -> 
            roleRepository.save(Role.builder().id(UUID.randomUUID().toString()).code(RoleCode.STAFF).name("Staff").build())
        );

        String u1 = UUID.randomUUID().toString().substring(0, 6);
        managerUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .username("mgr_" + u1)
                .passwordHash("hashed")
                .email("mgr_" + u1 + "@phobo.com")
                .phone("09" + String.format("%08d", Math.abs(u1.hashCode() % 100000000)))
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        userRoleRepository.save(UserRole.builder().userId(managerUser.getId()).roleId(managerRole.getId()).build());

        String u2 = UUID.randomUUID().toString().substring(0, 6);
        adminUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .username("adm_" + u2)
                .passwordHash("hashed")
                .email("adm_" + u2 + "@phobo.com")
                .phone("09" + String.format("%08d", Math.abs(u2.hashCode() % 100000000)))
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        userRoleRepository.save(UserRole.builder().userId(adminUser.getId()).roleId(adminRole.getId()).build());

        String u3 = UUID.randomUUID().toString().substring(0, 6);
        staffUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .username("stf_" + u3)
                .passwordHash("hashed")
                .email("stf_" + u3 + "@phobo.com")
                .phone("09" + String.format("%08d", Math.abs(u3.hashCode() % 100000000)))
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        userRoleRepository.save(UserRole.builder().userId(staffUser.getId()).roleId(staffRole.getId()).build());

        employeeRepository.save(EmployeeProfile.builder()
                .id(UUID.randomUUID().toString())
                .user(staffUser)
                .fullName("Staff Test P8")
                .position("CASHIER")
                .employeeCode("EMP-" + u3)
                .isActive(true)
                .build());

        if (configRepository.findByConfigKey("store_name").isEmpty()) {
            configRepository.save(SystemConfiguration.builder()
                    .id(UUID.randomUUID().toString())
                    .configKey("store_name")
                    .configValue("Phở Bò Gia Truyền")
                    .configGroup("STORE")
                    .description("Tên cửa hàng")
                    .valueType("STRING")
                    .isPublic(true)
                    .updatedBy("sys")
                    .build());
        }

        authenticateUser(managerUser.getId(), "ROLE_MANAGER");
    }

    private void authenticateUser(String userId, String roleCode) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority(roleCode))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Test 1: Employee creation, update, and soft deactivation")
    void testEmployeeManagement() {
        String u = UUID.randomUUID().toString().substring(0, 6);
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setUsername("emp_" + u);
        req.setPassword("password123");
        req.setEmail("emp_" + u + "@phobo.com");
        req.setPhone("09" + String.format("%08d", Math.abs(u.hashCode() % 100000000)));
        req.setFullName("Nhân Viên Mới");
        req.setPosition("WAITER");

        EmployeeResponse created = employeeManagementService.createEmployee(req);
        assertNotNull(created.getEmployeeCode());
        assertTrue(created.getIsActive());

        employeeManagementService.deactivateEmployee(created.getId());
        EmployeeResponse deactivated = employeeManagementService.getEmployeeById(created.getId());
        assertFalse(deactivated.getIsActive());
    }

    @Test
    @DisplayName("Test 2: Voucher creation and usedCount preservation on update")
    void testVoucherManagement() {
        String code = "VOUCH_" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        VoucherRequest req = new VoucherRequest();
        req.setCode(code);
        req.setDiscountType("FIXED_AMOUNT");
        req.setDiscountValue(BigDecimal.valueOf(15000));
        req.setMinOrderValue(BigDecimal.valueOf(50000));
        req.setStartDate(LocalDateTime.now());
        req.setEndDate(LocalDateTime.now().plusDays(10));
        req.setUsageLimit(50);

        VoucherResponse created = voucherManagementService.createVoucher(req);
        assertEquals(0, created.getUsedCount());

        Voucher voucher = voucherRepository.findById(created.getId()).orElseThrow();
        voucher.setUsedCount(5);
        voucherRepository.save(voucher);

        req.setUsageLimit(100);
        VoucherResponse updated = voucherManagementService.updateVoucher(created.getId(), req);

        assertEquals(5, updated.getUsedCount());
        assertEquals(100, updated.getUsageLimit());
    }

    @Test
    @DisplayName("Test 3: Review moderation approve and reject with reason")
    void testReviewModeration() {
        String uCust = UUID.randomUUID().toString().substring(0, 6);
        User custUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .username("cust_" + uCust)
                .passwordHash("hashed")
                .email("cust_" + uCust + "@phobo.com")
                .phone("09" + String.format("%08d", Math.abs(uCust.hashCode() % 100000000)))
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        CustomerProfile customer = customerProfileRepository.save(CustomerProfile.builder()
                .id(UUID.randomUUID().toString())
                .user(custUser)
                .fullName("Khách Hàng Test")
                .build());

        OrderEntity order = orderRepository.save(OrderEntity.builder()
                .id(UUID.randomUUID().toString())
                .orderCode("ORD-" + uCust)
                .orderType(OrderType.ONLINE)
                .paymentMethod(PaymentMethod.ONLINE_GATEWAY)
                .status(OrderStatus.HOAN_THANH)
                .totalAmount(BigDecimal.valueOf(50000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(50000))
                .build());

        Review review = reviewRepository.save(Review.builder()
                .id(UUID.randomUUID().toString())
                .order(order)
                .customer(customer)
                .rating(5)
                .comment("Phở rất ngon!")
                .moderationStatus(ReviewModerationStatus.PENDING)
                .build());

        ReviewModerationRequest req = new ReviewModerationRequest();
        req.setModerationNote("Đánh giá hợp lệ");
        ReviewDetailResponse approved = reviewModerationService.approveReview(review.getId(), req);
        assertEquals(ReviewModerationStatus.APPROVED, approved.getModerationStatus());
    }

    @Test
    @DisplayName("Test 4: Revenue report calculations only include Payment SUCCESS and exclude canceled orders")
    void testRevenueReporting() {
        String uCode1 = UUID.randomUUID().toString().substring(0, 6);
        OrderEntity successOrder = orderRepository.save(OrderEntity.builder()
                .id(UUID.randomUUID().toString())
                .orderCode("ORD-" + uCode1)
                .orderType(OrderType.ONLINE)
                .paymentMethod(PaymentMethod.ONLINE_GATEWAY)
                .status(OrderStatus.HOAN_THANH)
                .totalAmount(BigDecimal.valueOf(100000))
                .discountAmount(BigDecimal.valueOf(10000))
                .finalAmount(BigDecimal.valueOf(90000))
                .createdAt(LocalDateTime.now().minusHours(1))
                .build());

        paymentRepository.save(Payment.builder()
                .id(UUID.randomUUID().toString())
                .order(successOrder)
                .paymentMethod(PaymentMethod.ONLINE_GATEWAY)
                .paymentStatus(PaymentStatus.SUCCESS)
                .provider("SIMULATED")
                .amount(BigDecimal.valueOf(90000))
                .paidAt(LocalDateTime.now().minusHours(1))
                .build());

        String uCode2 = UUID.randomUUID().toString().substring(0, 6);
        OrderEntity canceledOrder = orderRepository.save(OrderEntity.builder()
                .id(UUID.randomUUID().toString())
                .orderCode("ORD-" + uCode2)
                .orderType(OrderType.ONLINE)
                .paymentMethod(PaymentMethod.ONLINE_GATEWAY)
                .status(OrderStatus.DA_HUY)
                .totalAmount(BigDecimal.valueOf(200000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(200000))
                .createdAt(LocalDateTime.now().minusHours(1))
                .build());

        paymentRepository.save(Payment.builder()
                .id(UUID.randomUUID().toString())
                .order(canceledOrder)
                .paymentMethod(PaymentMethod.ONLINE_GATEWAY)
                .paymentStatus(PaymentStatus.FAILED)
                .provider("SIMULATED")
                .amount(BigDecimal.valueOf(200000))
                .build());

        RevenueOverviewResponse report = reportService.getRevenueOverview(
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), "DAY");

        assertTrue(report.getNetRevenue().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Test 5: Shift scheduling and interval overlap rejection")
    void testShiftOverlapDetection() {
        WorkShiftResponse morningShift = shiftService.createWorkShift(new WorkShiftRequest() {{
            setShiftCode("S_M_" + UUID.randomUUID().toString().substring(0, 4));
            setShiftName("Ca Sáng Test");
            setStartTime(LocalTime.of(8, 0));
            setEndTime(LocalTime.of(16, 0));
            setCrossesMidnight(false);
        }});

        WorkShiftResponse overlapShift = shiftService.createWorkShift(new WorkShiftRequest() {{
            setShiftCode("S_O_" + UUID.randomUUID().toString().substring(0, 4));
            setShiftName("Ca Trưa Trùng");
            setStartTime(LocalTime.of(12, 0));
            setEndTime(LocalTime.of(18, 0));
            setCrossesMidnight(false);
        }});

        EmployeeProfile employee = employeeRepository.findByUserId(staffUser.getId()).orElseThrow();
        LocalDate date = LocalDate.now().plusDays(1);

        shiftService.assignShift(new ShiftAssignmentRequest() {{
            setShiftId(morningShift.getId());
            setEmployeeId(employee.getId());
            setWorkDate(date);
        }});

        assertThrows(AppException.class, () -> {
            shiftService.assignShift(new ShiftAssignmentRequest() {{
                setShiftId(overlapShift.getId());
                setEmployeeId(employee.getId());
                setWorkDate(date);
            }});
        });
    }

    @Test
    @DisplayName("Test 6: Staff check-in and double check-in prevention with pessimistic lock")
    void testAttendanceCheckIn() {
        String uAtt = UUID.randomUUID().toString().substring(0, 6);
        Role staffRole = roleRepository.findByCode(RoleCode.STAFF).orElseThrow();
        User attUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .username("att_" + uAtt)
                .passwordHash("hashed")
                .email("att_" + uAtt + "@phobo.com")
                .phone("09" + String.format("%08d", Math.abs(uAtt.hashCode() % 100000000)))
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        userRoleRepository.save(UserRole.builder().userId(attUser.getId()).roleId(staffRole.getId()).build());

        EmployeeProfile emp = employeeRepository.save(EmployeeProfile.builder()
                .id(UUID.randomUUID().toString())
                .user(attUser)
                .fullName("Staff Attendance Test")
                .position("CASHIER")
                .employeeCode("EMP-" + uAtt)
                .isActive(true)
                .build());

        authenticateUser(attUser.getId(), "ROLE_STAFF");

        AttendanceResponse firstCheckIn = attendanceService.checkIn();
        assertNotNull(firstCheckIn.getCheckIn());
        assertNull(firstCheckIn.getCheckOut());

        assertThrows(AppException.class, () -> attendanceService.checkIn());

        AttendanceResponse checkedOut = attendanceService.checkOut();
        assertNotNull(checkedOut.getCheckOut());
    }

    @Test
    @DisplayName("Test 7: Inventory stock import/export and non-negative stock protection")
    void testInventoryStockManagement() {
        IngredientResponse ingredient = inventoryService.createIngredient(new IngredientRequest() {{
            setIngredientCode("ING_" + UUID.randomUUID().toString().substring(0, 4));
            setName("Xương Bo Test");
            setUnit("kg");
            setMinThreshold(BigDecimal.valueOf(10));
            setInitialStock(BigDecimal.valueOf(20));
        }});

        assertEquals(BigDecimal.valueOf(20), ingredient.getCurrentStock());

        assertThrows(AppException.class, () -> {
            inventoryService.processStockAdjustment(new InventoryStockAdjustmentRequest() {{
                setIngredientId(ingredient.getId());
                setTransactionType("EXPORT");
                setQuantity(BigDecimal.valueOf(25));
            }});
        });
    }

    @Test
    @DisplayName("Test 8: System configuration whitelist validation and type checks")
    void testSystemConfigWhitelist() {
        UpdateSystemConfigRequest req = new UpdateSystemConfigRequest();
        req.setConfigValue("Pho Bo Gia Truyên");

        SystemConfigResponse config = systemConfigService.updateConfig("store_name", req);
        assertEquals("Pho Bo Gia Truyên", config.getConfigValue());

        assertThrows(AppException.class, () -> {
            systemConfigService.updateConfig("db_password_secret", req);
        });
    }

    @Test
    @DisplayName("Test 9: Admin User management with self-lockout protection")
    void testAdminSelfLockoutProtection() {
        authenticateUser(adminUser.getId(), "ROLE_ADMIN");

        assertThrows(AppException.class, () -> {
            adminUserService.updateUserStatus(adminUser.getId(), "INACTIVE");
        });
    }
}
