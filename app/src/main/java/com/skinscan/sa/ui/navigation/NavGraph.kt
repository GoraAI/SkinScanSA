package com.skinscan.sa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.skinscan.sa.ui.screens.OnboardingScreen
import com.skinscan.sa.ui.screens.PopiaConsentScreen
import com.skinscan.sa.ui.screens.SplashScreen
import com.skinscan.sa.ui.screens.home.HomeScreen
import com.skinscan.sa.ui.screens.scan.ScanScreen

/**
 * Main navigation graph for Glow Guide
 *
 * Routes:
 * - splash → onboarding → popia_consent → profile_setup → home
 * - home → scan → results → recommendations
 * - home → history → scan_detail
 * - home → profile → settings
 */

// Navigation route constants
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val POPIA_CONSENT = "popia_consent"
    const val PROFILE_SETUP = "profile_setup"
    const val HOME = "home"
    const val SCAN = "scan"
    const val RESULTS = "results"
    const val RECOMMENDATIONS = "recommendations"
    const val HISTORY = "history"
    const val SCAN_DETAIL = "scan_detail/{scanId}"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
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
                }
            )
        }

        // TODO Story 1.5: Implement profile setup screen
        // TODO Story 3.x: Implement scan screens
        // TODO Story 4.x: Implement results screens
        // TODO Story 5.x: Implement history screens
    }
}
