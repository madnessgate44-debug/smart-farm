package com.ahmedpasha.smartfarm.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ahmedpasha.smartfarm.ui.screens.*
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel

enum class ScreenTab(val route: String, val title: String, val icon: ImageVector, val testTag: String) {
    DASHBOARD("dashboard", "الرئيسية", Icons.Default.Dashboard, "nav_dashboard"),
    CHAT("chat", "بليغ", Icons.Default.Chat, "nav_chat"),
    TASKS("tasks", "المهام", Icons.Default.Task, "nav_tasks"),
    TABLES("tables", "الجداول", Icons.Default.TableChart, "nav_tables"),
    FINANCE("finance", "المالية", Icons.Default.AccountBalance, "nav_finance"),
    WORKERS("workers", "العمال", Icons.Default.People, "nav_workers"),
    INVENTORY("inventory", "المخازن", Icons.Default.Inventory, "nav_inventory"),
    CHARTS("charts", "الرسوم", Icons.Default.BarChart, "nav_charts"),
    MEETINGS("meetings", "الاجتماعات", Icons.Default.Groups, "nav_meetings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmNavigation(viewModel: FarmViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(ScreenTab.DASHBOARD, ScreenTab.CHAT, ScreenTab.TASKS, ScreenTab.TABLES, ScreenTab.CHARTS).forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenTab.DASHBOARD.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenTab.DASHBOARD.route) { HomeDashboard(viewModel) }
            composable(ScreenTab.CHAT.route) { ChatScreen(viewModel) }
            composable(ScreenTab.TASKS.route) { TasksScreen(viewModel) }
            composable(ScreenTab.TABLES.route) { TablesScreen(viewModel) }
            composable(ScreenTab.FINANCE.route) { FinancialDashboard(viewModel) }
            composable(ScreenTab.WORKERS.route) { WorkerManagement(viewModel) }
            composable(ScreenTab.INVENTORY.route) { InventoryScreen(viewModel) }
            composable(ScreenTab.CHARTS.route) { ChartsScreen(viewModel) }
            composable(ScreenTab.MEETINGS.route) { MeetingsScreen(viewModel) }
        }
    }
}