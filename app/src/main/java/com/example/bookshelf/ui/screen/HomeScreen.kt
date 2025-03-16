package com.example.bookshelf.ui.screen

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.animatedSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.bookshelf.R
import com.example.bookshelf.model.ImageLinks
import com.example.bookshelf.model.Items
import com.example.bookshelf.model.VolumeInfo
import com.example.bookshelf.ui.TopAppBar
import com.example.bookshelf.ui.component.enterAlwaysScrollBehavior
import com.example.bookshelf.ui.navigation.LocalSharedTransitionScope
import com.example.bookshelf.ui.navigation.Route
import com.example.bookshelf.ui.theme.BookShelfTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("functionName")
@Stable
@Composable
fun HomeScreen(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    modifier: Modifier = Modifier
){
    val favoriteBookList by homeViewModel.favoriteBookList.collectAsState()
    val bookShelfItems by searchViewModel.showedBookShelfItems.collectAsState()
    val searchUiState = searchViewModel.searchUiState
    val homeUiState = homeViewModel.homeUiState

    // renderInSharedTransitionScopeOverlayがHome画面に戻るときにだけ動作するようにしている
    var isHome by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if(!isHome){
            delay(1000)
            isHome = true
        }
    }

    val scrollBehavior = enterAlwaysScrollBehavior(
        state = remember { TopAppBarState(-Float.MAX_VALUE, 0f, 0f)}
    )

    // HorizontalPagerとTabが連動して動くため
    val titles = listOf("Favorite", "Search")
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState{titles.size}
    if(searchUiState.firstSearch) {
        selectedTabIndex = 1
    }
    LaunchedEffect(selectedTabIndex){
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }.collect {
            if(!it){
                selectedTabIndex = pagerState.currentPage
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)) {
        TopAppBar(
            modifier = Modifier
                .wrapContentSize(),
            scrollBehavior = scrollBehavior,
            titleText = stringResource(R.string.app_name),
            navController = navController

        )
        OriginalTabRow(
            pagerState = pagerState,
            titles = titles,
            selectedTabIndex = selectedTabIndex,
            changeSelectedTabIndex = {index:Int -> selectedTabIndex = index},
            homeViewModel = homeViewModel,
            homeUiState = homeUiState,
            isHome = isHome,
        )
        Box(modifier = Modifier.fillMaxSize()) {
            HomeContents(
                pagerState = pagerState,
                bookShelfItems = bookShelfItems,
                searchUiState = searchUiState,
                homeUiState = homeUiState,
                navController = navController,
                animatedVisibilityScope = animatedVisibilityScope,
                favoriteBookList = favoriteBookList,
                research = searchViewModel::getBookShelfItems,
                toggleFavoriteBook = homeViewModel::toggleFavoriteBook,
                isFirstSearch = searchViewModel::isFirstSearch
            )
            OriginalFAB(
                navController = navController,
                animatedVisibilityScope = animatedVisibilityScope,
                isHome = isHome,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .wrapContentSize()
                    .padding(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            )
        }
    }
}

@Suppress("functionName")
@Composable
private fun HomeContents(
    pagerState: PagerState,
    bookShelfItems: List<Pair<String, Items?>>,
    searchUiState: SearchUiState,
    homeUiState: HomeUiState,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    favoriteBookList: List<Pair<String, Items?>>,
    research: suspend (Boolean) -> Unit,
    toggleFavoriteBook: suspend (Items) -> Unit,
    isFirstSearch: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val stateForFavoriteScreen = rememberLazyGridState()
    val stateForSearchScreen = rememberLazyGridState()
    HorizontalPager(
        state = pagerState
    ){ index ->
        if (index == 0) {
            FavoriteScreen(
                homeUiState = homeUiState,
                favoriteBookList = favoriteBookList,
                state = stateForFavoriteScreen,
                navController = navController,
                toggleFavoriteBook = toggleFavoriteBook,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        } else {
            SearchResultScreen(
                searchUiState = searchUiState,
                homeUiState = homeUiState,
                state = stateForSearchScreen,
                bookShelfItems= bookShelfItems,
                favoriteBookList = favoriteBookList,
                navController = navController,
                research = research,
                toggleFavoriteBook = toggleFavoriteBook,
                isFirstSearch = isFirstSearch,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = modifier
            )
        }
    }
}

@Suppress("functionName")
@Composable
private fun FavoriteScreen (
    homeUiState: HomeUiState,
    favoriteBookList: List<Pair<String, Items?>>,
    state: LazyGridState,
    navController: NavHostController,
    toggleFavoriteBook: suspend (Items) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier
){
    AnimatedContent(
        targetState = homeUiState.screenUiState,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(300)
            ) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "Animated Favorite",
        modifier = Modifier.fillMaxSize()
    ) { targetState ->
        when(targetState) {
            is FavoriteScreenUiState.Loading -> LoadingScreen()
            is FavoriteScreenUiState.Success -> OriginalLazyVerticalGrid(
                modifier = modifier,
                screenName = "favorite",
                noItemsText = stringResource(R.string.noFavorite),
                showedItems = favoriteBookList,
                favoriteBookList = favoriteBookList,
                navController = navController,
                state = state,
                toggleFavoriteBook = toggleFavoriteBook,
                animatedVisibilityScope = animatedVisibilityScope
            )
            is FavoriteScreenUiState.Error -> ErrorScreen(targetState.errorType, targetState.errorDetails)
        }
    }
}

