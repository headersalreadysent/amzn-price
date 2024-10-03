package co.ec.amazonfiyattakip.ui.part

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.ui.LocalNavigation
import kotlinx.coroutines.launch

data class NavigationItem(val title: String, val destination: String, val icon: ImageVector)

val navItems = listOf<NavigationItem>(
    NavigationItem("Dashboard", "main", Icons.Filled.Home)
)


@Composable
fun ModalPart(drawerState: DrawerState) {

    val scope = rememberCoroutineScope()
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(.6F),
        windowInsets = WindowInsets.Companion.navigationBars
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
        ) {

            Text(
                "Amazon Fiyat Takibi", modifier = Modifier
                    .padding(16.dp)
                    .padding(top = 50.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        HorizontalDivider()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                val navigation= LocalNavigation.current
                navItems.forEach {
                    NavigationDrawerItem(
                        label = { Text(text = it.title) },
                        icon = {
                            Icon(it.icon, contentDescription = "${it.title} navigation item")
                        },
                        selected = false,
                        onClick = {
                            navigation.navigate(it.destination)
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Version 1.0",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "About",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}