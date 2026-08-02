package com.whistle

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.whistle.MainActivity.Companion.LOG_TAG
import com.whistle.ui.navigation.WhistleNavHost
import com.whistle.ui.theme.WhistleTheme
import com.whistle.ui.viewmodel.WhistleViewModel
import com.whistle.ui.viewmodel.WhistleViewModelFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.whistle.ui.navigation.AppBottomBar


private lateinit var mViewModel: WhistleViewModel

class MainActivity : ComponentActivity() {

    companion object{
        private const val LOG_TAG = "Whistle.MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "onCreate() Called")
        // init firebase within our app
        FirebaseApp.initializeApp(this)

        // injects app check token TODO: make this not debug mode
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        // only the cloud name is required for unsigned uploads
        MediaManager.init(this, mapOf("cloud_name" to "dxuicewpu"))

        enableEdgeToEdge()
        val factory = WhistleViewModelFactory()
        mViewModel = ViewModelProvider(this, factory)[factory.getViewModelClass()]

        Log.d(LOG_TAG, "setting content")
        setContent {
            MainActivityContent(whistleViewModel = mViewModel)
        }
    }

    override fun onStart(){
        super.onStart()
        Log.d(LOG_TAG, "onStart() Called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(LOG_TAG, "onResume() Called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(LOG_TAG, "onPause() Called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(LOG_TAG, "onStop() Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy() Called")
    }

    override fun onContentChanged() {
        super.onContentChanged()
        Log.d(LOG_TAG, "onContentChanged() Called")
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.d(LOG_TAG, "onPostCreate() Called")
    }

    override fun onPostResume() {
        super.onPostResume()
        Log.d(LOG_TAG, "onPostResume() Called")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(LOG_TAG, "onAttachedToWindow() Called")
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        Log.d(LOG_TAG, "onEnterAnimationComplete() Called")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(LOG_TAG, "onDetachedFromWindow() Called")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mViewModel.saveInstanceState(bundle = outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mViewModel.restoreInstanceState(bundle = savedInstanceState)
    }
}

@Composable
private fun MainActivityContent(
    whistleViewModel: WhistleViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    WhistleTheme(darkTheme = whistleViewModel.darkModeState.value) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AppBottomBar(
                    navController = navController,
                    whistleViewModel = whistleViewModel,
                    context = context
                )
            }
        ) { innerPadding ->
            Column (modifier = Modifier.padding(innerPadding)){
                WhistleNavHost(
                    navController = navController,
                    whistleViewModel = whistleViewModel,
                    context = context,
                    modifier = Modifier
                )
            }
        }
    }
}