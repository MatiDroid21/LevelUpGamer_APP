package ui.profile

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: ProfileViewModel) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    var hasCamera by remember { mutableStateOf(false) }
    var hasRead by remember { mutableStateOf(false) }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamera = granted }

    val readPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

    val readPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasRead = granted }

    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok && pendingUri != null) {
            vm.setLastSavedPhoto(pendingUri)
            Toast.makeText(context, "Foto guardada en galería", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
        }
        pendingUri = null
    }

    Scaffold(
        containerColor = Color(0xFF181840),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PERFIL GAMER",
                        color = Color(0xFF42F5E3),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF222C44))
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF181840), Color(0xFF242851))
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Correo: ${ui.email ?: "No disponible"}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "UID: ${ui.uid ?: "No disponible"}",
                color = Color(0xFF42F5E3),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Nombre: ${ui.displayName ?: "No disponible"}",
                color = Color(0xFFDB26ED),
                style = MaterialTheme.typography.titleMedium
            )

            if (ui.lastSavedPhoto != null) {
                Image(
                    painter = rememberAsyncImagePainter(ui.lastSavedPhoto),
                    contentDescription = "Última foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFF222C44), shape = MaterialTheme.shapes.large)
                        .padding(8.dp)
                )
            }

            Button(
                onClick = {
                    if (!hasCamera) cameraPermLauncher.launch(Manifest.permission.CAMERA)
                    if (!hasRead) readPermLauncher.launch(readPerm)
                    val dest = vm.createDestinationUriForCurrentUser(context)
                    if (dest == null) {
                        vm.setError("No se pudo crear destino (UID no disponible)")
                        return@Button
                    }
                    pendingUri = dest
                    takePictureLauncher.launch(dest)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BA1FF)),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Tomar foto y guardar en galería", color = Color.White)
            }

            ui.error?.let {
                Text(it, color = Color(0xFFFF357A), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
