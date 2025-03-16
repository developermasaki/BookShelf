package com.example.bookshelf.ui


import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bookshelf.R
import com.example.bookshelf.ui.navigation.BookShelfNavHost
import com.example.bookshelf.ui.navigation.LocalSharedTransitionScope


@Suppress("functionName")
@Stable
@Composable
fun BookShelfApp() {
    val navController = rememberNavController()
    BookShelfNavHost(navController)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Suppress("functionName")
@Stable
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior?,
    titleText: String,
    canNavigateBack: Boolean = false,
    isDisplayConfirmBack: Boolean = false,
    confirmIsBack: @Composable (NavHostController) -> Boolean = {false},
    navController: NavHostController,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")

    // 編集時に戻るか確認するため
    var isConfirmBack by remember { mutableStateOf(false) }
    if(isConfirmBack) {
        isConfirmBack = confirmIsBack(navController)
    }

    with(sharedTransitionScope) {
        AnimatedVisibility(true) {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    AnimationModel(
                        visible = canNavigateBack,
                        content = {
                            IconButton(
                                onClick = {
                                    if(isDisplayConfirmBack) {
                                        Log.d("TopAppBar", "NotBack${confirmIsBack}")
                                        isConfirmBack = true
                                    } else {
                                        Log.d("TopAppBar", "Back")
                                        keyboardController?.hide()
                                        if(navController.currentBackStackEntry != null) navController.popBackStack()
                                    }
                                }
                            ){
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.search)
                                )
                            }
                        }
                    )
                },
                title = {
                    Text(
                        text = titleText,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = modifier
                    .animateContentSize()
                    .renderInSharedTransitionScopeOverlay(
                        renderInOverlay = {isTransitionActive},
                        zIndexInOverlay = 1f,
                    )
                    .animateEnterExit(
                        enter = fadeIn() + slideInVertically {
                            it
                        },
                        exit = fadeOut(animationSpec = tween(0))
                    )
            )
        }
    }
}

@Suppress("functionName")
@Stable
@Composable
fun AnimationModel(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
        label = "TopAppBar"
    ) {
        content()
    }
}