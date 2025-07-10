package com.example.pulseplayer.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Eliminar",
    dismissText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss:() -> Unit
){
    AlertDialog(
        onDismiss,
        title = {
            Text(text = "Eliminar $title", color = Color.White)
        },
        text = {
            Text(text = "¿Estas seguro que quieres borrar $message?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText,color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, color = Color.White)
            }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}