@Suppress("functionName")
@Composable
private fun SearchResultScreen(
    searchUiState: SearchUiState,
    homeUiState: HomeUiState,
    state: LazyGridState,
    bookShelfItems: List<Pair<String, Items?>>,
    favoriteBookList: List<Pair<String, Items?>>,
    navController: NavHostController,
    research: suspend (Boolean) -> Unit,
    toggleFavoriteBook: suspend (Items) -> Unit,
    isFirstSearch: (Boolean) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier
){
    // ある程度スクロールしたら、自動でAPIの読み込みをするため
    var every38Index by rememberSaveable { mutableIntStateOf(38) }
    LaunchedEffect(state, searchUiState) {
        snapshotFlow { state.firstVisibleItemIndex }
            .collect {
                if (!homeUiState.showOnFavoriteScreen) {
                    if (it >= every38Index - 30) {
                        research(false)
                        every38Index += 38
                    } else if (searchUiState.firstSearch) {
                        isFirstSearch(false)
                        state.requestScrollToItem(0)
                        every38Index = 38
                    }
                }
            }
    }

    AnimatedContent(
        targetState = searchUiState.screenUiState,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(300)
            ) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "Animated Search",
        modifier = Modifier.fillMaxSize(),
    ) { targetState ->
        when(targetState) {
            is SearchScreenUiState.Loading ->
                if(!targetState.noSearched){
                    LoadingScreen()
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.noSearched),
                        )
                    }
                }
            is SearchScreenUiState.Success ->OriginalLazyVerticalGrid(
                modifier = modifier,
                screenName = "search",
                noItemsText = stringResource(R.string.noBook),
                showedItems = bookShelfItems,
                favoriteBookList = favoriteBookList,
                navController = navController,
                state = state,
                toggleFavoriteBook = toggleFavoriteBook,
                animatedVisibilityScope = animatedVisibilityScope
            )
            is SearchScreenUiState.Error -> ErrorScreen(targetState.errorType, targetState.errorDetails)
        }
    }
}

