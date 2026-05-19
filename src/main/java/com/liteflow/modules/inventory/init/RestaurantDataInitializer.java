package com.liteflow.modules.inventory.init;

import com.liteflow.modules.inventory.model.Product;
import com.liteflow.modules.inventory.dao.ProductDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@WebListener
public class RestaurantDataInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Starting data initialization ===");

        try {
            ProductDAO productDAO = new ProductDAO();

            // Kiểm tra nếu bảng đã có dữ liệu
            if (productDAO.count() > 0) {
                System.out.println("Products table already has data. Skipping initialization.");
                return;
            }

            List<Product> products = createMenuItems();
            for (Product product : products) {
                productDAO.insert(product);
            }

            System.out.println("Inserted " + products.size() + " menu items successfully!");

        } catch (Exception e) {
            System.err.println("Error initializing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }

    private List<Product> createMenuItems() {
        return Arrays.asList(
                // ========== MÓN KHAI VỊ ==========
                createProduct(
                        "Gỏi Cuốn Tôm Thịt",
                        "Tôm tươi, thịt ba chỉ, bún, rau sống cuốn bánh tráng, ăn kèm nước chấm đậu phộng",
                        "https://images.unsplash.com/photo-1559314809-0d155014e29e?w=600",
                        "APPETIZER", "ACTIVE", "Đĩa"
                ),
                createProduct(
                        "Chả Giò Chiên Giòn",
                        "Chả giò nhân thịt băm, miến, nấm mèo, cà rốt, hành tây, chiên giòn vàng",
                        "https://images.unsplash.com/photo-1625938144757-3492c2fd5fba?w=600",
                        "APPETIZER", "ACTIVE", "Đĩa"
                ),
                createProduct(
                        "Súp Măng Cua",
                        "Súp nóng hổi với măng tươi, thịt cua xay, trứng gà và nấm hương",
                        "https://placehold.co/600x400/FF8C00/white?text=Soup+Monng+Cua",
                        "APPETIZER", "ACTIVE", "Tô"
                ),

                // ========== MÓN CHÍNH - HẢI SẢN ==========
                createProduct(
                        "Tôm Hùm Nướng Phô Mai",
                        "Tôm hùm Canada tươi sống, nướng với sốt phô mai và trứng muối",
                        "https://images.unsplash.com/photo-1559742811-822873691151?w=600",
                        "MAIN_COURSE", "ACTIVE", "Con"
                ),
                createProduct(
                        "Cá Hồi Áp Chảo",
                        "Cá hồi Na Uy áp chảo cùng sốt cam chua ngọt, ăn kèm rau củ",
                        "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=600",
                        "MAIN_COURSE", "ACTIVE", "Phần"
                ),
                createProduct(
                        "Mực Chiên Mắm Tỏi",
                        "Mực tươi chiên giòn, sốt mắm tỏi cay thơm đặc trưng",
                        "https://images.unsplash.com/photo-1583688211617-71b0f12a4dab?w=600",
                        "MAIN_COURSE", "ACTIVE", "Đĩa"
                ),

                // ========== MÓN CHÍNH - THỊT ==========
                createProduct(
                        "Bò Bít Tết Sốt Tiêu Đen",
                        "Thịt bò nhập khẩu, tẩm ướp gia vị, sốt tiêu đen đậm đà",
                        "https://images.unsplash.com/photo-1546964124-0cce460f38ef?w=600",
                        "MAIN_COURSE", "ACTIVE", "Phần"
                ),
                createProduct(
                        "Gà Chiên Giòn",
                        "Đùi gà chiên giòn, nước sốt mật ong và mù tạt",
                        "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=600",
                        "MAIN_COURSE", "ACTIVE", "Đĩa"
                ),

                // ========== MÓN CHAY ==========
                createProduct(
                        "Cơm Chiên Chay Thập Cẩm",
                        "Cơm chiên cùng đậu hũ, nấm, bắp non, cà rốt, đậu que, hạt điều",
                        "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=600",
                        "VEGETARIAN", "ACTIVE", "Đĩa"
                ),

                // ========== MÌ & CHÁO ==========
                createProduct(
                        "Mì Ý Sốt Bò Bằm",
                        "Mì Ý spaghetti sốt cà chua, thịt bò bằm, phô mai parmesan",
                        "https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=600",
                        "NOODLE", "ACTIVE", "Đĩa"
                ),
                createProduct(
                        "Hủ Tiếu Nam Vang",
                        "Hủ tiếu dai, tôm, thịt bằm, gan, lòng non, nước dùng hầm xương",
                        "https://placehold.co/600x400/D2691E/white?text=Hu+Tieu+Nam+Vang",
                        "NOODLE", "ACTIVE", "Tô"
                ),

                // ========== ĐỒ UỐNG - CÓ CỒN ==========
                createProduct(
                        "Bia Hà Nội",
                        "Bia tươi thơm ngon, lạnh, phù hợp với các món nhậu",
                        "https://placehold.co/600x400/FFD700/black?text=Ha+Noi+Beer",
                        "BEER", "ACTIVE", "Chai"
                ),
                createProduct(
                        "Vang Đỏ Château Latour",
                        "Rượu vang đỏ Pháp, nồng độ 13%, ủ từ nho Cabernet Sauvignon",
                        "https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?w=600",
                        "WINE", "ACTIVE", "Chai"
                ),
                createProduct(
                        "Cocktail Mojito",
                        "Rum trắng, bạc hà tươi, chanh, đường, soda, đá viên",
                        "https://images.unsplash.com/photo-1514362545856-9b6f6f9b7b7f?w=600",
                        "COCKTAIL", "ACTIVE", "Ly"
                ),

                // ========== ĐỒ UỐNG - KHÔNG CỒN ==========
                createProduct(
                        "Nước Ép Cam",
                        "Cam tươi vắt 100%, không đường, giữ nguyên vitamin",
                        "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=600",
                        "JUICE", "ACTIVE", "Ly"
                ),
                createProduct(
                        "Sinh Tố Bơ",
                        "Bơ sáp xay cùng sữa đặc, đá, tạo thành sinh tố béo ngậy",
                        "https://images.unsplash.com/photo-1525385133512-2f3bdd039054?w=600",
                        "SMOOTHIE", "ACTIVE", "Ly"
                ),
                createProduct(
                        "Trà Sữa Trân Châu",
                        "Trà đen, sữa tươi, đường, trân châu dai giòn",
                        "https://images.unsplash.com/photo-1560619209-e689dc8d7510?w=600",
                        "TEA", "ACTIVE", "Ly"
                ),
                createProduct(
                        "Cà Phê Đen Đá",
                        "Cà phê Robusta phin, đậm vị, thơm, đá lạnh",
                        "https://images.unsplash.com/photo-1529736576490-d6f8f2e6c6d6?w=600",
                        "COFFEE", "ACTIVE", "Ly"
                ),
                createProduct(
                        "Trà Gừng Mật Ong",
                        "Trà nóng gừng tươi, mật ong nguyên chất, tốt cho tiêu hóa",
                        "https://placehold.co/600x400/FF8C00/white?text=Ginger+Honey+Tea",
                        "TEA", "ACTIVE", "Ly"
                ),

                // ========== TRÁNG MIỆNG ==========
                createProduct(
                        "Chè Thái",
                        "Chè trái cây, hạt lựu, sương sáo, nước cốt dừa",
                        "https://images.unsplash.com/photo-1574085733277-851f4fffb7d1?w=600",
                        "DESSERT", "ACTIVE", "Ly"
                ),
                createProduct(
                        "Bánh Flan Caramel",
                        "Bánh flan mềm mịn, sốt caramel ngọt dịu, đá bào",
                        "https://placehold.co/600x400/DAA520/white?text=Caramel+Flan",
                        "DESSERT", "ACTIVE", "Phần"
                )
        );
    }

    private Product createProduct(String name, String description, String imageUrl,
                                  String productType, String status, String unit) {
        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setProductType(productType);
        product.setStatus(status);
        product.setUnit(unit);
        product.setIsDeleted(false);
        product.setImportDate(LocalDateTime.now());
        return product;
    }
}