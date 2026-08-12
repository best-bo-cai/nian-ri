package com.nianri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nianri.ui.calendar.CalendarScreen
import com.nianri.ui.chat.ChatScreen
import com.nianri.ui.config.AiConfigScreen
import com.nianri.ui.config.SmtpConfigScreen
import com.nianri.ui.days.DaysScreen
import com.nianri.ui.edit.EditEventScreen
import com.nianri.ui.events.EventsScreen
import com.nianri.ui.navigation.Screen
import com.nianri.ui.navigation.bottomNavItems
import com.nianri.ui.profile.ProfileScreen
import com.nianri.ui.search.SearchScreen
import com.nianri.ui.theme.NianRiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            NianRiTheme(darkTheme = isDarkMode) {
                MainApp(onDarkModeChange = { isDarkMode = it })
            }
        }
    }
}

@Composable
fun MainApp(onDarkModeChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Days.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Days.route) {
                DaysScreen(
                    onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToEdit = { eventId ->
                        navController.navigate(Screen.EditEvent.createRoute(eventId))
                    }
                )
            }
            composable(Screen.Events.route) {
                EventsScreen(
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToEdit = { eventId ->
                        navController.navigate(Screen.EditEvent.createRoute(eventId))
                    }
                )
            }
            composable(Screen.Chat.route) {
                ChatScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToAiConfig = { navController.navigate(Screen.AiConfig.route) },
                    onNavigateToSmtpConfig = { navController.navigate(Screen.SmtpConfig.route) },
                    onDarkModeChange = onDarkModeChange
                )
            }
            composable(Screen.AiConfig.route) {
                AiConfigScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.SmtpConfig.route) {
                SmtpConfigScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { eventId ->
                        navController.navigate(Screen.EditEvent.createRoute(eventId))
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { eventId ->
                        navController.navigate(Screen.EditEvent.createRoute(eventId))
                    }
                )
            }
            composable(
                route = Screen.EditEvent.route,
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1L
                EditEventScreen(
                    eventId = if (eventId == -1L) null else eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
