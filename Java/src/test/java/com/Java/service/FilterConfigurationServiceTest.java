package com.Java.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Filter Configuration Service Tests")
class FilterConfigurationServiceTest {

    private FilterConfigurationService filterConfigurationService;

    @BeforeEach
    void setUp() {
        filterConfigurationService = new FilterConfigurationService();
    }

    @Test
    @DisplayName("Should return all filters for pricing manager")
    void shouldReturnAllFiltersForPricingManager() {
        // Given
        String userRole = "pricing_manager";

        // When
        List<String> filters = filterConfigurationService.getFiltersForRole(userRole);

        // Then
        assertNotNull(filters, "Filters should not be null");
        assertEquals(4, filters.size(), "Pricing manager should have 4 filters");
        assertTrue(filters.contains("building"), "Should contain building filter");
        assertTrue(filters.contains("room_type"), "Should contain room_type filter");
        assertTrue(filters.contains("beds"), "Should contain beds filter");
        assertTrue(filters.contains("grade"), "Should contain grade filter");
    }

    @Test
    @DisplayName("Should return limited filters for regional manager")
    void shouldReturnLimitedFiltersForRegionalManager() {
        // Given
        String userRole = "regional_manager";

        // When
        List<String> filters = filterConfigurationService.getFiltersForRole(userRole);

        // Then
        assertNotNull(filters);
        assertEquals(2, filters.size(), "Regional manager should have 2 filters");
        assertTrue(filters.contains("building"));
        assertTrue(filters.contains("room_type"));
        assertFalse(filters.contains("beds"), "Should not contain beds filter");
        assertFalse(filters.contains("grade"), "Should not contain grade filter");
    }

    @Test
    @DisplayName("Should return moderate filters for reporting user")
    void shouldReturnModerateFiltersForReportingUser() {
        // Given
        String userRole = "reporting_user";

        // When
        List<String> filters = filterConfigurationService.getFiltersForRole(userRole);

        // Then
        assertNotNull(filters);
        assertEquals(3, filters.size(), "Reporting user should have 3 filters");
        assertTrue(filters.contains("building"));
        assertTrue(filters.contains("room_type"));
        assertTrue(filters.contains("currency"));
        assertFalse(filters.contains("beds"));
        assertFalse(filters.contains("grade"));
    }

    @Test
    @DisplayName("Should return pricing manager filters for unknown role")
    void shouldReturnDefaultFiltersForUnknownRole() {
        // Given
        String unknownRole = "unknown_role";

        // When
        List<String> filters = filterConfigurationService.getFiltersForRole(unknownRole);

        // Then
        assertNotNull(filters);
        assertEquals(4, filters.size(), "Unknown role should default to pricing manager filters");
        assertTrue(filters.contains("building"));
        assertTrue(filters.contains("room_type"));
        assertTrue(filters.contains("beds"));
        assertTrue(filters.contains("grade"));
    }

    @Test
    @DisplayName("Should handle null role gracefully")
    void shouldHandleNullRoleGracefully() {
        // Given
        String nullRole = null;

        // When
        List<String> filters = filterConfigurationService.getFiltersForRole(nullRole);

        // Then
        assertNotNull(filters, "Should not return null for null role");
        assertEquals(4, filters.size(), "Null role should default to pricing manager filters");
    }

    @Test
    @DisplayName("Should handle empty role gracefully")
    void shouldHandleEmptyRoleGracefully() {
        // Given
        String emptyRole = "";

        // When
        List<String> filters = filterConfigurationService.getFiltersForRole(emptyRole);

        // Then
        assertNotNull(filters);
        assertEquals(4, filters.size(), "Empty role should default to pricing manager filters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"pricing_manager", "regional_manager", "reporting_user"})
    @DisplayName("Should always include building and room_type filters for all valid roles")
    void shouldAlwaysIncludeBasicFiltersForValidRoles(String role) {
        // When
        List<String> filters = filterConfigurationService.getFiltersForRole(role);

        // Then
        assertTrue(filters.contains("building"),
                "Role " + role + " should always have building filter");
        assertTrue(filters.contains("room_type"),
                "Role " + role + " should always have room_type filter");
    }

    @Test
    @DisplayName("Should return all available roles")
    void shouldReturnAllAvailableRoles() {
        // When
        List<String> roles = filterConfigurationService.getAvailableRoles();

        // Then
        assertNotNull(roles, "Roles should not be null");
        assertEquals(3, roles.size(), "Should have exactly 3 roles");
        assertTrue(roles.contains("pricing_manager"), "Should contain pricing_manager");
        assertTrue(roles.contains("regional_manager"), "Should contain regional_manager");
        assertTrue(roles.contains("reporting_user"), "Should contain reporting_user");
    }

    @Test
    @DisplayName("Should return modifiable copy of roles")
    void shouldReturnModifiableCopyOfRoles() {
        // When
        List<String> roles = filterConfigurationService.getAvailableRoles();

        // Then - Should be able to modify the returned list without affecting the service
        assertDoesNotThrow(() -> {
            roles.add("new_role");
        }, "Returned list should be modifiable");

        assertEquals(4, roles.size(), "Modified list should have 4 roles");

        // Verify original service data is unchanged
        List<String> freshRoles = filterConfigurationService.getAvailableRoles();
        assertEquals(3, freshRoles.size(), "Service should still return 3 original roles");
    }

    @Test
    @DisplayName("Should return consistent results for same role")
    void shouldReturnConsistentResultsForSameRole() {
        // Given
        String role = "pricing_manager";

        // When
        List<String> filters1 = filterConfigurationService.getFiltersForRole(role);
        List<String> filters2 = filterConfigurationService.getFiltersForRole(role);

        // Then
        assertEquals(filters1.size(), filters2.size(), "Should return same number of filters");
        assertTrue(filters1.containsAll(filters2), "Should contain same filters");
        assertTrue(filters2.containsAll(filters1), "Should contain same filters");
    }

    @Test
    @DisplayName("Should have different filter counts for different roles")
    void shouldHaveDifferentFilterCountsForDifferentRoles() {
        // When
        List<String> pricingManagerFilters = filterConfigurationService.getFiltersForRole("pricing_manager");
        List<String> regionalManagerFilters = filterConfigurationService.getFiltersForRole("regional_manager");
        List<String> reportingUserFilters = filterConfigurationService.getFiltersForRole("reporting_user");

        // Then
        assertNotEquals(pricingManagerFilters.size(), regionalManagerFilters.size(),
                "Pricing manager and regional manager should have different filter counts");
        assertNotEquals(regionalManagerFilters.size(), reportingUserFilters.size(),
                "Regional manager and reporting user should have different filter counts");
        assertNotEquals(pricingManagerFilters.size(), reportingUserFilters.size(),
                "Pricing manager and reporting user should have different filter counts");
    }

    @Test
    @DisplayName("Should validate role hierarchy - pricing manager has most filters")
    void shouldValidateRoleHierarchy() {
        // When
        List<String> pricingManagerFilters = filterConfigurationService.getFiltersForRole("pricing_manager");
        List<String> regionalManagerFilters = filterConfigurationService.getFiltersForRole("regional_manager");
        List<String> reportingUserFilters = filterConfigurationService.getFiltersForRole("reporting_user");

        // Then
        assertTrue(pricingManagerFilters.size() >= regionalManagerFilters.size(),
                "Pricing manager should have same or more filters than regional manager");
        assertTrue(pricingManagerFilters.size() >= reportingUserFilters.size(),
                "Pricing manager should have same or more filters than reporting user");
    }
}
