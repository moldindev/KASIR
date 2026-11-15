package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val indonesiaLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()

fun formatRupiah(amount: Long): String {
  val formatter = NumberFormat.getNumberInstance(indonesiaLocale)
  val absText = formatter.format(kotlin.math.abs(amount))
  return if (amount < 0) "-Rp $absText" else "Rp $absText"
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
