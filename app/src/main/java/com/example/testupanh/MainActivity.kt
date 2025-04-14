package com.example.testupanh

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.widget.Toast  // <- Thêm dòng này trên đầu file

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp


import coil.compose.rememberAsyncImagePainter
import com.example.testupanh.network.RetrofitInstance

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var imageUri by remember { mutableStateOf<Uri?>(null) }

            // Launcher để mở thư viện chọn ảnh
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                imageUri = uri
            }

            // Giao diện đơn giản
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    imagePickerLauncher.launch("image/*")
                }) {
                    Text(text = "Chọn ảnh")
                }
                Button(
                    onClick = {
                        imageUri?.let {
                            uploadImageToCloudinary(context = this@MainActivity, imageUri = it)
                        } ?: Log.e("UPLOAD", "❗ imageUri null, chưa chọn ảnh")
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Upload ảnh")
                }


                Spacer(modifier = Modifier.height(20.dp))

                imageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Ảnh đã chọn",
                        modifier = Modifier
                            .height(300.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    private fun uploadImageToCloudinary(context: Context, imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)

                if (inputStream == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Không thể mở input stream từ Uri", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val requestBody = inputStream.readBytes()
                    .toRequestBody("image/*".toMediaTypeOrNull())

                val part = MultipartBody.Part.createFormData(
                    "file", "image.jpg", requestBody
                )

                val uploadPreset = "my_unsigned_preset" // Đảm bảo preset là unsigned
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val response = RetrofitInstance.api.uploadImage(part, uploadPreset)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.secure_url
                        Toast.makeText(context, "Ảnh đã upload thành công: $imageUrl", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Upload thất bại: ${response.code()}- ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
//                        Log.e("UPLOAD", "❌ Upload thất bại: ${response.code()} - ${response.errorBody()?.string()}")

                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
