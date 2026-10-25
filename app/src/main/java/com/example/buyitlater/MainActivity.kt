
package com.example.buyitlater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buyitlater.ui.theme.BuyItLaterTheme

data class ShoppingItem(
    val id: Int,
    val name: String,
    val price: Double,
    val productLink: String,
    val waitingHours: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BuyItLaterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BuyItLaterApp()
                }
            }
        }
    }
}

@Composable
fun BuyItLaterApp() {
    val wishlist = remember { mutableStateListOf<ShoppingItem>() }
    var showAddForm by remember { mutableStateOf(false) }
    var nextId by remember { mutableIntStateOf(1) }

    if (showAddForm) {
        AddItemScreen(
            onSave = { name, price, link, hours ->
                wishlist.add(
                    ShoppingItem(
                        id = nextId++,
                        name = name,
                        price = price,
                        productLink = link,
                        waitingHours = hours
                    )
                )
                showAddForm = false
            },
            onCancel = { showAddForm = false }
        )
    } else {
        HomeScreen(
            wishlist = wishlist,
            onAddItem = { showAddForm = true },
            onDeleteItem = { item -> wishlist.remove(item) }
        )
    }
}

@Composable
fun HomeScreen(
    wishlist: List<ShoppingItem>,
    onAddItem: () -> Unit,
    onDeleteItem: (ShoppingItem) -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = "Buy It Later",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Think before you spend.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "My Wishlist (${wishlist.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (wishlist.isEmpty()) {
                Text(
                    text = "Your wishlist is empty. Add something you're thinking about buying!",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(wishlist, key = { it.id }) { item ->
                        ShoppingItemCard(
                            item = item,
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddItem,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add Item", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun AddItemScreen(
    onSave: (String, Double, String, Int) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var productLink by remember { mutableStateOf("") }
    var waitingHours by remember { mutableIntStateOf(24) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add an Item",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Price (Rs.)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = productLink,
                onValueChange = { productLink = it },
                label = { Text("Product link (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Choose waiting period",
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = waitingHours == 24,
                    onClick = { waitingHours = 24 }
                )
                Text("24 hours")

                RadioButton(
                    selected = waitingHours == 168,
                    onClick = { waitingHours = 168 }
                )
                Text("7 days")
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()

                    errorMessage = when {
                        name.isBlank() ->
                            "Please enter a product name."
                        price == null || !price.isFinite() || price <= 0 ->
                            "Enter a valid price greater than zero."
                        productLink.isNotBlank() &&
                                !productLink.startsWith("https://", ignoreCase = true) &&
                                !productLink.startsWith("http://", ignoreCase = true) ->
                            "The product link must start with http:// or https://."
                        else -> null
                    }

                    if (errorMessage == null) {
                        onSave(
                            name.trim(),
                            price!!,
                            productLink.trim(),
                            waitingHours
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Item")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rs. ${"%,.2f".format(item.price)}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Waiting period: ${
                    if (item.waitingHours == 24) "24 hours" else "7 days"
                }",
                color = MaterialTheme.colorScheme.primary
            )

            if (item.productLink.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.productLink,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}
