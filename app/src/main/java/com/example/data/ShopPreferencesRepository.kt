package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.shopPreferencesDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "shop_preferences")

class ShopPreferencesRepository(private val dataStore: DataStore<Preferences>) {
  val shopName: Flow<String> =
    dataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { preferences ->
        preferences[SHOP_NAME_KEY]?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_SHOP_NAME
      }

  suspend fun updateShopName(newName: String) {
    val cleanName = newName.trim().take(40).ifBlank { DEFAULT_SHOP_NAME }
    dataStore.edit { preferences -> preferences[SHOP_NAME_KEY] = cleanName }
  }

  companion object {
    const val DEFAULT_SHOP_NAME = "Kasir Sembako"
    private val SHOP_NAME_KEY = stringPreferencesKey("shop_name")

    fun fromContext(context: Context): ShopPreferencesRepository =
      ShopPreferencesRepository(context.applicationContext.shopPreferencesDataStore)
  }
}
