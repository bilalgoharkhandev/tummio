package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.reminder.SmartReminderManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reminder.NotificationHelper
import com.example.ui.MainViewModel
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.*
import com.example.ui.theme.LittleRoutineTheme

class MainActivity : ComponentActivity() {
    private val pendingNavigation = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNavigation.value = intent.getStringExtra("navigate_to")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)
        pendingNavigation.value = intent.getStringExtra("navigate_to")
        lifecycleScope.launch { SmartReminderManager.restoreAll(this@MainActivity) }

        setContent {
            val viewModel: MainViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            LittleRoutineTheme(darkTheme = isDark, dynamicColor = false) {
                LittleRoutineApp(viewModel, pendingNavigation.value) { pendingNavigation.value = null }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun LittleRoutineApp(viewModel: MainViewModel, requestedDestination: String?, onDestinationHandled: () -> Unit) {
    val context = LocalContext.current
    val allBabies by viewModel.allBabies.collectAsState()

    var currentScreen by remember { mutableStateOf(NavRoutes.HOME) }
    val screenStack = remember { mutableStateListOf(NavRoutes.HOME) }

    // Request notification permissions for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(requestedDestination, allBabies.isNotEmpty()) {
        if (requestedDestination != null && allBabies.isNotEmpty()) {
            val allowed = setOf(NavRoutes.FEEDING, NavRoutes.SLEEP, NavRoutes.DIAPER)
            if (requestedDestination in allowed) {
                currentScreen = requestedDestination
                screenStack.clear()
                screenStack.add(NavRoutes.HOME)
                screenStack.add(requestedDestination)
            }
            onDestinationHandled()
        }
    }

    // Function to navigate to destination
    fun navigateTo(route: String) {
        if (currentScreen != route) {
            screenStack.add(route)
            currentScreen = route
        }
    }

    fun popBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            currentScreen = screenStack.last()
        } else {
            currentScreen = NavRoutes.HOME
        }
    }

    // BackHandler on secondary screens
    if (currentScreen != NavRoutes.HOME) {
        BackHandler {
            popBack()
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(NavRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(NavRoutes.FEEDING, "Feeding", Icons.Filled.LocalDrink, Icons.Outlined.LocalDrink),
        BottomNavItem(NavRoutes.GROWTH, "Growth", Icons.Filled.Straighten, Icons.Outlined.Straighten),
        BottomNavItem(NavRoutes.HISTORY, "History", Icons.Filled.History, Icons.Outlined.History),
        BottomNavItem(NavRoutes.INSIGHTS, "Insights", Icons.Filled.AutoGraph, Icons.Outlined.AutoGraph)
    )

    // Show onboarding if no babies exist
    if (allBabies.isEmpty()) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = {
                currentScreen = NavRoutes.HOME
            }
        )
        return
    }

    val isBottomBarVisible = currentScreen in listOf(
        NavRoutes.HOME,
        NavRoutes.FEEDING,
        NavRoutes.GROWTH,
        NavRoutes.HISTORY,
        NavRoutes.INSIGHTS
    )

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 3.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentScreen == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentScreen != item.route) {
                                    currentScreen = item.route
                                    screenStack.clear()
                                    screenStack.add(item.route)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            alwaysShowLabel = false,
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                NavRoutes.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navigateTo(route) }
                )
                NavRoutes.FEEDING -> FeedingScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.GROWTH -> GrowthScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.INSIGHTS -> InsightsScreen(
                    viewModel = viewModel,
                    onNavigateToEducation = { navigateTo(NavRoutes.EDUCATION) },
                    onNavigateBack = { popBack() }
                )
                NavRoutes.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() },
                    onNavigateReminders = { navigateTo(NavRoutes.SMART_REMINDERS) }
                )
                NavRoutes.SMART_REMINDERS -> SmartReminderScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.FORMULA -> FormulaScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.DIAPER -> DiaperScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.SLEEP -> SleepScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.SYMPTOMS -> SymptomScreen(
                    viewModel = viewModel,
                    onNavigateBack = { popBack() }
                )
                NavRoutes.EDUCATION -> EducationCenterScreen(
                    onNavigateBack = { popBack() }
                )
            }
        }
    }
}
