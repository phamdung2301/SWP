package com.liteflow.modules.core.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ma trận phân quyền theo URL prefix (sau context path), dùng cho {@link com.liteflow.modules.core.filter.AuthenticationFilter}.
 * <p>
 * Nguyên tắc: mặc định từ chối; mỗi role chỉ được các prefix đã liệt kê; Owner/Admin bypass toàn bộ.
 * Prefix so khớp {@code requestURI.startsWith(prefix)} trên chuỗi đã lowercase (JSP và servlet đều dùng chung).
 */
public final class AccessPolicy {

    private AccessPolicy() {
    }

    private static final Set<String> SUPER_ROLES = Set.of("owner", "admin");

    /** Role name (đúng như trong bảng Roles, không phân biệt hoa thường khi tra cứu) → danh sách prefix được phép. */
    private static final Map<String, List<String>> ROLE_PREFIXES = buildRolePrefixes();

    private static Map<String, List<String>> buildRolePrefixes() {
        Map<String, List<String>> m = new LinkedHashMap<>();

        // Quản lý vận hành: gần như toàn bộ nghiệp vụ, trừ cấu hình hệ thống / gửi mass / gán nhân sự nền tảng
        m.put("Manager", Arrays.asList(
                "/dashboard",
                "/dashboard.jsp",
                "/report",
                "/api/reports",
                "/api/demand-forecast",
                "/api/chatbot",
                "/employees",
                "/employee/employeelist",
                "/employee/paysheet",
                "/schedule",
                "/schedule.jsp",
                "/attendance",
                "/attendance.jsp",
                "/compensation",
                "/api/employee",
                "/api/employee/compensation",
                "/api/payroll",
                "/api/timesheet",
                "/api/leave-request",
                "/api/forgot-clock",
                "/api/notices",
                "/api/personal-schedule",
                "/procurement",
                "/products",
                "/setprice",
                "/roomtable",
                "/reception",
                "/inventory/",
                "/sales",
                "/cashier",
                "/cart",
                "/checkout",
                "/api/cashier",
                "/api/order",
                "/api/payment",
                "/api/reservation",
                "/settings",
                "/alert",
                "/api/notification",
                "/api/company-info",
                "/admin/recalculate-attendance-flags",
                "/payment/",
                "/kitchen",
                "/api/kitchen",
                "/api/order/status"
        ));

        m.put("HR Officer", Arrays.asList(
                "/dashboard",
                "/dashboard.jsp",
                "/employees",
                "/employee",
                "/employee/",
                "/schedule",
                "/schedule.jsp",
                "/attendance",
                "/attendance.jsp",
                "/payroll",
                "/leaveRequests",
                "/compensation",
                "/api/employee",
                "/api/employee/compensation",
                "/api/payroll",
                "/api/timesheet",
                "/api/leave-request",
                "/api/forgot-clock",
                "/api/notices",
                "/api/personal-schedule",
                "/admin/recalculate-attendance-flags",
                "/employee/setup",
                "/employee/paysheet"
        ));

        m.put("Procurement Officer", Arrays.asList(
                "/dashboard",
                "/dashboard.jsp",
                "/procurement",
                "/suppliers",
                "/invoices",
                "/api/company-info",
                "/report/revenue",
                "/report/revenue/print"
        ));

        m.put("Inventory Manager", Arrays.asList(
                "/dashboard",
                "/dashboard.jsp",
                "/inventory",
                "/products",
                "/setprice",
                "/stock",
                "/roomtable",
                "/roomtable.jsp",
                "/reception",
                "/api/reservation",
                "/procurement",
                "/procurement/dashboard",
                "/procurement/supplier",
                "/procurement/po",
                "/procurement/po-items",
                "/procurement/gr",
                "/procurement/invoice/print",
                "/report/revenue",
                "/report/revenue/print"
        ));

        m.put("Cashier", Arrays.asList(
                "/dashboard",
                "/dashboard.jsp",
                "/cashier",
                "/cart",
                "/checkout",
                "/sales",
                "/roomtable",
                "/reception",
                "/api/cashier",
                "/api/order",
                "/api/payment",
                "/api/reservation",
                "/products",
                "/inventory/productlist",
                "/inventory/roomtable",
                "/inventory/table-fragment",
                "/payment/",
                "/sales/"
        ));

        m.put("Kitchen", Arrays.asList(
                "/kitchen",
                "/api/kitchen",
                "/api/order/status",
                "/api/order/table"
        ));

        m.put("Employee", Arrays.asList(
                "/dashboard-employee",
                "/dashboard-employee.jsp",
                "/schedule",
                "/schedule.jsp",
                "/attendance",
                "/attendance.jsp",
                "/employee/paysheet",
                "/api/personal-schedule",
                "/api/notices",
                "/api/leave-request",
                "/api/forgot-clock",
                "/api/timesheet",
                "/api/employee/salary-summary"
        ));

        return Collections.unmodifiableMap(m);
    }

    public static boolean isSuperRole(String roleName) {
        if (roleName == null) {
            return false;
        }
        return SUPER_ROLES.contains(roleName.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * @param path request path không gồm context path (ví dụ {@code /dashboard.jsp})
     */
    public static boolean isAuthorized(List<String> roles, String path) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        String p = path == null ? "" : path.toLowerCase(Locale.ROOT);
        for (String raw : roles) {
            if (raw == null) {
                continue;
            }
            if (isSuperRole(raw)) {
                return true;
            }
        }
        for (String raw : roles) {
            if (raw == null) {
                continue;
            }
            if (isSuperRole(raw)) {
                continue;
            }
            List<String> prefixes = prefixesForRole(raw);
            for (String prefix : prefixes) {
                String px = prefix.toLowerCase(Locale.ROOT);
                if ("/*".equals(px) || p.startsWith(px)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> prefixesForRole(String roleFromSession) {
        String key = roleFromSession.trim();
        for (Map.Entry<String, List<String>> e : ROLE_PREFIXES.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return Collections.emptyList();
    }

    /** Cho log / debug: tất cả prefix theo role. */
    public static String describeMatrix() {
        return ROLE_PREFIXES.entrySet().stream()
                .map(en -> en.getKey() + " -> " + en.getValue().size() + " prefixes")
                .collect(Collectors.joining(", "));
    }

    public static List<String> allConfiguredRoles() {
        return new ArrayList<>(ROLE_PREFIXES.keySet());
    }
}
