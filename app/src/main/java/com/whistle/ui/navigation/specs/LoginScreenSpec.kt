package com.whistle.ui.navigation.specs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.whistle.ui.login.LoginScreen
import com.whistle.ui.viewmodel.WhistleViewModel

object LoginScreenSpec : IScreenSpec {

    override val route: String
        get() = "login"

    @Composable
    override fun Content(
        whistleViewModel: WhistleViewModel,
        navController: NavController,  // Pass the NavController
        modifier: Modifier,
        context: Context
    ) {
        LoginScreen(navController = navController)
    }

    @Composable
    override fun BottomAppBarActions(
        whistleViewModel: WhistleViewModel,
        navController: NavHostController,
        context: Context
    ) {

    }
}
