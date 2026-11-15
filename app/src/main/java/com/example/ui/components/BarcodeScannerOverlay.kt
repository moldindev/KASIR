package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.theme.PriceTextMedium
import com.example.ui.viewmodel.ScannerMode
import com.example.util.formatRupiah
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

@Composable
fun BarcodeScannerOverlay(
  scannerMode: ScannerMode,
  totalItems: Int,
  totalAmount: Long,
  scanBannerMessage: String?,
  onBarcodeDetected: (String) -> Unit,
  onClearBanner: () -> Unit,
  onOpenPayment: () -> Unit,
  onClose: () -> Unit,
) {
  BackHandler { onClose() }

  val context = LocalContext.current
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      hasCameraPermission = granted
    }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  LaunchedEffect(scanBannerMessage) {
    if (scanBannerMessage != null) {
      delay(2200)
      onClearBanner()
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = if (hasCameraPermission) Color.Black else MaterialTheme.colorScheme.background,
  ) {
    if (!hasCameraPermission) {
      CameraPermissionContent(
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onClose = onClose,
      )
    } else {
      ScannerCameraContent(
        scannerMode = scannerMode,
        totalItems = totalItems,
        totalAmount = totalAmount,
        scanBannerMessage = scanBannerMessage,
        onBarcodeDetected = onBarcodeDetected,
        onOpenPayment = onOpenPayment,
        onClose = onClose,
      )
    }
  }
}

