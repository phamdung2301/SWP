package com.liteflow.modules.core.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessPolicyTest {

    @Test
    void employeeCannotAccessPayrollApi() {
        List<String> roles = Collections.singletonList("Employee");
        assertFalse(AccessPolicy.isAuthorized(roles, "/api/payroll/list"));
        assertFalse(AccessPolicy.isAuthorized(roles, "/api/employee/compensation/x"));
    }

    @Test
    void employeeCanSalarySummaryOnlyUnderEmployeeApi() {
        List<String> roles = Collections.singletonList("Employee");
        assertTrue(AccessPolicy.isAuthorized(roles, "/api/employee/salary-summary"));
    }

    @Test
    void kitchenCannotOpenProductsAdmin() {
        List<String> roles = Collections.singletonList("Kitchen");
        assertFalse(AccessPolicy.isAuthorized(roles, "/products"));
        assertFalse(AccessPolicy.isAuthorized(roles, "/employees"));
    }

    @Test
    void ownerBypassesMatrix() {
        List<String> roles = Arrays.asList("Employee", "Owner");
        assertTrue(AccessPolicy.isAuthorized(roles, "/api/ai-agent-config"));
    }

    @Test
    void hrCanPayrollAndSetup() {
        List<String> roles = Collections.singletonList("HR Officer");
        assertTrue(AccessPolicy.isAuthorized(roles, "/api/payroll/list"));
        assertTrue(AccessPolicy.isAuthorized(roles, "/employee/setupEmployee.jsp"));
    }

    @Test
    void managerCannotUseEmployeeSetupPrefix() {
        List<String> roles = Collections.singletonList("Manager");
        assertFalse(AccessPolicy.isAuthorized(roles, "/employee/setupEmployee.jsp"));
        assertTrue(AccessPolicy.isAuthorized(roles, "/employee/employeeList.jsp"));
    }
}
