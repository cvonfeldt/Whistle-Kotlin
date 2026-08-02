package com.whistle.ui.navigation.specs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.whistle.ui.feed.FeedScreen
import com.whistle.ui.signup.SignupScreen
import com.whistle.ui.viewmodel.WhistleViewModel

object SignupScreenSpec : IScreenSpec {

    override val route: String
        get() = "signup"

    @Composable
    override fun Content(
        whistleViewModel: WhistleViewModel,
        navController: NavController,
        modifier: Modifier,
        context: Context
    ) {
        SignupScreen(navController = navController)
    }

    @Composable
    override fun BottomAppBarActions(
        whistleViewModel: WhistleViewModel,
        navController: NavHostController,
        context: Context
    ) {

    }
}