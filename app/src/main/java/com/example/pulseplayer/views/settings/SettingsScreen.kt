package com.example.pulseplayer.views.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulseplayer.data.local.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    var displayName by remember { mutableStateOf(UserPreferences.getDisplayName(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B1A))
            )
        },
        containerColor = Color(0xFF060911)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121B33)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tu perfil", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Dispositivo: $deviceName", color = Color.White.copy(alpha = 0.74f))
                    Text("¿Cómo te gustaría que te llame?", color = Color.White.copy(alpha = 0.9f))
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White),
                        placeholder = { Text("Ej: Alex", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF60A5FA),
                            focusedBorderColor = Color(0xFF60A5FA),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.28f)
                        )
                    )
                    Button(
                        onClick = { UserPreferences.setDisplayName(context, displayName) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar nombre")
                    }
                }
            }
        }
    }
}
