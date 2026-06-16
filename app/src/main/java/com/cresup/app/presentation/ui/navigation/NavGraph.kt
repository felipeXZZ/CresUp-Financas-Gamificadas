package com.cresup.app.presentation.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cresup.app.presentation.ui.screens.analytics.AnalyticsScreen
import com.cresup.app.presentation.ui.screens.social.SocialScreen
import com.cresup.app.presentation.ui.screens.auth.LoginScreen
import com.cresup.app.presentation.ui.screens.auth.RegisterScreen
import com.cresup.app.presentation.ui.screens.dashboard.DashboardScreen
import com.cresup.app.presentation.ui.screens.desafios.DesafiosScreen
import com.cresup.app.presentation.ui.screens.gastos.GastosScreen
import com.cresup.app.presentation.ui.screens.metas.MetasScreen
import com.cresup.app.presentation.ui.screens.onboarding.OnboardingScreen
import com.cresup.app.presentation.ui.screens.perfil.PerfilScreen
import com.cresup.app.presentation.ui.screens.splash.SplashScreen
import com.cresup.app.presentation.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Gastos : Screen("gastos")
    object Metas : Screen("metas")
    object Desafios : Screen("desafios")
    object Perfil : Screen("perfil")
    object Analytics : Screen("analytics")
    object Social : Screen("social")
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.Gastos,
    Screen.Metas,
    Screen.Desafios,
    Screen.Social,
    Screen.Perfil
)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CresUpBottomNav(
                    currentRoute = currentRoute ?: "",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 4 } }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashComplete = {
                        val dest = if (authViewModel.isLoggedIn) Screen.Dashboard.route
                                    else Screen.Login.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAnalytics = {
                        navController.navigate(Screen.Analytics.route)
                    }
                )
            }

            composable(Screen.Gastos.route) { GastosScreen() }
            composable(Screen.Metas.route) { MetasScreen() }
            composable(Screen.Desafios.route) { DesafiosScreen() }

            composable(Screen.Perfil.route) {
                PerfilScreen(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Social.route) { SocialScreen() }
        }
    }
}
