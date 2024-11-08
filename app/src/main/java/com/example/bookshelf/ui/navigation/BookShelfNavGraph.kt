package com.example.bookshelf.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.bookshelf.ui.AppViewModelProvider
import com.example.bookshelf.ui.screen.DetailsScreen
import com.example.bookshelf.ui.screen.HomeScreen
import com.example.bookshelf.ui.screen.HomeViewModel
import com.example.bookshelf.ui.screen.SearchScreen
import com.example.bookshelf.ui.screen.SearchViewModel
import kotlinx.serialization.Serializable

/*
当初、BookCardがネストされすぎて、引数ではスコープを渡すことが出来なかった。そこで、CompositionLocalProviderで渡すことにした。
ただ、NavHostではAnimatedVisibilityScopeを直接渡すことが出来ないので、AnimatedContentに変えようと思った。
しかし、NavHostでルートを定義できた方が後々画面を追加する際に便利かと思い、NavHostで対応しようと考えた。
だが、LazyVerticalGridには独自のScopeがあり、CompositionLocalProviderでは指定できず、AnimatedContentでScopeを渡そうとしたが、うまくアニメーション
できなかった。
そこで、SharedTransitionScopeだけCompositionLocalProviderを使用し、AnimatedVisibilityScopeはthis@Composableで指定できることに気づき、他のComposableも
同様の対応をした。
数日にわたる長い戦いだったが、なんとか思い描いた動きになった。
 */

@Immutable
interface Route {
    @Serializable
    object Search
    @Serializable
    object Home
    @Serializable
    object Parent
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("functionName")
@Stable
@Composable
fun BookShelfNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
){
    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
            LocalOverscrollConfiguration provides null
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.Parent,
                modifier = Modifier.fillMaxSize()
            ) {
                navigation<Route.Parent>(startDestination = Route.Home){
                    composable<Route.Home> { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(Route.Parent)
                        }
                        val homeViewModel: HomeViewModel = viewModel(viewModelStoreOwner = parentEntry,factory = AppViewModelProvider.Factory)
                        val searchViewModel: SearchViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = AppViewModelProvider.Factory)

                        HomeScreen(
                            navController = navController,
                            animatedVisibilityScope = this@composable,
                            homeViewModel = homeViewModel,
                            searchViewModel = searchViewModel
                        )
                    }
                    composable(
                        "favoriteDetails/{favoriteItemId}",
                        arguments = listOf(navArgument("favoriteItemId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(Route.Parent)
                        }
                        val homeViewModel: HomeViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = AppViewModelProvider.Factory)
                        val searchViewModel: SearchViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = AppViewModelProvider.Factory)

                        DetailsScreen(
                            sharedElementKey = backStackEntry.arguments?.getString("favoriteItemId")!!,
                            judgeScreen = "favorite",
                            navController = navController,
                            animatedVisibilityScope = this@composable,
                            searchViewModel = searchViewModel,
                            homeViewModel = homeViewModel
                        )
                    }
                    composable(
                        "searchDetails/{searchItemId}",
                        arguments = listOf(navArgument("searchItemId"){type = NavType.StringType})
                    ){backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(Route.Parent)
                        }
                        val homeViewModel: HomeViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = AppViewModelProvider.Factory)
                        val searchViewModel: SearchViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = AppViewModelProvider.Factory)

                        DetailsScreen(
                            sharedElementKey = backStackEntry.arguments?.getString("searchItemId")!!,
                            judgeScreen = "search",
                            navController = navController,
                            animatedVisibilityScope = this@composable,
                            searchViewModel = searchViewModel,
                            homeViewModel = homeViewModel
                        )
                    }
                    composable<Route.Search> { backStackEntry ->
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(Route.Parent)
                        }
                        val homeViewModel: HomeViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = AppViewModelProvider.Factory)
                        val searchViewModel: SearchViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = AppViewModelProvider.Factory)

                        SearchScreen(
                            navController = navController,
                            animatedVisibilityScope = this@composable,
                            homeViewModel = homeViewModel,
                            searchViewModel = searchViewModel
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
