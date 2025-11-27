package com.liufeng.ballfight

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.*

object LocalizationUtil {
    fun applyLanguage(context: Context): Context {
        val locale = getSavedLocale(context)
        return updateResources(context, locale)
    }

    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.locales = localeList
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }
}