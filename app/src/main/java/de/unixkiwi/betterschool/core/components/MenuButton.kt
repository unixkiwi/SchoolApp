package de.unixkiwi.betterschool.core.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import de.unixkiwi.betterschool.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        shape = MaterialShapes.Cookie4Sided.toShape(),
        colors = IconButtonDefaults.iconButtonColors().copy(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.menu_24px),
            contentDescription = null
        )
    }
}