package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.database.CartItemWithProduct
import com.example.database.InquiryEntity
import com.example.database.ProductEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainStoreScreen(viewModel: StoreViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    
    // Total quantity in cart for the navigation badge
    val cartCount = cartItems.sumOf { it.cartItem.quantity }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            StickyPremiumHeader(viewModel = viewModel)
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen is AppScreen.Catalog,
                    onClick = { viewModel.navigateTo(AppScreen.Catalog) },
                    icon = { Icon(Icons.Filled.Storefront, contentDescription = "Shop Catalog") },
                    label = { Text("Catalog") },
                    modifier = Modifier.testTag("nav_catalog")
                )
                NavigationBarItem(
                    selected = currentScreen is AppScreen.Cart,
                    onClick = { viewModel.navigateTo(AppScreen.Cart) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text(cartCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Shopping Cart")
                        }
                    },
                    label = { Text("Cart") },
                    modifier = Modifier.testTag("nav_cart")
                )
                NavigationBarItem(
                    selected = currentScreen is AppScreen.Contact,
                    onClick = { viewModel.navigateTo(AppScreen.Contact) },
                    icon = { Icon(Icons.Filled.ContactPage, contentDescription = "Contact Page") },
                    label = { Text("Contact Us") },
                    modifier = Modifier.testTag("nav_contact")
                )
            }
        },
        floatingActionButton = {
            // Floating WhatsApp Button present on every screen
            FloatingActionButton(
                onClick = { viewModel.triggerWhatsAppGeneral(context) },
                containerColor = Color(0xFF25D366), // Official WhatsApp Green
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("floating_whatsapp_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "Quick WhatsApp Order",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is AppScreen.Catalog -> CatalogView(viewModel = viewModel)
                    is AppScreen.Cart -> CartView(viewModel = viewModel)
                    is AppScreen.Contact -> ContactView(viewModel = viewModel)
                }
            }
        }
    }

    // Detail Dialog overlay when a product is clicked
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    selectedProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { viewModel.selectProduct(null) },
            onAddToCart = { option, qty ->
                viewModel.addToCart(product, option, qty, context)
                viewModel.selectProduct(null)
            },
            onOrderWhatsApp = { option ->
                viewModel.orderSingleProductOnWhatsApp(product, option, context)
            }
        )
    }
}