@Suppress("functionName")
@Composable
private fun OriginalLazyVerticalGrid(
    modifier: Modifier = Modifier,
    screenName: String,
    noItemsText: String,
    showedItems: List<Pair<String, Items?>>,
    favoriteBookList: List<Pair<String, Items?>>,
    navController: NavHostController,
    state: LazyGridState,
    toggleFavoriteBook: suspend (Items) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope?
) {
    if (showedItems.isNotEmpty()) {
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(
                top = 32.dp,
                bottom =  WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
        ) {
            items(
                items = showedItems,
                key = { it.first }
            ) { book ->
                BookCard(
                    items = book.second ?: Items(),
                    judgeScreen = screenName,
                    onClick = {
                        navController.navigate("${DetailsDestination.route}/${book.second?.id}/${screenName}")
                    },
                    toggleFavoriteBook = toggleFavoriteBook,
                    favoriteBookList = favoriteBookList,
                    sharedElementKey = book.second?.id,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = noItemsText,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("functionName")
@Stable
@Composable
private fun BookCard(
    modifier: Modifier = Modifier,
    items: Items,
    sharedElementKey: String?,
    judgeScreen: String,
    onClick: () -> Unit,
    toggleFavoriteBook: suspend (Items) -> Unit,
    favoriteBookList: List<Pair<String, Items?>>,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember{ mutableStateOf(false) }
    val sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")

    with(sharedTransitionScope) {
        if (animatedVisibilityScope != null) {
            Card(
                modifier = modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "Card${judgeScreen}${sharedElementKey}") ,
                        animatedVisibilityScope,
                        placeHolderSize = animatedSize
                    )
                    .fillMaxWidth()
                    .aspectRatio(0.5F),
                onClick = {
                    onClick()
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = items.volumeInfo?.title ?: stringResource(R.string.noData),
                        minLines = 2,
                        maxLines = 2,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.skipToLookaheadSize()
                    )
                    val imageUrl = items.volumeInfo?.imageLinks?.thumbnail?.replace("http","https") ?: R.drawable.no_image

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    toggleFavoriteBook(items)
                                }
                            }
                        ) {
                            if (
                                favoriteBookList.any { it.second?.id == items.id }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            } else if(isSystemInDarkTheme()) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.DarkGray
                                )
                            }
                        }
                    }


                    Box(modifier = Modifier.fillMaxSize()) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isLoading,
                            enter = fadeIn(tween(durationMillis = 300, delayMillis = 100)),
                            exit = fadeOut(tween(durationMillis = 300, delayMillis = 100)),
                            label = "Progress",
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                            )
                        }

                        AsyncImage(
                            model = ImageRequest.Builder(context = LocalContext.current)
                                .data(imageUrl)
                                .crossfade(100)
                                .placeholderMemoryCacheKey("image-key${sharedElementKey}")
                                .memoryCacheKey("image-key${sharedElementKey}")
                                .build(),
                            contentDescription = items.volumeInfo?.title ?: stringResource(R.string.noBookPhoto),
                            error = painterResource(R.drawable.ic_broken_image),
                            contentScale = ContentScale.FillWidth,
                            onSuccess = {
                                isLoading = true
                            },
                            modifier = Modifier
                                .sharedBounds(
                                    rememberSharedContentState(key = "BookImage${judgeScreen}${sharedElementKey}"),
                                    animatedVisibilityScope,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                )
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("functionName")
@Composable
private fun OriginalTabRow(
    pagerState: PagerState,
    titles: List<String>,
    selectedTabIndex: Int,
    changeSelectedTabIndex: (Int) -> Unit,
    homeViewModel: HomeViewModel,
    homeUiState: HomeUiState,
    isHome: Boolean,
    modifier: Modifier = Modifier,
){
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    with(sharedTransitionScope) {
        AnimatedVisibility(true, modifier = modifier) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .wrapContentSize()
                    .renderInSharedTransitionScopeOverlay(
                        renderInOverlay = {isTransitionActive && !isHome},
                        zIndexInOverlay = 1f,
                    )
                    .animateEnterExit(
                        enter = fadeIn() + slideInVertically {
                            it
                        },
                        exit = fadeOut(animationSpec = tween(0))
                    )
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            changeSelectedTabIndex(index)
                            homeViewModel.isShowFavoriteScreen(!homeUiState.showOnFavoriteScreen)
                        },
                        text = {
                            Text(
                                text = title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("functionName")
@Composable
private fun OriginalFAB(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isHome: Boolean,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")

    with(sharedTransitionScope) {
        AnimatedVisibility(true, modifier = modifier) {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Route.Search)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .sharedBounds(
                        rememberSharedContentState(key = "FAB"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .renderInSharedTransitionScopeOverlay(
                        renderInOverlay = {isTransitionActive && !isHome},
                        zIndexInOverlay = 1f,
                    )
                    .animateEnterExit(
                        enter = fadeIn() + slideInVertically {
                            it
                        },
                        exit = fadeOut(animationSpec = tween(0))
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
        }
    }
}

@Suppress("functionName")
@Stable
@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 本当はCircularProgressIndicatorを使いたかったが、LoginScreenからSuccessScreenに移る途中でかたつく問題が発生し、gifに切り替えた。
        LoadingImage()
    }
}

@Suppress("functionName")
@Stable
@Composable
private fun LoadingImage(
    modifier: Modifier = Modifier
) {
    val gifEnabledLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Surface(
        shape = RoundedCornerShape(60.dp),
        color = Color.White,
        modifier = Modifier.size(40.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(R.drawable._50__1_)
                .build(),
            imageLoader = gifEnabledLoader,
            modifier = modifier.padding(8.dp),
            contentDescription = null
        )
    }
}

@Suppress("functionName")
@Stable
@Composable
private fun ErrorScreen(
    errorType: String,
    errorDetails: String,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.ic_connection_error),
            contentDescription = stringResource(R.string.connectionError)
        )
        Text(
            text = if (errorType == "IOException") stringResource(R.string.connectionError) else if(errorDetails.contains("429")) stringResource(R.string.apiRequestOver) else stringResource(R.string.httpError),
        )
    }
}


@Suppress("functionName")
@Preview
@Composable
fun PreviewSuccessScreen1() {
    val fakeBookShelfList: List<Pair<String, Items?>> = listOf(
        "1" to Items(),
        "2" to Items(
            volumeInfo = VolumeInfo(
                title = "科学者たちの選択　発想と行動力の源泉",
                imageLinks = ImageLinks(
                    thumbnail = "R.drawable.ic_launcher_foreground"
                )
            )
        ),
        "3" to Items(
            volumeInfo = VolumeInfo(
                title = "手軽に学べる科学の重要テーマ200（サイエンス・アイ新書）",
                imageLinks = ImageLinks(
                    thumbnail = "R.drawable.ic_launcher_foreground"
                )
            )
        )
    )
    val searchUiState = SearchUiState()

    BookShelfTheme {
        HomeContents(
            pagerState = rememberPagerState { 0 },
            bookShelfItems = fakeBookShelfList,
            searchUiState = searchUiState,
            homeUiState = HomeUiState(),
            navController = rememberNavController(),
            animatedVisibilityScope = null,
            research = {},
            favoriteBookList = emptyList(),
            isFirstSearch ={},
            toggleFavoriteBook = {},
        )
    }
}