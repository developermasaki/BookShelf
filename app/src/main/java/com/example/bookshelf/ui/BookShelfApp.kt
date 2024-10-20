package com.example.bookshelf.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bookshelf.R
import com.example.bookshelf.ui.component.enterAlwaysScrollBehavior
import com.example.bookshelf.ui.screen.BookShelfContentUiState
import com.example.bookshelf.ui.screen.BookShelfViewModel
import com.example.bookshelf.ui.screen.HomeContent

@OptIn(ExperimentalMaterial3Api::class)
@Stable
@Composable
fun BookShelfApp(modifier: Modifier = Modifier) {
    val bookShelfViewModel: BookShelfViewModel = viewModel(factory = BookShelfViewModel.Factory)

    // 検索後にTopAppBarが畳まれた状況になるのを回避するため
    val scrollBehavior = if(bookShelfViewModel.bookShelfContentUiState.firstSearch) TopAppBarDefaults.enterAlwaysScrollBehavior() else enterAlwaysScrollBehavior()

    val navController = rememberNavController()
    val favoriteBookList by bookShelfViewModel.favoriteBookList.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            scrollBehavior = scrollBehavior,
            bookShelfContentUiState = bookShelfViewModel.bookShelfContentUiState,
            navController = navController,
            updateSearchShowOn = {bookShelfViewModel.isShowSearchScreen()},
            research = {bookShelfViewModel.getBookShelfItems()},
            showDetailsScreen = {bookShelfViewModel.isShowDetailsScreen()}
        )
        HomeContent(
            screenUiState = bookShelfViewModel.screenUiState,
            scrollBehavior = scrollBehavior,
            bookShelfContentUiState = bookShelfViewModel.bookShelfContentUiState,
            navController = navController,
            pressFabButton = {bookShelfViewModel.isShowSearchScreen()},
            onValueChanged = {editTextField, string -> bookShelfViewModel.updateEditTextField(editTextField, string)},
            research = {isResearch -> bookShelfViewModel.getBookShelfItems(isResearch)},
            authorsListUp = {bookShelfViewModel.authorsListUp(it)},
            showPrice = {price, country -> bookShelfViewModel.showPrice(price, country)},
            pickUpItem = {key -> bookShelfViewModel.pickUpItem(key)},
            isShowDetailsScreen = {bookShelfViewModel.isShowDetailsScreen()},
            bestImage = {items -> bookShelfViewModel.bestImage(items)},
            favoriteBookList = favoriteBookList,
            isShowFavoriteScreen = bookShelfViewModel::isShowFavoriteScreen,
            toggleFavoriteBook = bookShelfViewModel::toggleFavoriteBook
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Stable
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    bookShelfContentUiState: BookShelfContentUiState,
    navController: NavHostController,
    updateSearchShowOn: () -> Unit,
    showDetailsScreen: () -> Unit,
    research: (Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    CenterAlignedTopAppBar(
        scrollBehavior = if(!bookShelfContentUiState.showOnDetailsScreen && !bookShelfContentUiState.showOnSearchScreen)scrollBehavior else null,
        navigationIcon = {
            AnimationModel(
                visible = navController.previousBackStackEntry != null,
                content = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            navController.navigateUp()
                            if (bookShelfContentUiState.showOnSearchScreen) {
                                updateSearchShowOn()
                            } else {
                                showDetailsScreen()
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
                text = if(bookShelfContentUiState.showOnSearchScreen)stringResource(R.string.search) else if(bookShelfContentUiState.showOnDetailsScreen) stringResource(R.string.details) else stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.primary,
            )
        },
        actions = {
            AnimationModel(
                visible = !bookShelfContentUiState.showOnSearchScreen
            ) {
                IconButton(
                    onClick = {research(false)}
                ){
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.reimport)
                    )
                }
            }
        },
        modifier = modifier
            .animateContentSize()
    )
}

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