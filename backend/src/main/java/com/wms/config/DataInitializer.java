package com.wms.config;

import com.wms.model.core.InventoryItem;
import com.wms.model.core.Product;
import com.wms.model.core.StorageLocation;
import com.wms.model.user.Customer;
import com.wms.model.user.Supplier;
import com.wms.model.user.WarehouseManager;
import com.wms.model.user.WarehouseStaff;
import com.wms.repository.CustomerRepository;
import com.wms.repository.InventoryRepository;
import com.wms.repository.ProductRepository;
import com.wms.repository.StorageLocationRepository;
import com.wms.repository.SupplierRepository;
import com.wms.repository.WarehouseManagerRepository;
import com.wms.repository.WarehouseStaffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(ProductRepository productRepository,
                               StorageLocationRepository storageLocationRepository,
                               InventoryRepository inventoryRepository,
                               CustomerRepository customerRepository,
                               SupplierRepository supplierRepository,
                               WarehouseManagerRepository managerRepository,
                               WarehouseStaffRepository staffRepository) {
        return args -> {

            // ── Products & Inventory ────────────────────────────────────────
            if (productRepository.count() == 0) {
                Product laptop = new Product();
                laptop.setSku("SKU-LAPTOP-001");
                laptop.setName("Industrial Laptop");
                laptop.setDescription("Rugged warehouse laptop");
                laptop.setUnitPrice(new BigDecimal("1200.00"));
                laptop.setCategory("Electronics");
                laptop = productRepository.save(laptop);

                Product scanner = new Product();
                scanner.setSku("SKU-SCAN-001");
                scanner.setName("Barcode Scanner");
                scanner.setDescription("Handheld barcode scanner");
                scanner.setUnitPrice(new BigDecimal("145.50"));
                scanner.setCategory("Electronics");
                scanner = productRepository.save(scanner);

                StorageLocation locationA = storageLocationRepository.findByCode("LOC-A1")
                        .orElseGet(() -> {
                            StorageLocation location = new StorageLocation();
                            location.setCode("LOC-A1");
                            location.setZone("A");
                            location.setAisle("1");
                            location.setRack("R1");
                            location.setBin("B1");
                            return storageLocationRepository.save(location);
                        });

                StorageLocation locationB = storageLocationRepository.findByCode("LOC-B1")
                        .orElseGet(() -> {
                            StorageLocation location = new StorageLocation();
                            location.setCode("LOC-B1");
                            location.setZone("B");
                            location.setAisle("1");
                            location.setRack("R1");
                            location.setBin("B1");
                            return storageLocationRepository.save(location);
                        });

                InventoryItem item1 = new InventoryItem();
                item1.setProduct(laptop);
                item1.setStorageLocation(locationA);
                item1.setQuantity(20);
                item1.setReorderLevel(5);
                inventoryRepository.save(item1);

                InventoryItem item2 = new InventoryItem();
                item2.setProduct(scanner);
                item2.setStorageLocation(locationB);
                item2.setQuantity(80);
                item2.setReorderLevel(20);
                inventoryRepository.save(item2);
            }

            // ── Seed Demo Users (one per role) ──────────────────────────────
            if (managerRepository.count() == 0) {
                WarehouseManager mgr = new WarehouseManager();
                mgr.setName("Yashas");
                mgr.setEmail("manager@wms.com");
                mgr.setPassword("manager123");
                mgr.setPhone("1111111111");
                mgr.setRole("MANAGER");
                mgr.setEmployeeCode("EMP-MGR-001");
                mgr.setManagedWarehouseName("Main Warehouse");
                managerRepository.save(mgr);
            }

            if (staffRepository.count() == 0) {
                WarehouseStaff staff = new WarehouseStaff();
                staff.setName("Vinod");
                staff.setEmail("staff@wms.com");
                staff.setPassword("staff123");
                staff.setPhone("2222222222");
                staff.setRole("STAFF");
                staff.setEmployeeCode("EMP-STF-001");
                staff.setShift("MORNING");
                staffRepository.save(staff);
            }

            if (customerRepository.count() == 0) {
                Customer customer = new Customer();
                customer.setName("Vikas");
                customer.setEmail("customer@wms.com");
                customer.setPassword("customer123");
                customer.setPhone("3333333333");
                customer.setRole("CUSTOMER");
                customer.setCustomerCode("CUST-001");
                customerRepository.save(customer);
            }

            if (supplierRepository.count() == 0) {
                Supplier supplier = new Supplier();
                supplier.setName("Vishwas");
                supplier.setEmail("supplier@wms.com");
                supplier.setPassword("supplier123");
                supplier.setPhone("4444444444");
                supplier.setRole("SUPPLIER");
                supplier.setSupplierCode("SUP-001");
                supplier.setCompanyName("WMS Supplies Inc");
                supplierRepository.save(supplier);
            }
        };
    }
}