// STICKY PREMIUM HEADER COMPONENT
@Composable
fun StickyPremiumHeader(viewModel: StoreViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Micro Top Info Contact Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Phone icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+8801725996712",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Owner icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Proprietor: Rofiqul Islam",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            // Brand Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mesars Bogdadi Traders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "মেসার্স বোগদাদী ট্রেডার্স",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // CTA Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { viewModel.triggerCallNow(context) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call Store Owner Now",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = { viewModel.triggerWhatsAppGeneral(context) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF25D366)
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "WhatsApp Store Chat",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// 1. CATALOG TAB SCREEN
@Composable
fun CatalogView(viewModel: StoreViewModel, modifier: Modifier = Modifier) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val featuredProducts by viewModel.featuredProducts.collectAsStateWithLifecycle()

    val categories = listOf("All", "LPG Cylinders", "Gas Stoves", "Rice Cookers", "Electric Kettles", "Kitchen Appliances")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Hero Trust Section Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ESTABLISHED & TRUSTED DEALER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Serving customers with trusted LPG and home appliance solutions under the leadership of Rofiqul Islam.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = "Top brands like Beximco, Bashundhara, Miyako & Panasonic delivered right to your doorstep across Bangladesh.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Trust Badges Grid
        item {
            TrustBadgesSection()
        }

        // Search Bar Row
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search LPG or Appliances...") },
                placeholder = { Text("Search brand name, cylinder size...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_search_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Categories List (Horizontal Chips)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Product Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Products Display Header
        item {
            Text(
                text = if (selectedCategory == "All") "Feature Products Catalog" else "$selectedCategory Products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Product Cards Grid alternative inside LazyColumn
        if (filteredProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Store,
                            contentDescription = "Empty state icon",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No matching items found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try clearing search filters or search a different keyword.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            // Group lists in pairs for grid styling or write separate items
            val chunked = filteredProducts.chunked(2)
            items(chunked) { rowProducts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Display product 1
                    val prod1 = rowProducts[0]
                    Box(modifier = Modifier.weight(1f)) {
                        ProductCard(
                            product = prod1,
                            onClick = { viewModel.selectProduct(prod1) }
                        )
                    }

                    // Display product 2 if exists
                    if (rowProducts.size > 1) {
                        val prod2 = rowProducts[1]
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(
                                product = prod2,
                                onClick = { viewModel.selectProduct(prod2) }
                            )
                        }
                    } else {
                        // Invisible spacer box to align grid perfectly
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// TRUST BADGES GRIDS
@Composable
fun TrustBadgesSection(modifier: Modifier = Modifier) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustBadgeItem(
                icon = Icons.Filled.Verified,
                label = "Trusted\nBusiness",
                color = MaterialTheme.colorScheme.primary
            )
            TrustBadgeItem(
                icon = Icons.Filled.LocalShipping,
                label = "Fast\nDelivery",
                color = MaterialTheme.colorScheme.secondary
            )
            TrustBadgeItem(
                icon = Icons.Filled.Payments,
                label = "Secure\nPayments",
                color = Color(0xFF2E7D32)
            )
            TrustBadgeItem(
                icon = Icons.Filled.SupportAgent,
                label = "Active\nSupport",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun TrustBadgeItem(icon: ImageVector, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(72.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

// INDIVIDUAL PRODUCT CARD
@Composable
fun ProductCard(product: ProductEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // Product Category tag & weight atop beautiful colored container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.01f)
                            )
                        )
                    )
            ) {
                // Large styled initial letters/icon or generic drawing to look spectacular
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val illustrationIcon = when (product.category) {
                        "LPG Cylinders" -> Icons.Filled.LocalGasStation
                        "Gas Stoves" -> Icons.Filled.Whatshot
                        "Rice Cookers" -> Icons.Filled.Microwave
                        "Electric Kettles" -> Icons.Filled.SoupKitchen
                        else -> Icons.Filled.Kitchen
                    }
                    Icon(
                        imageVector = illustrationIcon,
                        contentDescription = product.name,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Superimposed badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = product.brand,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 9.sp
                        )
                    }

                    if (product.originalPrice > product.price) {
                        val savingsPercent = (((product.originalPrice - product.price) / product.originalPrice) * 105).toInt()
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SAVE ${savingsPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            // Product Details Block
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating star",
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${product.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "• ${product.weight}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        if (product.originalPrice > product.price) {
                            Text(
                                text = "BDT ${product.originalPrice.toInt()}",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                        Text(
                            text = "BDT ${product.price.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    FilledIconButton(
                        onClick = onClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Details",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// 2. SHOPPING CART TAB VIEW
@Composable
fun CartView(viewModel: StoreViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val totalAmount = cartItems.sumOf { (it.product?.price ?: 0.0) * it.cartItem.quantity }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shopping Basket",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (cartItems.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearCartItems() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear All")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Cart")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.RemoveShoppingCart,
                        contentDescription = "Empty Basket",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Your basket is empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Go to the product catalog and select items to order on WhatsApp.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.Catalog) },
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explore Products")
                    }
                }
            }
        } else {
            // Cart items list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { itemWithProduct ->
                    CartItemRow(
                        itemWithProduct = itemWithProduct,
                        onIncrease = { 
                            viewModel.updateCartQuantity(itemWithProduct.cartItem.id, itemWithProduct.cartItem.quantity + 1) 
                        },
                        onDecrease = {
                            viewModel.updateCartQuantity(itemWithProduct.cartItem.id, itemWithProduct.cartItem.quantity - 1)
                        },
                        onDelete = {
                            viewModel.removeCartItem(itemWithProduct.cartItem.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtotal/Receipt details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                        Text("BDT ${totalAmount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shipping Delivery", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Cash on Delivery (Flexible)", 
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "BDT ${totalAmount.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // checkout Button
            Button(
                onClick = { viewModel.checkoutCartOnWhatsApp(cartItems, context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_order_whatsapp_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat, 
                    contentDescription = "Order on WhatsApp",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ORDER ON WHATSAPP", 
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    itemWithProduct: CartItemWithProduct,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    val prod = itemWithProduct.product
    val cartItem = itemWithProduct.cartItem

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon representing Category
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (prod?.category) {
                        "LPG Cylinders" -> Icons.Filled.LocalGasStation
                        "Gas Stoves" -> Icons.Filled.Whatshot
                        "Rice Cookers" -> Icons.Filled.Microwave
                        "Electric Kettles" -> Icons.Filled.SoupKitchen
                        else -> Icons.Filled.Kitchen
                    },
                    contentDescription = prod?.name ?: "Product",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Middle Column with name, option & individual price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prod?.name ?: "Unknown Product",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Option: ${cartItem.selectedOption}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "BDT ${prod?.price?.toInt() ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Right side column containing controls and subtotal
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove Item",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease Quantity",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "${cartItem.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase Quantity",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// 3. CONTACT TAB VIEW
@Composable
fun ContactView(viewModel: StoreViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val contactName by viewModel.contactName.collectAsStateWithLifecycle()
    val contactPhone by viewModel.contactPhone.collectAsStateWithLifecycle()
    val contactMessage by viewModel.contactMessage.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingInquiry.collectAsStateWithLifecycle()
    val inquiries by viewModel.inquiriesFlow.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Business Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Official Profile Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Business, contentDescription = "Firm")
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Company Name", style = MaterialTheme.typography.labelSmall)
                                Text("Mesars Bogdadi Traders (মেসার্স বোগদাদী ট্রেডার্স)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = "Proprietor")
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Proprietor", style = MaterialTheme.typography.labelSmall)
                                Text("Rofiqul Islam", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PhoneAndroid, contentDescription = "Contacts")
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Phone & WhatsApp Support", style = MaterialTheme.typography.labelSmall)
                                Text("+8801725996712", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, contentDescription = "Hours")
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Business Hours", style = MaterialTheme.typography.labelSmall)
                                Text("9:00 AM - 10:00 PM (Daily Support)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Direct Quick Action CTAs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerCallNow(context) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "Call")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Store")
                        }

                        Button(
                            onClick = { viewModel.triggerWhatsAppGeneral(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = "WhatsApp")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp")
                        }
                    }
                }
            }
        }

        // Custom Simulated High-Fidelity Google Maps Vector drawing
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Store Location Map (Bangladesh)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Visit us directly to purchase: Mesars Bogdadi Traders, reliable delivery across Bangladesh.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                SimulatedGoogleMapCanvas()
            }
        }

        // Contact Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Send Offline Inquiry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Send design, orders, or gas dealership queries. Submitting will also option to send details directly to Owner's WhatsApp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { viewModel.contactName.value = it },
                        label = { Text("Your Complete Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("contact_name_input")
                    )

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { viewModel.contactPhone.value = it },
                        label = { Text("Your Contact Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("contact_phone_input")
                    )

                    OutlinedTextField(
                        value = contactMessage,
                        onValueChange = { viewModel.contactMessage.value = it },
                        label = { Text("Describe Your Question / Ordered Cylinder") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("contact_message_input")
                    )

                    Button(
                        onClick = { viewModel.submitContactInquiry(context) },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_contact_form_btn")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Filled.Send, contentDescription = "Send Email Inquiry")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit to Owner")
                        }
                    }
                }
            }
        }

        // Locally saved Inquiry History Feed
        if (inquiries.isNotEmpty()) {
            item {
                Text(
                    text = "Your Submitted Inquiries (${inquiries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(inquiries) { inq ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(inq.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(inq.phone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(inq.message, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "Reference Saved Locally",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        // Footer Section Requirements
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Mesars Bogdadi Traders",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Proprietor: Rofiqul Islam",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Phone: +8801725996712 | WhatsApp: +8801725996712",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "© 2026 - All Rights Reserved.",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

// SIMULATED GOOGLE MAP OUTLINE CANVAS DESIGN
@Composable
fun SimulatedGoogleMapCanvas(modifier: Modifier = Modifier) {
    val roadColor = Color(0xFFEEEEEE)
    val waterColor = Color(0xFFBBDEFB)
    val parkColor = Color(0xFFC8E6C9)
    val gridColor = Color(0xFFCFD8DC)
    val fontColor = Color(0xFF37474F)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
    ) {
        // 1. Draw solid background represents grey land mass
        drawRect(Color(0xFFF5F5F5))

        // 2. Draw active Park areas
        drawRoundRect(
            color = parkColor,
            topLeft = Offset(10f, 10f),
            size = Size(100f, 120f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
        )
        drawCircle(
            color = parkColor,
            center = Offset(size.width - 150f, 40f),
            radius = 35f
        )

        // 3. Draw active River / Lake water
        val lakePath = Path().apply {
            moveTo(0f, size.height - 30f)
            quadraticTo(
                size.width * 0.4f, size.height - 60f,
                size.width * 0.6f, size.height
            )
            lineTo(0f, size.height)
            close()
        }
        drawPath(lakePath, color = waterColor)

        // 4. Draw Map Street Grids
        // Horizontal main street
        drawRect(roadColor, Offset(0f, size.height * 0.5f - 18f), Size(size.width, 36f))
        // Vertical main street
        drawRect(roadColor, Offset(size.width * 0.35f - 18f, 0f), Size(36f, size.height))
        // Diagonal bypass road
        drawLine(
            roadColor,
            Offset(0f, 10f),
            Offset(size.width, size.height - 20f),
            strokeWidth = 24f
        )

        // Street division dashed vectors
        val stroke = Stroke(width = 1.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        drawLine(
            gridColor,
            Offset(0f, size.height * 0.5f),
            Offset(size.width, size.height * 0.5f),
            pathEffect = stroke.pathEffect,
            strokeWidth = stroke.width
        )
        drawLine(
            gridColor,
            Offset(size.width * 0.35f, 0f),
            Offset(size.width * 0.35f, size.height),
            pathEffect = stroke.pathEffect,
            strokeWidth = stroke.width
        )

        // 5. Drawing Map Pin Drop with glowing aura representing Mesars Bogdadi Traders Location
        val centerPin = Offset(size.width * 0.35f, size.height * 0.5f)
        // Draw pulse circle shadow
        drawCircle(
            color = Color(0xFFFF5722).copy(alpha = 0.2f),
            center = centerPin,
            radius = 32f
        )
        // Draw pin core point
        drawCircle(
            color = Color(0xFFFF5722),
            center = centerPin,
            radius = 7f
        )
        // Draw tear pin icon
        val pinPath = Path().apply {
            moveTo(centerPin.x, centerPin.y)
            cubicTo(
                centerPin.x - 14f, centerPin.y - 14f,
                centerPin.x - 14f, centerPin.y - 32f,
                centerPin.x, centerPin.y - 32f
            )
            cubicTo(
                centerPin.x + 14f, centerPin.y - 32f,
                centerPin.x + 14f, centerPin.y - 14f,
                centerPin.x, centerPin.y
            )
            close()
        }
        drawPath(pinPath, color = Color(0xFFFF5722))
        // Inner white point of the pin
        drawCircle(
            color = Color.White,
            center = Offset(centerPin.x, centerPin.y - 21f),
            radius = 4f
        )
    }
}

// 4. PRODUCT QUICK DETAILS DIALOG SCREEN
@Composable
fun ProductDetailDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onAddToCart: (selectedOption: String, qty: Int) -> Unit,
    onOrderWhatsApp: (selectedOption: String) -> Unit
) {
    // Generate logical packaging options based on product category
    val optionsList = when (product.category) {
        "LPG Cylinders" -> listOf("Full Cylinder + Gas Package", "Refill Gas ONLY (Bring Empty Cylinder)", "Premium Brass Safety Regulator Combo")
        "Gas Stoves" -> listOf("Single Complete Unit", "Unit + LPG Gas Connection Hose Package", "Double Unit Value Saver")
        else -> listOf("Standard Unit Package", "Unit + 1-Year Official Extended Warranty Bundle")
    }

    var selectedOption by remember { mutableStateOf(optionsList[0]) }
    var buyQuantity by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Diagonal header action
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close dialog")
                        }
                    }
                }

                // Title, rating and spec badges
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Brand: ${product.brand}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text("•", color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = product.weight,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text("•", color = MaterialTheme.colorScheme.outline)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = "*", tint = Color(0xFFFFA000), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${product.rating}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Price display with discount details
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "BDT ${product.price.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (product.originalPrice > product.price) {
                                    Text(
                                        text = "BDT ${product.originalPrice.toInt()}",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        )
                                    )
                                }
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (product.stockStatus == "In Stock") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                )
                            ) {
                                Text(
                                    text = product.stockStatus,
                                    color = if (product.stockStatus == "In Stock") Color(0xFF2E7D32) else Color(0xFFC62828),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Description
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Product Specifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // Package option list selectors
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Select Package Option", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        optionsList.forEach { option ->
                            val isChosen = option == selectedOption
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isChosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isChosen) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedOption = option }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(
                                    selected = isChosen,
                                    onClick = { selectedOption = option }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // Quantity selector & checkout buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order Quantity", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (buyQuantity > 1) buyQuantity-- },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Minus")
                            }
                            Text(
                                text = "$buyQuantity",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { buyQuantity++ },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Plus")
                            }
                        }
                    }
                }

                // Actions buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onAddToCart(selectedOption, buyQuantity) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("modal_add_to_cart_btn")
                        ) {
                            Icon(Icons.Filled.AddShoppingCart, contentDescription = "Add Basket")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADD TO BASKET BASKET", fontWeight = FontWeight.Bold) // Custom distinct tag info
                        }

                        Button(
                            onClick = { onOrderWhatsApp(selectedOption) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("modal_order_whatsapp_btn")
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = "WhatsApp Check", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ORDER ON WHATSAPP", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
