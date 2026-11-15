package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

private val indonesiaLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()

fun formatRupiah(amount: Long): String {
  val formatter = NumberFormat.getNumberInstance(indonesiaLocale)
  val absText = formatter.format(abs(amount))
  return if (amount < 0) "-Rp $absText" else "Rp $absText"
}

fun formatQuarterQuantity(quarterUnits: Int): String {
  if (quarterUnits <= 0) return "0"
  val whole = quarterUnits / 4
  val rem = quarterUnits % 4
  val frac =
    when (rem) {
      1 -> "1/4"
      2 -> "1/2"
      3 -> "3/4"
      else -> ""
    }
  return when {
    whole == 0 -> frac
    frac.isEmpty() -> "$whole"
    else -> "$whole $frac"
  }
}

fun formatDecimalQuantity(quantity: Double): String {
  val roundedQuarters = round(quantity * 4.0).toInt()
  if (abs(quantity - roundedQuarters / 4.0) < 0.001) {
    return formatQuarterQuantity(roundedQuarters)
  }
  val formatted = String.format(indonesiaLocale, "%.2f", quantity).trimEnd('0').trimEnd(',', '.')
  return formatted.ifEmpty { "0" }
}

fun calculateFractionalSubtotal(unitPrice: Long, quarterUnits: Int): Long {
  if (quarterUnits <= 0 || unitPrice <= 0L) return 0L
  return (unitPrice * quarterUnits + 2L) / 4L
}

fun calculateDecimalSubtotal(unitPrice: Long, quantity: Double): Long {
  if (quantity <= 0.0 || unitPrice <= 0L) return 0L
  return round(unitPrice * quantity).toLong()
}

fun formatTimeOnly(timestamp: Long): String {
  val sdf = SimpleDateFormat("HH:mm", indonesiaLocale)
  return sdf.format(Date(timestamp))
}

fun formatFullDateTime(timestamp: Long): String {
  val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesiaLocale)
  return sdf.format(Date(timestamp))
}

enum class DateFilter(val label: String) {
  TODAY("Hari Ini"),
  YESTERDAY("Kemarin"),
  ALL("Semua"),
}

fun getDayRangeMillis(filter: DateFilter, nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
  if (filter == DateFilter.ALL) return 0L to Long.MAX_VALUE

  val cal = Calendar.getInstance()
  cal.timeInMillis = nowMillis
  cal.set(Calendar.HOUR_OF_DAY, 0)
  cal.set(Calendar.MINUTE, 0)
  cal.set(Calendar.SECOND, 0)
  cal.set(Calendar.MILLISECOND, 0)

  if (filter == DateFilter.YESTERDAY) {
    cal.add(Calendar.DAY_OF_YEAR, -1)
  }

  val start = cal.timeInMillis
  cal.add(Calendar.DAY_OF_YEAR, 1)
  val end = cal.timeInMillis - 1L
  return start to end
}
