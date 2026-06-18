package com.example.repository

import com.example.database.CartItemEntity
import com.example.database.CartItemWithProduct
import com.example.database.InquiryEntity
import com.example.database.ProductEntity
import com.example.database.StoreDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StoreRepository(private val storeDao: StoreDao) {

    val allProducts: Flow<List<ProductEntity>> = storeDao.getAllProducts()
    val featuredProducts: Flow<List<ProductEntity>> = storeDao.getFeaturedProducts()
    val cartItems: Flow<List<CartItemWithProduct>> = storeDao.getCartItems()
    val allInquiries: Flow<List<InquiryEntity>> = storeDao.getAllInquiries()

    suspend fun getProductById(id: Int): ProductEntity? {
        return storeDao.getProductById(id)
    }

    suspend fun addProductToCart(productId: Int, selectedOption: String, quantity: Int = 1) {
        // Check if item already exists in cart with same option
        val currentItems = storeDao.getCartItems().first()
        val existingItem = currentItems.find { 
            it.cartItem.productId == productId && it.cartItem.selectedOption == selectedOption 
        }

        if (existingItem != null) {
            val updatedItem = existingItem.cartItem.copy(
                quantity = existingItem.cartItem.quantity + quantity
            )
            storeDao.updateCartItem(updatedItem)
        } else {
            val newItem = CartItemEntity(
                productId = productId,
                quantity = quantity,
                selectedOption = selectedOption
            )
            storeDao.insertCartItem(newItem)
        }
    }

    suspend fun updateCartItemQuantity(cartItemId: Int, newQuantity: Int) {
        if (newQuantity <= 0) {
            storeDao.deleteCartItemById(cartItemId)
        } else {
            // Retrieve current item to update
            val currentItems = storeDao.getCartItems().first()
            val item = currentItems.find { it.cartItem.id == cartItemId }
            if (item != null) {
                storeDao.updateCartItem(item.cartItem.copy(quantity = newQuantity))
            }
        }
    }

    suspend fun deleteCartItem(cartItemId: Int) {
        storeDao.deleteCartItemById(cartItemId)
    }

    suspend fun clearCart() {
        storeDao.clearCart()
    }

    suspend fun submitInquiry(name: String, phone: String, message: String) {
        val inquiry = InquiryEntity(
            name = name,
            phone = phone,
            message = message
        )
        storeDao.insertInquiry(inquiry)
    }

    suspend fun prepopulateDatabaseIfNeeded() {
        // Check if products table is empty
        val currentProducts = storeDao.getAllProducts().first()
        if (currentProducts.isEmpty()) {
            val initialCatalog = listOf(
                ProductEntity(
                    name = "Bashundhara LP Gas Cylinder 12kg",
                    category = "LPG Cylinders",
                    brand = "Bashundhara",
                    price = 1420.0,
                    originalPrice = 1600.0,
                    description = "The most trusted brand of LPG gas in Bangladesh under the Bashundhara group. Delivers a highly efficient, constant-intensity blue flame for fast and secure cooking. Features premium quality three-layer protection valve safety mechanisms. Perfect for households.",
                    stockStatus = "In Stock",
                    isFeatured = true,
                    weight = "12 kg Cylinder",
                    rating = 4.9f
                ),
                ProductEntity(
                    name = "Beximco LPG Cylinder 12kg",
                    category = "LPG Cylinders",
                    brand = "Beximco",
                    price = 1390.0,
                    originalPrice = 1550.0,
                    description = "Premium class Beximco liquefied petroleum gas cylinder featuring advanced composite material that makes it lightweight and explosion-proof. Transparent body lets you monitor gas levels in real-time. High safety and smart styling.",
                    stockStatus = "In Stock",
                    isFeatured = true,
                    weight = "12 kg Cylinder",
                    rating = 4.8f
                ),
                ProductEntity(
                    name = "Omera LP Gas Cylinder 12kg",
                    category = "LPG Cylinders",
                    brand = "Omera",
                    price = 1380.0,
                    originalPrice = 1500.0,
                    description = "Omera LPG provides highly reliable performance with strict weight calibration and leakproofing. Delivers high heat efficiency and clear, soot-free combustion. Perfect for household cooking, gas stoves, and smart kitchens.",
                    stockStatus = "In Stock",
                    isFeatured = false,
                    weight = "12 kg Cylinder",
                    rating = 4.7f
                ),
                ProductEntity(
                    name = "Jamuna LP Gas Cylinder 12kg",
                    category = "LPG Cylinders",
                    brand = "Jamuna",
                    price = 1375.0,
                    originalPrice = 1480.0,
                    description = "Jamuna Group's trusted LP gas cylinder. Complete leakage protection check, manufactured with state of the art technology in heavy-gauge steel. Delivers stable, constant flame with extremely low wastage.",
                    stockStatus = "In Stock",
                    isFeatured = false,
                    weight = "12 kg Cylinder",
                    rating = 4.6f
                ),
                ProductEntity(
                    name = "Miyako Tempered Glass Double Burner Gas Stove",
                    category = "Gas Stoves",
                    brand = "Miyako",
                    price = 2950.0,
                    originalPrice = 3800.0,
                    description = "Beautiful modern dual-burner gas stove featuring a highly resilient, high-contrast black tempered glass surface. Features automatic ignition (no matchsticks required), heavy brass burners for lifetime service, and a gas-saving burner design.",
                    stockStatus = "In Stock",
                    isFeatured = true,
                    weight = "Double Burner",
                    rating = 4.8f
                ),
                ProductEntity(
                    name = "RFL Premium Glass Double Burner Gas Stove",
                    category = "Gas Stoves",
                    brand = "RFL",
                    price = 3800.0,
                    originalPrice = 4500.0,
                    description = "Heavy-duty dual burner setup with thick high-performance tempered glass. Low gas consumption with high blue fire capability. Beautiful floral background accent design, rust-proof stainless steel frame base. Highly popular across Bangladesh.",
                    stockStatus = "In Stock",
                    isFeatured = true,
                    weight = "Double Burner",
                    rating = 4.9f
                ),
                ProductEntity(
                    name = "Miyako Stainless Steel Single Burner Gas Stove",
                    category = "Gas Stoves",
                    brand = "Miyako",
                    price = 1550.0,
                    originalPrice = 1900.0,
                    description = "Sturdy single-burner stove built fully in high-grade stainless steel. Highly portable, extremely easy to clean after messy cooking. Automatic piezoelectric ignition. Extremely safe and perfect for small families or compact rooms.",
                    stockStatus = "Limited Stock",
                    isFeatured = false,
                    weight = "Single Burner",
                    rating = 4.5f
                ),
                ProductEntity(
                    name = "Miyako Double Pot Rice Cooker 1.8L",
                    category = "Rice Cookers",
                    brand = "Miyako",
                    price = 2350.0,
                    originalPrice = 2800.0,
                    description = "Convenient 1.8 liters electric rice cooker including dual inner pots: one non-stick coated pot and one stainless steel pot. Automatic keep-warm feature keeps cooked rice tasty for hours. Energy-saving, robust safety fuse integration.",
                    stockStatus = "In Stock",
                    isFeatured = true,
                    weight = "1.8L Double Pot",
                    rating = 4.7f
                ),
                ProductEntity(
                    name = "Panasonic Smart Rice Cooker 2.2L",
                    category = "Rice Cookers",
                    brand = "Panasonic",
                    price = 4900.0,
                    originalPrice = 5600.0,
                    description = "Heavy anodized durable heating pan rice cooker from Panasonic. Offers massive 2.2L capacity with automatic multi-stage heat controls. Saves nutrition value of rice, includes steamer tray for preparing side dishes.",
                    stockStatus = "Limited Stock",
                    isFeatured = false,
                    weight = "2.2L Capacity",
                    rating = 4.9f
                ),
                ProductEntity(
                    name = "Miyako Stainless Steel Electric Kettle 1.5L",
                    category = "Electric Kettles",
                    brand = "Miyako",
                    price = 880.0,
                    originalPrice = 1200.0,
                    description = "Heavy duty 1500W electric kettle that boils water in under 3 minutes. Features a stainless steel internal body, cool-touch handle grip, three-layer safety auto cut-off on boil, and cordless base design for comfortable handling.",
                    stockStatus = "In Stock",
                    isFeatured = true,
                    weight = "1.5 Liters",
                    rating = 4.6f
                ),
                ProductEntity(
                    name = "Nova Cool-Touch Double Wall Kettle 2.0L",
                    category = "Electric Kettles",
                    brand = "Nova",
                    price = 1150.0,
                    originalPrice = 1500.0,
                    description = "Double walled thermal insulation electric kettle with stainless steel seamless cooking chamber. Outer plastic cover remains completely cool even when water is boiling inside, protecting your children from burns. Autoshutoff trigger.",
                    stockStatus = "In Stock",
                    isFeatured = false,
                    weight = "2.0 Liters",
                    rating = 4.5f
                ),
                ProductEntity(
                    name = "Sogo Touchscreen Infrared Cooker",
                    category = "Kitchen Appliances",
                    brand = "Sogo",
                    price = 3850.0,
                    originalPrice = 4800.0,
                    description = "Universal cooktop technology utilizing infrared heat waves. Compatible with ALL materials of cooking utensils, unlike induction. Features a gorgeous flat scratch-resistant ceramic glass display, smart touch slider pad, and 8 program pre-sets.",
                    stockStatus = "In Stock",
                    isFeatured = true,
                    weight = "2000W Power",
                    rating = 4.8f
                ),
                ProductEntity(
                    name = "Miyako Smart Induction Cooker",
                    category = "Kitchen Appliances",
                    brand = "Miyako",
                    price = 4150.0,
                    originalPrice = 5000.0,
                    description = "High efficiency smart induction cooker with touch panel. Saves up to 50% energy compared to traditional gas systems. Intelligent pan-sensing triggers, high speed boiling modes, child safety lock, elegant black look.",
                    stockStatus = "In Stock",
                    isFeatured = false,
                    weight = "Eco Friendly",
                    rating = 4.7f
                )
            )
            storeDao.insertProducts(initialCatalog)
        }
    }
}
