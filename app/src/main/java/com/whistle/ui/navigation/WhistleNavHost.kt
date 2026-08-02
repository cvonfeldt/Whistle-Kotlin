package com.whistle.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.whistle.ui.navigation.specs.IScreenSpec
import com.whistle.ui.viewmodel.WhistleViewModel

@Composable
fun WhistleNavHost(
    navController: NavHostController,
    whistleViewModel: WhistleViewModel,
    modifier: Modifier,
    context: Context
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = IScreenSpec.root
    ) {
        navigation(
            route = IScreenSpec.root,
            startDestination = IScreenSpec.startDestination
        ) {
            IScreenSpec.allScreens.forEach { (_, screen) ->
                if(screen != null) {
                    composable(
                        route = screen.route,
                    ) { navBackStackEntry ->
                        screen.Content(
                            modifier = modifier,
                            navController = navController,
                            whistleViewModel = whistleViewModel,
                            context = context,
                        )
                    }
                }
            }
        }
    }
}