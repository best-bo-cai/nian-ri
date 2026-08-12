package com.nianri.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Days : Screen("days")
    data object Events : Screen("events")
    data object Chat : Screen("chat")
    data object Profile : Screen("profile")
    data object Calendar : Screen("calendar")
    data object Search : Screen("search")
    data object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Long = -1L) = "edit_event/$eventId"
    }
    data object AiConfig : Screen("ai_config")
    data object SmtpConfig : Screen("smtp_config")
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "日子",
        route = Screen.Days.route,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    BottomNavItem(
        label = "事件",
        route = Screen.Events.route,
        selectedIcon = Icons.Filled.EventNote,
        unselectedIcon = Icons.Outlined.EventNote
    ),
    BottomNavItem(
        label = "对话",
        route = Screen.Chat.route,
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    ),
    BottomNavItem(
        label = "我的",
        route = Screen.Profile.route,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)
