package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ProductEditorState

private val unitOptions = listOf("pcs", "kg", "liter", "renceng", "botol", "pouch", "karung", "tabung")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductEditorSheet(
  editorState: ProductEditorState,
  duplicateBarcodeProductName: String?,
  onNameChange: (String) -> Unit,
  onPriceChange: (String) -> Unit,
  onUnitChange: (String) -> Unit,
  onBarcodeChange: (String) -> Unit,
  onAddToCartToggle: (Boolean) -> Unit,
  onScanBarcodeClick: () -> Unit,
  onSave: () -> Unit,
  onDelete: (Long) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var showDeleteConfirm by remember { mutableStateOf(false) }

  val isEditing = editorState.existingId != null
  val parsedPrice = editorState.priceText.toLongOrNull() ?: 0L
  val isValid =
    editorState.name.trim().isNotEmpty() &&
      parsedPrice > 0L &&
      duplicateBarcodeProductName == null

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            when {
              isEditing -> "Ubah Barang"
              editorState.isFromUnknownCheckoutScan -> "Barang Baru Terdeteksi"
              else -> "Daftarkan Barang"
            },
          style = MaterialTheme.typography.headlineSmall,
        )
        if (isEditing) {
          IconButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.testTag("delete_product_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = "Hapus barang",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      if (editorState.isFromUnknownCheckoutScan && !isEditing) {
        Surface(
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.secondaryContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "Kode batang belum terdaftar. Lengkapi nama & harga untuk mendaftarkan barang.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(12.dp),
          )
        }
      }

      // Barcode field + dedicated Scan button right at top or with form so user can scan first!
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = editorState.barcode,
            onValueChange = onBarcodeChange,
            label = { Text("Kode Batang (Opsional)") },
            placeholder = { Text("Scan atau ketik kode") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Filled.QrCode2,
                contentDescription = null,
              )
            },
            trailingIcon = {
              if (editorState.barcode.isNotEmpty()) {
                IconButton(onClick = { onBarcodeChange("") }) {
                  Icon(imageVector = Icons.Filled.Close, contentDescription = "Hapus kode batang")
                }
              }
            },
            isError = duplicateBarcodeProductName != null,
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("product_barcode_input"),
            shape = MaterialTheme.shapes.small,
          )

          FilledTonalButton(
            onClick = onScanBarcodeClick,
            modifier = Modifier.height(56.dp).testTag("editor_scan_barcode_button"),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 14.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.QrCodeScanner,
              contentDescription = "Scan kode batang",
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Scan")
          }
        }

        if (duplicateBarcodeProductName != null) {
          Text(
            text = "Kode batang ini sudah terdaftar untuk \"$duplicateBarcodeProductName\".",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }

      OutlinedTextField(
        value = editorState.name,
        onValueChange = onNameChange,
        label = { Text("Nama Barang") },
        placeholder = { Text("Contoh: Minyak Goreng 1 L") },
        singleLine = true,
        keyboardOptions =
          KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next,
          ),
        modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
        shape = MaterialTheme.shapes.small,
      )

      OutlinedTextField(
        value = editorState.priceText,
        onValueChange = onPriceChange,
        label = { Text("Harga Jual") },
        prefix = { Text("Rp ") },
        placeholder = { Text("15000") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag("product_price_input"),
        shape = MaterialTheme.shapes.small,
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Satuan",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          unitOptions.forEach { unit ->
            FilterChip(
              selected = editorState.unit == unit,
              onClick = { onUnitChange(unit) },
              label = { Text(unit) },
            )
          }
        }
      }

      if (!isEditing) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .clickable { onAddToCartToggle(!editorState.addToCartOnSave) }
              .padding(vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Checkbox(
            checked = editorState.addToCartOnSave,
            onCheckedChange = onAddToCartToggle,
            modifier = Modifier.testTag("add_to_cart_after_save_checkbox"),
          )
          Text(
            text = "Langsung masukkan ke keranjang kasir setelah disimpan",
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }

      Spacer(modifier = Modifier.height(2.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier.weight(1f).height(48.dp).testTag("cancel_product_button"),
          shape = MaterialTheme.shapes.small,
        ) {
          Text("Batal")
        }
        Button(
          onClick = onSave,
          enabled = isValid,
          modifier = Modifier.weight(1f).height(48.dp).testTag("save_product_button"),
          shape = MaterialTheme.shapes.small,
        ) {
          Text(if (editorState.addToCartOnSave && !isEditing) "Simpan & Kasirkan" else "Simpan")
        }
      }
    }
  }

  val existingId = editorState.existingId
  if (showDeleteConfirm && existingId != null) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("Hapus Barang?") },
      text = { Text("\"${editorState.name}\" akan dihapus dari katalog barang.") },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteConfirm = false
            onDelete(existingId)
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
          Text("Hapus")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
      },
    )
  }
}
