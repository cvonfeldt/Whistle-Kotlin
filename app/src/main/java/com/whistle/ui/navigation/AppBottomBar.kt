package com.whistle.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.whistle.ui.navigation.specs.IScreenSpec
import com.whistle.ui.viewmodel.WhistleViewModel

@Composable
fun AppBottomBar(
    navController: NavHostController,
    whistleViewModel: WhistleViewModel,
    context: Context
) {
    val navBackStackEntryState = navController.currentBackStackEntryAsState()

    navBackStackEntryState.value?.let { navBackStackEntry ->
        IScreenSpec.BottomBar(
            whistleViewModel,
            navController = navController,
            navBackStackEntry = navBackStackEntry,
            context = context,
        )
    }
}