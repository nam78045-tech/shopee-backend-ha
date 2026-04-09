package com.shopee.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;
import java.nio.file.*;
import java.util.*;

@SpringBootApplication
@RestController
@CrossOrigin // Giúp Frontend gọi API từ GitHub Pages không bị chặn
public class BackendApplication implements WebMvcConfigurer {

    private static List<Map<String, Object>> storage = new ArrayList<>();
    private static Map<String, Integer> cartStats = new HashMap<>();

    // Đường dẫn để lưu ảnh
    private final String uploadPath = System.getProperty("user.dir") + "/uploads/";

    // CHỈ GIỮ 1 HÀM MAIN NÀY (Hàm main cũ rỗng phải xóa đi)
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackendApplication.class);
        // Đoạn này để Render/Railway tự cấp cổng cho Web
        String port = System.getenv("PORT");
        if (port == null) port = "8080"; 
        app.setDefaultProperties(Collections.singletonMap("server.port", port));
        app.run(args);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @GetMapping("/api/products")
    public List<Map<String, Object>> getProducts() {
        return storage;
    }

    @PostMapping("/api/products")
    public String addProduct(
            @RequestParam("name") String name,
            @RequestParam("price") int price,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(uploadPath);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> product = new HashMap<>();
            product.put("id", storage.size() + 1);
            product.put("name", name);
            product.put("price", price);
            product.put("image", "/images/" + fileName);
            product.put("description", description);
            
            storage.add(product);
            return "Đã đăng sản phẩm thành công!";
        } catch (Exception e) {
            return "Lỗi khi tải ảnh: " + e.getMessage();
        }
    }

    @PostMapping("/api/cart/add")
    public void trackCart(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        cartStats.put(name, cartStats.getOrDefault(name, 0) + 1);
    }

    @GetMapping("/api/cart/stats")
    public Map<String, Integer> getStats() {
        return cartStats;
    }
}