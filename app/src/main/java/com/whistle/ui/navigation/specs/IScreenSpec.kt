package com.whistle.ui.navigation.specs

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.whistle.ui.viewmodel.WhistleViewModel

private val LOG_TAG = "IscreenSpec"

sealed interface IScreenSpec {

    companion object {
        val allScreens = IScreenSpec::class.sealedSubclasses.associate {
            Log.d(
                LOG_TAG,
                "allScreens: mapping route \"${it.objectInstance?.route ?: ""}\" to object \"${it.objectInstance}\""
            )
            it.objectInstance?.route to it.objectInstance
        }
        val root = "whistle"
        val startDestination = LoginScreenSpec.route

        @Composable
        fun BottomBar(
            whistleViewModel: WhistleViewModel,
            navController: NavHostController,
            navBackStackEntry: NavBackStackEntry,
            context: Context
        ) {
            val route = navBackStackEntry?.destination?.route ?: ""

            allScreens[route]?.BottomAppBarContent(
                whistleViewModel = whistleViewModel,
                navController = navController,
                navBackStackEntry = navBackStackEntry,
                context = context
            )
        }

    }

    val route: String

    @Composable
    fun Content(
        whistleViewModel: WhistleViewModel,
        navController: NavController,
        modifier: Modifier,
        context: Context
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BottomAppBarContent(
        whistleViewModel: WhistleViewModel,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        context: Context
    ) {
        BottomAppBar(
            containerColor = Color(0xFFDCDCDC),
            actions = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomAppBarActions(
                        whistleViewModel = whistleViewModel,
                        navController = navController,
                        context = context
                    )
                }
            }
        )
    }

    @Composable
    abstract public fun BottomAppBarActions(
        whistleViewModel: WhistleViewModel,
        navController: NavHostController,
        context: Context
    )
}