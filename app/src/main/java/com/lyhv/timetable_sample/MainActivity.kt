package com.lyhv.timetable_sample

import MainDestinations
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.lyhv.timetable_sample.ui.theme.TimeTableTheme
import rememberAppState

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeTableTheme {
//                Scaffold {
//                    Surface {
//                        MainTable()
//                    }
//                }
                TimeTableApp()
            }
        }
    }
}

@Composable
fun TimeTableApp() {
    val appState = rememberAppState()
    Scaffold(
        scaffoldState = appState.scaffoldState,
        bottomBar = {
            if (appState.shouldShowBottomBar) {
                JetsnackBottomBar(
                    tabs = appState.bottomBarTabs,
                    currentRoute = appState.currentRoute!!,
                    navigateToRoute = appState::navigateToBottomBarRoute
                )
            }
        },
    ) {
        NavHost(
            navController = appState.navController,
            startDestination = MainDestinations.App,
            modifier = Modifier.padding(it)
        ) {
            navGraph(onEventClicked = appState::navigateToEvent)
        }
    }
}

private fun NavGraphBuilder.navGraph(onEventClicked: (String, NavBackStackEntry) -> Unit) {
    navigation(
        route = MainDestinations.App,
        startDestination = AppSections.HOME.route
    ) {
        composable(AppSections.HOME.route) { from ->
            Home(Modifier)
        }
        composable(AppSections.TIMETABLE.route) { from ->
            TimeTable(Modifier) {
                onEventClicked(it, from)
            }
        }
        composable(AppSections.PROFILE.route) { from ->
            Profile(Modifier)
        }
    }
    composable(
        route = "${MainDestinations.EVENT_DETAIL_ROUTE}/{${MainDestinations.EVENT_NAME}}",
        arguments = listOf(navArgument(MainDestinations.EVENT_NAME) { type = NavType.StringType })
    ) { backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val eventName = arguments.getString(MainDestinations.EVENT_NAME) ?: ""
        EventDetail(eventName)
    }
}
