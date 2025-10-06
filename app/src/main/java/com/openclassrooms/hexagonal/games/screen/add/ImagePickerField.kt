package com.openclassrooms.hexagonal.games.screen.add

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.openclassrooms.hexagonal.games.R

@Composable
fun ImagePickerField() {

    val viewModel: AddViewModel = viewModel()
    var uploadUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Lanceur pour ouvrir la galerie
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != null) {
            isUploading = true
            uploadUrl = viewModel.uploadImageToFirebase(selectedImageUri!!)
            isUploading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aperçu de l'image
        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = stringResource(R.string.preview_photo),
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // 🔄 Indicateur pendant l’upload
        if (isUploading) {
            CircularProgressIndicator()
        }

        // ✅ Message après upload
        if (uploadUrl != null && !isUploading) {
            Text(
                text = stringResource(R.string.uploaded_photo),
                color = Color.Green
            )
        }

        // 🔹 Champ cliquable pour ouvrir la galerie
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0E0E0))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (selectedImageUri == null)
                    stringResource(R.string.select_photo)
                else
                    stringResource(R.string.change_photo),
                color = Color.DarkGray
            )
        }
    }
}
