package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.database.CartItemWithProduct
import com.example.database.InquiryEntity
import com.example.database.ProductEntity
import com.example.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder

sealed interface AppScreen {
    object Catalog : AppScreen
    object Cart : AppScreen
    object Contact : AppScreen
}

class StoreViewModel(private val repository: StoreRepository) : ViewModel() {

    // Current Active Screen
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Catalog)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Search and Filter parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Selected product for the quick view detail sheet/dialog
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Combined/Filtered Products list
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesCategory = (category == "All") || (product.category == category)
            val matchesQuery = product.name.contains(query, ignoreCase = true) || 
                               product.brand.contains(query, ignoreCase = true) ||
                               product.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Featured products
    val featuredProducts: StateFlow<List<ProductEntity>> = repository.featuredProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Cart Items flow
    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.cartItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Local submissions list for support/references
    val inquiriesFlow: StateFlow<List<InquiryEntity>> = repository.allInquiries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Contact Form Fields
    val contactName = MutableStateFlow("")
    val contactPhone = MutableStateFlow("")
    val contactMessage = MutableStateFlow("")

    private val _isSubmittingInquiry = MutableStateFlow(false)
    val isSubmittingInquiry: StateFlow<Boolean> = _isSubmittingInquiry.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateDatabaseIfNeeded()
        }
    }

    // Screen navigation
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Setters for searches and categories
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
    }

    // Cart operations
    fun addToCart(product: ProductEntity, option: String, quantity: Int = 1, context: Context) {
        viewModelScope.launch {
            repository.addProductToCart(product.id, option, quantity)
            Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateCartQuantity(cartItemId: Int, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(cartItemId, newQuantity)
        }
    }

    fun removeCartItem(cartItemId: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(cartItemId)
        }
    }

    fun clearCartItems() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Dial / Call Owner Action
    fun triggerCallNow(context: Context) {
        try {
            val phoneNum = "tel:+8801725996712"
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNum)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer, call +8801725996712", Toast.LENGTH_LONG).show()
        }
    }

    // Launch WhatsApp Direct Chat with general message
    fun triggerWhatsAppGeneral(context: Context) {
        val baseText = "Assalamu Alaikum, I want to order from Mesars Bogdadi Traders."
        triggerWhatsAppMessage(context, baseText)
    }

    // Private core method to dispatch WhatsApp URIs
    private fun triggerWhatsAppMessage(context: Context, messageText: String) {
        try {
            val number = "8801725996712" // Without plus sign
            val encodedMsg = URLEncoder.encode(messageText, "UTF-8")
            val url = "https://api.whatsapp.com/send?phone=$number&text=$encodedMsg"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp. Opening standard browser...", Toast.LENGTH_SHORT).show()
            try {
                val fallbackUrl = "https://wa.me/8801725996712?text=${URLEncoder.encode(messageText, "UTF-8")}"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp link failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Build unique pre-filled WhatsApp message for a single Product Purchase
    fun orderSingleProductOnWhatsApp(product: ProductEntity, option: String, context: Context) {
        val msg = """
            Assalamu Alaikum, I want to order from Mesars Bogdadi Traders.
            
            *Product:* ${product.name}
            *Category:* ${product.category}
            *Package/Option:* $option
            *Price:* BDT ${product.price}
            
            Please confirm my order and share delivery details!
        """.trimIndent()
        triggerWhatsAppMessage(context, msg)
    }

    // Build pre-filled WhatsApp checkout order details for full Cart contents
    fun checkoutCartOnWhatsApp(items: List<CartItemWithProduct>, context: Context) {
        if (items.isEmpty()) {
            Toast.makeText(context, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = items.sumOf { (it.product?.price ?: 0.0) * it.cartItem.quantity }
        
        val sb = StringBuilder()
        sb.append("Assalamu Alaikum, I want to order from Mesars Bogdadi Traders.\n\n")
        sb.append("🛒 *ORDER DETAILS:*\n")
        
        items.forEachIndexed { index, itemWithProduct ->
            val prod = itemWithProduct.product
            val cartItem = itemWithProduct.cartItem
            if (prod != null) {
                sb.append("${index + 1}. *${prod.name}*\n")
                sb.append("   - Option: ${cartItem.selectedOption}\n")
                sb.append("   - Quantity: ${cartItem.quantity} x BDT ${prod.price}\n")
                sb.append("   - Subtotal: BDT ${prod.price * cartItem.quantity}\n\n")
            }
        }
        
        sb.append("====================\n")
        sb.append("*Grand Total:* BDT $totalAmount\n")
        sb.append("====================\n\n")
        sb.append("Please verify my checkout list and contact me for the cash-on-delivery process.")

        triggerWhatsAppMessage(context, sb.toString())
    }

    // Submit Support/Feedback Contact Inquiry locally
    fun submitContactInquiry(context: Context) {
        val name = contactName.value.trim()
        val phone = contactPhone.value.trim()
        val msg = contactMessage.value.trim()

        if (name.isEmpty() || phone.isEmpty() || msg.isEmpty()) {
            Toast.makeText(context, "Please fill in all empty fields", Toast.LENGTH_SHORT).show()
            return
        }

        _isSubmittingInquiry.value = true
        viewModelScope.launch {
            try {
                repository.submitInquiry(name, phone, msg)
                Toast.makeText(context, "Inquiry saved successfully! Senders list updated.", Toast.LENGTH_LONG).show()
                
                // Also trigger WhatsApp to send the inquiry details directly to Rofiqul Islam! 
                // That translates the static form submission into direct owner feedback on WhatsApp! Awesome!
                val waMsg = """
                    *New Inquiry for Mesars Bogdadi Traders:*
                    
                    *Client Name:* $name
                    *Client Phone:* $phone
                    *Message:* $msg
                """.trimIndent()
                
                triggerWhatsAppMessage(context, waMsg)

                // Clear input boxes
                contactName.value = ""
                contactPhone.value = ""
                contactMessage.value = ""
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving inquiry: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isSubmittingInquiry.value = false
            }
        }
    }
}

class StoreViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