@Composable
private fun CameraPermissionContent(
  onRequestPermission: () -> Unit,
  onClose: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.secondaryContainer,
          modifier = Modifier.size(64.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.CameraAlt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(32.dp),
            )
          }
        }
        Text(
          text = stringResource(R.string.camera_permission_title),
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center,
        )
        Text(
          text = stringResource(R.string.camera_permission_desc),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
        Button(
          onClick = onRequestPermission,
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("grant_camera_button"),
          shape = MaterialTheme.shapes.small,
        ) {
          Text(stringResource(R.string.camera_permission_button))
        }
        OutlinedButton(
          onClick = onClose,
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("close_permission_button"),
          shape = MaterialTheme.shapes.small,
        ) {
          Text("Kembali")
        }
      }
    }
  }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun ScannerCameraContent(
  scannerMode: ScannerMode,
  totalItems: Int,
  totalAmount: Long,
  scanBannerMessage: String?,
  onBarcodeDetected: (String) -> Unit,
  onOpenPayment: () -> Unit,
  onClose: () -> Unit,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val haptic = LocalHapticFeedback.current
  val currentMode by rememberUpdatedState(scannerMode)
  val currentOnBarcodeDetected by rememberUpdatedState(onBarcodeDetected)

  var torchEnabled by remember { mutableStateOf(false) }
  var boundCamera by remember { mutableStateOf<Camera?>(null) }
  var lastCode by remember { mutableStateOf("") }
  var lastDetectedAt by remember { mutableLongStateOf(0L) }
  var hasPickedSingleCode by remember { mutableStateOf(false) }

  val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
  val scanner = remember {
    val options =
      BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    BarcodeScanning.getClient(options)
  }

  DisposableEffect(Unit) {
    onDispose {
      scanner.close()
      cameraExecutor.shutdown()
    }
  }

  LaunchedEffect(torchEnabled, boundCamera) {
    boundCamera?.cameraControl?.enableTorch(torchEnabled)
  }

  Box(modifier = Modifier.fillMaxSize()) {
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { viewContext ->
        val previewView = PreviewView(viewContext)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
        cameraProviderFuture.addListener(
          {
            val cameraProvider = cameraProviderFuture.get()
            val preview =
              Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
              }

            val analysis =
              ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { imageAnalysis ->
                  imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                      val inputImage =
                        InputImage.fromMediaImage(
                          mediaImage,
                          imageProxy.imageInfo.rotationDegrees,
                        )
                      scanner
                        .process(inputImage)
                        .addOnSuccessListener { barcodes ->
                          val value = barcodes.firstOrNull()?.rawValue?.trim()
                          if (!value.isNullOrEmpty()) {
                            if (currentMode == ScannerMode.PICK_FOR_PRODUCT) {
                              if (!hasPickedSingleCode) {
                                hasPickedSingleCode = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentOnBarcodeDetected(value)
                              }
                            } else {
                              val now = System.currentTimeMillis()
                              if (value != lastCode || (now - lastDetectedAt) > 1500L) {
                                lastCode = value
                                lastDetectedAt = now
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentOnBarcodeDetected(value)
                              }
                            }
                          }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                    } else {
                      imageProxy.close()
                    }
                  }
                }

            try {
              cameraProvider.unbindAll()
              boundCamera =
                cameraProvider.bindToLifecycle(
                  lifecycleOwner,
                  CameraSelector.DEFAULT_BACK_CAMERA,
                  preview,
                  analysis,
                )
            } catch (_: Exception) {
              // Camera unavailable on device/emulator
            }
          },
          ContextCompat.getMainExecutor(viewContext),
        )
        previewView
      },
    )

    // Semi-transparent viewfinder cutout overlay
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawRect(color = Color.Black.copy(alpha = 0.48f))
      val boxWidth = size.width * 0.78f
      val boxHeight = boxWidth * 0.56f
      val left = (size.width - boxWidth) / 2f
      val top = (size.height - boxHeight) / 2f - 40.dp.toPx()

      drawRoundRect(
        color = Color.Transparent,
        topLeft = Offset(left, top),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
        blendMode = BlendMode.Clear,
      )

      drawRoundRect(
        color = Color(0xFFFAF7F2),
        topLeft = Offset(left, top),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
        style = Stroke(width = 3.dp.toPx()),
      )
    }

    // Top controls
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onClose,
        modifier =
          Modifier.size(48.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            .testTag("scanner_close_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Tutup pemindai",
          tint = Color.White,
        )
      }

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.55f),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text =
              if (scannerMode == ScannerMode.PICK_FOR_PRODUCT) "Scan Kode Batang Barang"
              else "Scan untuk Kasir",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
          )
        }
      }

      IconButton(
        onClick = { torchEnabled = !torchEnabled },
        modifier =
          Modifier.size(48.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            .testTag("scanner_torch_button"),
      ) {
        Icon(
          imageVector = if (torchEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
          contentDescription = "Lampu kilat",
          tint = if (torchEnabled) Color(0xFFFFD54F) else Color.White,
        )
      }
    }

    // Scan feedback pill (Checkout mode)
    AnimatedVisibility(
      visible = scanBannerMessage != null && scannerMode == ScannerMode.CHECKOUT,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.Center).padding(top = 150.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 6.dp,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = scanBannerMessage.orEmpty(),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }

    // Bottom bar tailored to ScannerMode
    Surface(
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(16.dp),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 8.dp,
    ) {
      if (scannerMode == ScannerMode.PICK_FOR_PRODUCT) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Arahkan kamera ke kode batang kemasan untuk diisi otomatis.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
          )
          OutlinedButton(
            onClick = onClose,
            modifier = Modifier.height(48.dp).testTag("scanner_cancel_pick_button"),
            shape = MaterialTheme.shapes.small,
          ) {
            Text("Batal")
          }
        }
      } else {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(
              text =
                if (totalItems == 0) "Scan barang untuk masuk keranjang"
                else "$totalItems barang di keranjang",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = formatRupiah(totalAmount),
              style = PriceTextMedium,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = onClose,
              modifier = Modifier.height(48.dp).testTag("scanner_done_button"),
              shape = MaterialTheme.shapes.small,
            ) {
              Text("Selesai")
            }
            if (totalItems > 0) {
              Button(
                onClick = {
                  onClose()
                  onOpenPayment()
                },
                modifier = Modifier.height(48.dp).testTag("scanner_pay_button"),
                shape = MaterialTheme.shapes.small,
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                  ),
              ) {
                Text("Bayar")
              }
            }
          }
        }
      }
    }
  }
}
