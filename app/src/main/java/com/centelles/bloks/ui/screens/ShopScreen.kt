package com.centelles.bloks.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.centelles.bloks.R
import com.centelles.bloks.ui.components.BloomCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coins by viewModel.coins.collectAsState()
    val products by viewModel.products.collectAsState()
    val isRemoveAdsPurchased by viewModel.isRemoveAdsPurchased.collectAsState()
    val activeThemeId by viewModel.activeThemeId.collectAsState()
    val unlockedThemesMask by viewModel.unlockedThemesMask.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.shop_title), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    Text(
                        text = stringResource(R.string.common_coins_format, coins),
                        modifier = Modifier.padding(end = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.shop_free_coins),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            BloomCard {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.shop_watch_video), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(stringResource(R.string.shop_coins_reward), style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        val activity = context as? Activity
                        if (activity != null) {
                            viewModel.watchAdForCoins(activity)
                        }
                    }) {
                        Text(stringResource(R.string.shop_watch))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (products.isNotEmpty()) {
                val coinPacks = products.filter { it.productId.contains("coins") }
                if (coinPacks.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.shop_coin_packs),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(coinPacks) { pack ->
                            ShopItemCard(pack) {
                                val activity = context as? Activity
                                if (activity != null) {
                                    viewModel.buyProduct(activity, pack)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val removeAdsProduct = products.find { it.productId == "remove_ads" }
                if (removeAdsProduct != null && !isRemoveAdsPurchased) {
                    BloomCard {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.shop_remove_ads), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text(stringResource(R.string.shop_remove_ads_desc), style = MaterialTheme.typography.bodySmall)
                            }
                            Button(onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    viewModel.buyProduct(activity, removeAdsProduct)
                                }
                            }) {
                                Text(removeAdsProduct.oneTimePurchaseOfferDetails?.formattedPrice ?: stringResource(R.string.shop_buy))
                            }
                        }
                    }
                }
            } else {
                // If products are loading or empty, we can still show the Theme items since they are bought with coins!
                Text(
                    text = stringResource(R.string.shop_custom_themes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val themes = listOf(
                    Triple(0, stringResource(R.string.shop_theme_classic), 0),
                    Triple(1, stringResource(R.string.shop_theme_neon), 500),
                    Triple(2, stringResource(R.string.shop_theme_forest), 1000)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    themes.forEach { (id, name, cost) ->
                        val isUnlocked = (unlockedThemesMask and (1 shl id)) != 0
                        val isActive = activeThemeId == id
                        
                        BloomCard {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(if (isUnlocked) stringResource(R.string.shop_unlocked) else stringResource(R.string.shop_price_format, cost), style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = { viewModel.selectOrBuyTheme(id, cost) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text(if (isActive) stringResource(R.string.shop_active) else if (isUnlocked) stringResource(R.string.shop_activate) else stringResource(R.string.shop_buy))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopItemCard(product: com.android.billingclient.api.ProductDetails, onBuy: () -> Unit) {
    BloomCard {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🪙", fontSize = 40.sp)
            Text(text = product.productId.filter { it.isDigit() }.ifEmpty { stringResource(R.string.shop_pack) }, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text(text = stringResource(R.string.shop_coins), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBuy,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(product.oneTimePurchaseOfferDetails?.formattedPrice ?: stringResource(R.string.shop_buy))
            }
        }
    }
}
