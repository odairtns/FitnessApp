package com.aksoit.myfitnessapp.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aksoit.myfitnessapp.FitnessTrackerApplication
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.platform.timer.MonotonicTimerEngineImpl
import com.aksoit.myfitnessapp.ui.screens.history.HistoryScreen
import com.aksoit.myfitnessapp.ui.screens.history.HistoryViewModel
import com.aksoit.myfitnessapp.ui.screens.history.PersonalRecordsScreen
import com.aksoit.myfitnessapp.ui.screens.history.SessionDetailScreen
import com.aksoit.myfitnessapp.ui.screens.home.HomeScreen
import com.aksoit.myfitnessapp.ui.screens.home.HomeViewModel
import com.aksoit.myfitnessapp.ui.screens.player.WorkoutPlayerScreen
import com.aksoit.myfitnessapp.ui.screens.player.WorkoutPlayerViewModel
import com.aksoit.myfitnessapp.ui.screens.tools.ToolsScreen
import com.aksoit.myfitnessapp.ui.screens.tools.ToolsViewModel
import com.aksoit.myfitnessapp.ui.screens.workouts.ExerciseLibraryScreen
import com.aksoit.myfitnessapp.ui.screens.workouts.ExerciseLibraryViewModel
import com.aksoit.myfitnessapp.ui.screens.workouts.TemplateEditorScreen
import com.aksoit.myfitnessapp.ui.screens.workouts.TemplateEditorViewModel
import com.aksoit.myfitnessapp.ui.screens.workouts.WorkoutsListScreen
import com.aksoit.myfitnessapp.ui.screens.workouts.WorkoutsViewModel
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.DeepVoid
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val app = context.applicationContext as FitnessTrackerApplication
    val container = app.container

    var pendingSelectedBlockIndex by remember { mutableStateOf<Int?>(null) }
    var templateEditorVmRef by remember { mutableStateOf<TemplateEditorViewModel?>(null) }

    val isTopLevelDestination = when (currentDestination?.route) {
        ScreenRoute.Home::class.qualifiedName,
        ScreenRoute.Workouts::class.qualifiedName,
        ScreenRoute.History::class.qualifiedName,
        ScreenRoute.Tools::class.qualifiedName -> true
        else -> false
    }

    Scaffold(
        containerColor = DeepVoid,
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = DarkCharcoal
                ) {
                    val currentRoute = currentDestination?.route
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Home::class.qualifiedName,
                        onClick = {
                            navController.navigate(ScreenRoute.Home) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SprintGreen,
                            selectedTextColor = SprintGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Workouts::class.qualifiedName,
                        onClick = {
                            navController.navigate(ScreenRoute.Workouts) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Treinos") },
                        label = { Text("Treinos") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SprintGreen,
                            selectedTextColor = SprintGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.History::class.qualifiedName,
                        onClick = {
                            navController.navigate(ScreenRoute.History) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "Histórico") },
                        label = { Text("Histórico") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SprintGreen,
                            selectedTextColor = SprintGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Tools::class.qualifiedName,
                        onClick = {
                            navController.navigate(ScreenRoute.Tools) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Timer, contentDescription = "Ferramentas") },
                        label = { Text("Ferramentas") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SprintGreen,
                            selectedTextColor = SprintGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Home,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable<ScreenRoute.Home> {
                val vm = remember {
                    HomeViewModel(
                        container.observeActiveTemplatesUseCase,
                        container.observeWeeklyVolumeUseCase,
                        container.getRecoverableSessionUseCase,
                        container.discardWorkoutUseCase
                    )
                }
                HomeScreen(
                    viewModel = vm,
                    onStartWorkout = { templateId ->
                        navController.navigate(ScreenRoute.WorkoutPlayer(templateId))
                    },
                    onNavigateToWorkouts = { navController.navigate(ScreenRoute.Workouts) },
                    onNavigateToPrs = { navController.navigate(ScreenRoute.PersonalRecords) }
                )
            }

            composable<ScreenRoute.Workouts> {
                val vm = remember {
                    WorkoutsViewModel(
                        container.observeActiveTemplatesUseCase,
                        container.duplicateWorkoutTemplateUseCase,
                        container.archiveWorkoutTemplateUseCase,
                        container.deleteWorkoutTemplateUseCase,
                        container.exportWorkoutTemplateXmlUseCase
                    )
                }
                WorkoutsListScreen(
                    viewModel = vm,
                    onStartWorkout = { templateId ->
                        navController.navigate(ScreenRoute.WorkoutPlayer(templateId))
                    },
                    onEditTemplate = { templateId ->
                        navController.navigate(ScreenRoute.TemplateEditor(templateId))
                    },
                    onCreateTemplate = {
                        navController.navigate(ScreenRoute.TemplateEditor(null))
                    },
                    onExportXml = {
                        navController.navigate(ScreenRoute.Tools)
                    }
                )
            }

            composable<ScreenRoute.TemplateEditor> { backStack ->
                val route = backStack.toRoute<ScreenRoute.TemplateEditor>()
                val vm = remember(route.templateId) {
                    TemplateEditorViewModel(
                        container.getWorkoutTemplateUseCase,
                        container.createWorkoutTemplateUseCase,
                        container.updateWorkoutTemplateUseCase
                    )
                }
                templateEditorVmRef = vm
                TemplateEditorScreen(
                    templateId = route.templateId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onSelectExercise = { blockIdx ->
                        pendingSelectedBlockIndex = blockIdx
                        navController.navigate(ScreenRoute.ExerciseLibrary(selectMode = true))
                    }
                )
            }

            composable<ScreenRoute.ExerciseLibrary> { backStack ->
                val route = backStack.toRoute<ScreenRoute.ExerciseLibrary>()
                val vm = remember {
                    ExerciseLibraryViewModel(
                        container.observeExercisesUseCase,
                        container.saveExerciseUseCase
                    )
                }
                ExerciseLibraryScreen(
                    viewModel = vm,
                    selectMode = route.selectMode,
                    onNavigateBack = { navController.popBackStack() },
                    onExerciseSelected = { ex ->
                        val bIdx = pendingSelectedBlockIndex
                        if (bIdx != null) {
                            templateEditorVmRef?.addExerciseToBlock(bIdx, ex)
                            pendingSelectedBlockIndex = null
                        }
                    }
                )
            }

            composable<ScreenRoute.WorkoutPlayer> { backStack ->
                val route = backStack.toRoute<ScreenRoute.WorkoutPlayer>()
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                val playerTimer = remember {
                    MonotonicTimerEngineImpl(scope, container.monotonicClock)
                }
                val vm = remember(route.templateId) {
                    WorkoutPlayerViewModel(
                        templateId = route.templateId,
                        getWorkoutTemplateUseCase = container.getWorkoutTemplateUseCase,
                        startWorkoutUseCase = container.startWorkoutUseCase,
                        confirmSetUseCase = container.confirmSetUseCase,
                        saveAmrapResultUseCase = container.saveAmrapResultUseCase,
                        saveForTimeResultUseCase = container.saveForTimeResultUseCase,
                        finishWorkoutUseCase = container.finishWorkoutUseCase,
                        cancelWorkoutUseCase = container.cancelWorkoutUseCase,
                        discardWorkoutUseCase = container.discardWorkoutUseCase,
                        getRecoverableSessionUseCase = container.getRecoverableSessionUseCase,
                        saveRecoveryCheckpointUseCase = container.saveRecoveryCheckpointUseCase,
                        getLastLoadByExerciseAndSetUseCase = container.getLastLoadByExerciseAndSetUseCase,
                        timerEngine = playerTimer,
                        wallClock = container.wallClock,
                        monotonicClock = container.monotonicClock
                    )
                }
                WorkoutPlayerScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { sId ->
                        navController.navigate(ScreenRoute.SessionDetail(sId)) {
                            popUpTo(ScreenRoute.Home)
                        }
                    }
                )
            }

            composable<ScreenRoute.History> {
                val vm = remember {
                    HistoryViewModel(
                        container.observeSessionsUseCase,
                        container.observePersonalRecordsUseCase,
                        container.getSessionDetailUseCase
                    )
                }
                HistoryScreen(
                    viewModel = vm,
                    onNavigateToDetail = { sId ->
                        navController.navigate(ScreenRoute.SessionDetail(sId))
                    },
                    onNavigateToPrs = { navController.navigate(ScreenRoute.PersonalRecords) }
                )
            }

            composable<ScreenRoute.SessionDetail> { backStack ->
                val route = backStack.toRoute<ScreenRoute.SessionDetail>()
                val vm = remember {
                    HistoryViewModel(
                        container.observeSessionsUseCase,
                        container.observePersonalRecordsUseCase,
                        container.getSessionDetailUseCase
                    )
                }
                SessionDetailScreen(
                    sessionId = route.sessionId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<ScreenRoute.PersonalRecords> {
                val vm = remember {
                    HistoryViewModel(
                        container.observeSessionsUseCase,
                        container.observePersonalRecordsUseCase,
                        container.getSessionDetailUseCase
                    )
                }
                PersonalRecordsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<ScreenRoute.Tools> {
                val vm = remember {
                    ToolsViewModel(
                        container.independentTimerUseCase,
                        container.previewXmlImportUseCase,
                        container.importWorkoutTemplateUseCase
                    )
                }
                ToolsScreen(viewModel = vm)
            }
        }
    }
}
