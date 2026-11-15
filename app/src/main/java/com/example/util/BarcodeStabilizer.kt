package com.example.util

import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Normalizes raw ML Kit barcode strings and applies multi-frame consensus filtering
 * so transient camera/autofocus misreads and UPC-A vs EAN-13 formatting differences
 * never produce inconsistent values or redundant scan events.
 */
class BarcodeStabilizer(
  private val requiredConsecutiveMatches: Int = 3,
  private val matchWindowMillis: Long = 900L,
  private val cooldownMillis: Long = 2000L,
) {
  private var candidateCode: String = ""
  private var candidateCount: Int = 0
  private var candidateLastSeenAt: Long = 0L

  private var lastConfirmedCode: String = ""
  private var lastConfirmedAt: Long = 0L

  @Synchronized
  fun onFrameDetected(
    rawValue: String,
    format: Int,
    nowMillis: Long = System.currentTimeMillis(),
  ): String? {
    val normalized = normalizeBarcodeValue(rawValue, format)
    if (normalized.isEmpty()) return null
    if (!isValidBarcodeFormat(normalized, format)) return null

    if (normalized == candidateCode && (nowMillis - candidateLastSeenAt) <= matchWindowMillis) {
      candidateCount += 1
    } else {
      candidateCode = normalized
      candidateCount = 1
    }
    candidateLastSeenAt = nowMillis

    if (candidateCount < requiredConsecutiveMatches) {
      return null
    }

    // Prevent redundant triggers while the same barcode stays in front of the camera
    if (normalized == lastConfirmedCode && (nowMillis - lastConfirmedAt) <= cooldownMillis) {
      return null
    }

    lastConfirmedCode = normalized
    lastConfirmedAt = nowMillis
    candidateCode = ""
    candidateCount = 0
    return normalized
  }

  @Synchronized
  fun reset() {
    candidateCode = ""
    candidateCount = 0
    candidateLastSeenAt = 0L
    lastConfirmedCode = ""
    lastConfirmedAt = 0L
  }
}

/**
 * Strips invisible control/whitespace characters and normalizes 12-digit UPC-A barcodes
 * into canonical 13-digit GTIN-13 (`0` + UPC-A) so repeated scans decoded interchangeably
 * as UPC-A or EAN-13 always yield the exact same string.
 */
fun normalizeBarcodeValue(rawValue: String, format: Int): String {
  val clean = rawValue.filter { it.code in 33..126 }.trim()
  if (clean.isEmpty()) return ""
  return if (format == Barcode.FORMAT_UPC_A && clean.length == 12 && clean.all { it.isDigit() }) {
    "0$clean"
  } else {
    clean
  }
}

fun isValidBarcodeFormat(normalizedCode: String, format: Int): Boolean {
  return when (format) {
    Barcode.FORMAT_EAN_13 ->
      normalizedCode.length == 13 &&
        normalizedCode.all { it.isDigit() } &&
        hasValidGs1CheckDigit(normalizedCode)
    Barcode.FORMAT_UPC_A ->
      (normalizedCode.length == 13 || normalizedCode.length == 12) &&
        normalizedCode.all { it.isDigit() } &&
        hasValidGs1CheckDigit(normalizedCode)
    Barcode.FORMAT_EAN_8 ->
      normalizedCode.length == 8 &&
        normalizedCode.all { it.isDigit() } &&
        hasValidGs1CheckDigit(normalizedCode)
    Barcode.FORMAT_UPC_E ->
      normalizedCode.length in 6..8 && normalizedCode.all { it.isDigit() }
    Barcode.FORMAT_CODE_128 -> normalizedCode.length >= 6
    Barcode.FORMAT_QR_CODE -> normalizedCode.isNotEmpty()
    else -> true
  }
}

/**
 * Validates standard GS1 mod-10 check digit used by EAN-13, EAN-8, and UPC-A barcodes.
 */
fun hasValidGs1CheckDigit(digits: String): Boolean {
  if (digits.length < 2 || !digits.all { it.isDigit() }) return false
  val payload = digits.dropLast(1)
  val expectedCheck = digits.last().digitToInt()

  var sum = 0
  for (i in payload.indices) {
    val digit = payload[payload.length - 1 - i].digitToInt()
    val weight = if (i % 2 == 0) 3 else 1
    sum += digit * weight
  }
  val computedCheck = (10 - (sum % 10)) % 10
  return computedCheck == expectedCheck
}
