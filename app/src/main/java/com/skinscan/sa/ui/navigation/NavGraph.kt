package com.skinscan.sa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.skinscan.sa.ui.screens.OnboardingScreen
import com.skinscan.sa.ui.screens.PopiaConsentScreen
import com.skinscan.sa.ui.screens.SplashScreen
import com.skinscan.sa.ui.screens.home.HomeScreen
import com.skinscan.sa.ui.screens.recommendations.RecommendationsScreen
import com.skinscan.sa.ui.screens.results.ResultsScreen
import com.skinscan.sa.ui.screens.scan.ScanScreen

/**
 * Main navigation graph for Glow Guide
 *
 * Routes:
 * - splash → onboarding → popia_consent → profile_setup → home
 * - home → scan → results → recommendations
 * - home → history → scan_detail
 * - home → profile → settings
 *
 * Sprint 2: Added results/{scanId} route
 * Sprint 3: Added recommendations/{scanId} route
 */

// Navigation route constants
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val POPIA_CONSENT = "popia_consent"
    const val PROFILE_SETUP = "profile_setup"
    const val HOME = "home"
    const val SCAN = "scan"
    const val RESULTS = "results/{scanId}"
    const val RECOMMENDATIONS = "recommendations/{scanId}"
    const val HISTORY = "history"
    const val SCAN_DETAIL = "scan_detail/{scanId}"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    fun results(scanId: String) = "results/$scanId"
    fun recommendations(scanId: String) = "recommendations/$scanId"
    fun scanDetail(scanId: String) = "scan_detail/$scanId"
}

@Composable
fun SkinScanNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onNavigateToPOPIAConsent = {
                    navController.navigate(Routes.POPIA_CONSENT)
                }
            )
        }

        composable(Routes.POPIA_CONSENT) {
            PopiaConsentScreen(
                onNavigateToProfileSetup = {
                    // For Sprint 1: Skip profile setup, go straight to home
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onExitApp = {
                    // TODO: Exit app
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToScan = {
                    navController.navigate(Routes.SCAN)
                }
            )
        }

        composable(Routes.SCAN) {
            ScanScreen(
                onNavigateToHome = {
                    navController.popBackStack()
                },
                onNavigateToResults = { scanId ->
                    navController.navigate(Routes.results(scanId)) {
                        popUpTo(Routes.SCAN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.RESULTS,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) {
            ResultsScreen(
                onNavigateBack = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToRecommendations = { scanId ->
                    navController.navigate(Routes.recommendations(scanId))
                }
            )
        }

        composable(
            route = Routes.RECOMMENDATIONS,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) {
            RecommendationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // TODO Story 1.5: Implement profile setup screen
        // TODO Story 4.x: Implement history screens
    }
}
