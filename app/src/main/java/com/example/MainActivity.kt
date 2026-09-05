package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.models.UserRole
import com.example.ui.components.ChambaBottomNavigationBar
import com.example.ui.components.ChambaTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChambaAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ChambaAppRoot(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val isAuthScreen = currentScreen is Screen.Login ||
        currentScreen is Screen.Register ||
        currentScreen is Screen.ForgotPassword

    val showTopBar = !isAuthScreen &&
        currentScreen !is Screen.ChambaDetail &&
        currentScreen !is Screen.ChatConversation &&
        currentScreen !is Screen.WorkerProfile &&
        currentScreen !is Screen.PublishChamba &&
        currentScreen !is Screen.PriceEstimator &&
        currentScreen !is Screen.CustomerSupport

    val showBottomNav = !isAuthScreen

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showTopBar) {
                ChambaTopAppBar(
                    currentUser = currentUser,
                    unreadCount = unreadCount,
                    onNavigate = { viewModel.navigateTo(it) },
                    onSwitchRole = { viewModel.switchRoleForTesting(it) },
                    onSearchClick = { viewModel.navigateTo(Screen.Search) }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                ChambaBottomNavigationBar(
                    currentUser = currentUser,
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                when (screen) {
                    is Screen.Home -> {
                        if (currentUser?.esTrabajador == true) {
                            WorkerHomeScreen(viewModel = viewModel, currentUser = currentUser)
                        } else {
                            ClientHomeScreen(viewModel = viewModel, currentUser = currentUser)
                        }
                    }
                    is Screen.Search -> SearchScreen(viewModel = viewModel)
                    is Screen.PublishChamba -> PublishChambaScreen(viewModel = viewModel)
                    is Screen.ChambaDetail -> ChambaDetailScreen(chambaId = screen.chambaId, viewModel = viewModel)
                    is Screen.WorkerProfile -> WorkerProfileScreen(workerId = screen.workerId, viewModel = viewModel)
                    is Screen.MyChambas -> MyChambasScreen(viewModel = viewModel)
                    is Screen.Messages -> ChatListScreen(viewModel = viewModel)
                    is Screen.ChatConversation -> ChatConversationScreen(
                        chambaId = screen.chambaId,
                        otherUserId = screen.otherUserId,
                        otherUserName = screen.otherUserName,
                        viewModel = viewModel
                    )
                    is Screen.Notifications -> NotificationsScreen(viewModel = viewModel)
                    is Screen.Incomes -> IncomesScreen(viewModel = viewModel)
                    is Screen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
                    is Screen.PriceEstimator -> PriceEstimatorScreen(viewModel = viewModel, onNavigateBack = { viewModel.navigateTo(Screen.Home) })
                    is Screen.CustomerSupport -> CustomerSupportScreen(viewModel = viewModel, onNavigateBack = { viewModel.navigateTo(Screen.Profile) })
                    is Screen.Policies -> PoliciesScreen(viewModel = viewModel)
                    is Screen.Profile -> ProfileScreen(viewModel = viewModel)
                    is Screen.Login -> LoginScreen(viewModel = viewModel)
                    is Screen.Register -> RegisterScreen(viewModel = viewModel)
                    is Screen.ForgotPassword -> ForgotPasswordScreen(viewModel = viewModel)
                }
            }
        }
    }
}

