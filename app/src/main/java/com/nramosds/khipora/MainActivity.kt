package com.nramosds.khipora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nramosds.khipora.navigation.KhiporaApp
import com.nramosds.khipora.utils.AppTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { KhiporaApp(container) } }
    }
}
