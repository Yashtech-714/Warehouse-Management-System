package com.wms.controller;

import com.wms.repository.CustomerRepository;
import com.wms.repository.SupplierRepository;
import com.wms.repository.WarehouseManagerRepository;
import com.wms.repository.WarehouseStaffRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AuthController — MVC Controller layer (GRASP: Controller principle).
 * Delegates user lookup to repositories; contains no business logic (SOLID: SRP).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final WarehouseManagerRepository managerRepository;
    private final WarehouseStaffRepository staffRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;

    public AuthController(WarehouseManagerRepository managerRepository,
                          WarehouseStaffRepository staffRepository,
                          CustomerRepository customerRepository,
                          SupplierRepository supplierRepository) {
        this.managerRepository = managerRepository;
        this.staffRepository = staffRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * POST /auth/login
     * Searches all four user tables by email. Returns role + user info on success.
     * Password comparison is plain-text (sufficient for OOAD demo; swap with BCrypt if needed).
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        // --- Search WarehouseManager table ---
        Optional<com.wms.model.user.WarehouseManager> manager = managerRepository.findByEmail(email);
        if (manager.isPresent() && password.equals(manager.get().getPassword())) {
            return ok(manager.get().getId(), manager.get().getName(), email, "MANAGER");
        }

        // --- Search WarehouseStaff table ---
        Optional<com.wms.model.user.WarehouseStaff> staff = staffRepository.findByEmail(email);
        if (staff.isPresent() && password.equals(staff.get().getPassword())) {
            return ok(staff.get().getId(), staff.get().getName(), email, "STAFF");
        }

        // --- Search Customer table ---
        Optional<com.wms.model.user.Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent() && password.equals(customer.get().getPassword())) {
            return ok(customer.get().getId(), customer.get().getName(), email, "CUSTOMER");
        }

        // --- Search Supplier table ---
        Optional<com.wms.model.user.Supplier> supplier = supplierRepository.findByEmail(email);
        if (supplier.isPresent() && password.equals(supplier.get().getPassword())) {
            return ok(supplier.get().getId(), supplier.get().getName(), email, "SUPPLIER");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
    }

    /** GET /auth/me — returns user info from token stored in frontend localStorage. */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                                  @RequestHeader(value = "X-User-Role", required = false) String role,
                                                  @RequestHeader(value = "X-User-Name", required = false) String name) {
        if (userId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }
        Map<String, Object> body = new HashMap<>();
        body.put("id", userId);
        body.put("name", name != null ? name : "User");
        body.put("role", role);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> ok(Long id, String name, String email, String role) {
        String token = role + ":" + id + ":" + System.currentTimeMillis();
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("user", Map.of("id", id, "name", name, "email", email, "role", role));
        return ResponseEntity.ok(body);
    }

    // ---- Inner DTO ----
    public static class LoginRequest {
        @NotBlank(message = "Email is required.")
        private String email;

        @NotBlank(message = "Password is required.")
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
