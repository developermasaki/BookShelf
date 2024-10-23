package com.example.bookshelf.ui.screen

import android.os.Build.VERSION.SDK_INT
import android.util.Log
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
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import com.example.bookshelf.network.ImageLinks
import com.example.bookshelf.network.Items
import com.example.bookshelf.network.VolumeInfo
import com.example.bookshelf.ui.TopAppBar1
import com.example.bookshelf.ui.component.enterAlwaysScrollBehavior
import com.example.bookshelf.ui.navigation.LocalSharedTransitionScope
import com.example.bookshelf.ui.theme.BookShelfTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalSharedTransitionApi::class
)
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
    val screenUiState = searchViewModel.screenUiState2
    val searchUiState = searchViewModel.searchUiState
    val homeUiState = homeViewModel.homeUiState
    val isHome = remember { mutableStateOf(false) }
    LaunchedEffect(isHome){
        delay(1000)
        isHome.value = true
    }

    val scrollBehavior = enterAlwaysScrollBehavior(
        state = remember { TopAppBarState(-Float.MAX_VALUE, 0f, 0f)}
    )
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")

    // HorizontalPagerのため
    val titles = listOf("Favorite", "Search")
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState{titles.size}
    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if(!pagerState.isScrollInProgress)
            selectedTabIndex = pagerState.currentPage
    }

    with(sharedTransitionScope) {
        Column(modifier = modifier.fillMaxSize()) {
            AnimatedVisibility(true) {
                TopAppBar1(
                    scrollBehavior = scrollBehavior,
                    titleText = stringResource(R.string.app_name),
                    navController = navController,
                    modifier = Modifier
                        .renderInSharedTransitionScopeOverlay(
                            zIndexInOverlay = 1f,
                        )
                        .animateEnterExit(
                            enter = fadeIn() + slideInVertically {
                                it
                            },
                            exit = fadeOut() + slideOutVertically {
                                it
                            }
                        )
                )
            }
            androidx.compose.animation.AnimatedVisibility(true) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier
                        .renderInSharedTransitionScopeOverlay(
                            renderInOverlay = {isTransitionActive && !isHome.value},
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
                                selectedTabIndex = index
                                homeViewModel.isShowFavoriteScreen(!homeUiState.showOnFavoriteScreen)
                            },
                            text = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = screenUiState,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(1000)
                        ) togetherWith fadeOut(animationSpec = tween(1000))
                    },
                    label = "Animated HomeContent",
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
                ) { targetState ->
                    when(targetState) {
                        is ScreenUiState2.Loading -> LoginScreen2(pagerState)
                        is ScreenUiState2.Success -> HomeContents(
                            pagerState = pagerState,
                            searchUiState = searchUiState,
                            homeUiState = homeUiState,
                            navController = navController,
                            animatedVisibilityScope = animatedVisibilityScope,
                            favoriteBookList = favoriteBookList,
                            research = searchViewModel::getBookShelfItems,
                            bestImage = searchViewModel::bestImage,
                            toggleFavoriteBook = homeViewModel::toggleFavoriteBook,
                            isFirstSearch = searchViewModel::isFirstSearch,
                        )
                        is ScreenUiState2.Error -> ErrorScreen2(targetState.errorType, targetState.errorDetails)
                    }
                }
                FABfunction2(
                    navController = navController,
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                )
            }
        }
    }
}

@Stable
@Composable
fun HomeContents(
    pagerState: PagerState,
    searchUiState: SearchUiState,
    homeUiState: HomeUiState,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    favoriteBookList: List<Items?>,
    research: (Boolean) -> Unit,
    bestImage: (Items?) -> String?,
    toggleFavoriteBook: suspend (Items) -> Unit,
    isFirstSearch: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {


    // ある程度スクロールしたら、自動でAPIの読み込みをするため
    val state = rememberLazyGridState()
    var researchedIndex by rememberSaveable { mutableIntStateOf(0) }
    var every40Index by rememberSaveable { mutableIntStateOf(40) }
    LaunchedEffect(state, searchUiState.firstSearch) {
        snapshotFlow { state.firstVisibleItemIndex }
            .collect {
                Log.d("homeContents1", "hello${searchUiState.firstSearch}, $it, $researchedIndex, $every40Index")
                if (!homeUiState.showOnFavoriteScreen) {
                    if (it > every40Index - 20 && it > researchedIndex) {
                        research(false)
                        researchedIndex = it
                        every40Index += 40
                        Log.d(
                            "homeContents2",
                            "research${searchUiState.firstSearch}, ${it}, $researchedIndex"
                        )
                    } else if (searchUiState.firstSearch) {
                        Log.d("homeContents3", "firstSearch")
                        researchedIndex = 0
                        every40Index = 0
                    }
                }
            }
    }
    if(searchUiState.firstSearch) {
        isFirstSearch(false)
        state.requestScrollToItem(0)
    }

    HorizontalPager(
        state = pagerState
    ){ index ->
        if (index == 0) {
            OriginalLazyVerticalGrid2(
                state = remember {
                    LazyGridState(0, 0)
                },
                showedItems = favoriteBookList,
                navController = navController,
                bestImage = bestImage,
                toggleFavoriteBook = toggleFavoriteBook,
                favoriteBookList = favoriteBookList,
                animatedVisibilityScope = animatedVisibilityScope,
                text = stringResource(R.string.noFavorite),
                modifier = modifier
            )
        } else {
            OriginalLazyVerticalGrid2(
                state = state,
                showedItems = searchUiState.bookShelfItems,
                navController = navController,
                bestImage = bestImage,
                toggleFavoriteBook = toggleFavoriteBook,
                favoriteBookList = favoriteBookList,
                animatedVisibilityScope = animatedVisibilityScope,
                text = stringResource(R.string.noBook),
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Stable
@Composable
private fun OriginalLazyVerticalGrid2(
    state: LazyGridState,
    text: String,
    showedItems: List<Items?>,
    favoriteBookList: List<Items?>,
    navController: NavHostController,
    bestImage: (Items?) -> String?,
    toggleFavoriteBook: suspend (Items) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier
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
                items = showedItems.toList(),
                key = { UUID.randomUUID().toString()})
            { book ->
                BookCard2(
                    items = book ?: Items(),
                    onClick = {
                        navController.navigate("details/${book?.id}")
                    },
                    bestImage = bestImage,
                    toggleFavoriteBook = toggleFavoriteBook,
                    favoriteBookList = favoriteBookList,
                    sharedElementKey = book?.id,
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
                text = text,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Stable
@Composable
private fun BookCard2(
    modifier: Modifier = Modifier,
    items: Items,
    sharedElementKey: String?,
    onClick: () -> Unit,
    bestImage: (Items?) -> String?,
    toggleFavoriteBook: suspend (Items) -> Unit,
    favoriteBookList: List<Items?>,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found"),
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    var isLoading by remember{ mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    with(sharedTransitionScope) {
        if (animatedVisibilityScope != null) {
            Card(
                modifier = modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "BookCard${sharedElementKey}"),
                        animatedVisibilityScope,
                        placeHolderSize = animatedSize
                    )
                    .fillMaxWidth()
                    .aspectRatio(0.5F),
                onClick = onClick
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
                    val imageUrl = bestImage(items)?.replace("http","https") ?: R.drawable.no_image

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
                                favoriteBookList.any { it?.volumeInfo?.title == items.volumeInfo?.title }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.Red
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
                                    rememberSharedContentState(key = "BookImage${sharedElementKey}"),
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
@Stable
@Composable
fun FABfunction2(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")

    with(sharedTransitionScope) {
        if (animatedVisibilityScope != null) {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Route.Search)
                },
                modifier = modifier
                    .size(56.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "FAB"),
                        animatedVisibilityScope
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

@Stable
@Composable
private fun LoginScreen2(
    state: PagerState,
    modifier: Modifier = Modifier
) {
    state.requestScrollToPage(1)
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 本当はCircularProgressIndicatorを使いたかったが、LoginScreenからSuccessScreenに移る途中でかたつく問題が発生し、gifに切り替えた。
        LoadingImage2(size = 40)
    }
}

@Stable
@Composable
fun LoadingImage2(
    size: Int,
    modifier: Modifier = Modifier
) {
    val gifEnabledLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()
    Surface(
        shape = RoundedCornerShape(60.dp),
        color = Color.White,
        modifier = Modifier.size(size.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(R.drawable._50__1_)
                .crossfade(true)
                .build(),
            imageLoader = gifEnabledLoader,
            modifier = modifier.padding(8.dp),
            contentDescription = null
        )
    }
}

@Stable
@Composable
fun ErrorScreen2(
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
        Log.d("errorDetails", errorDetails)
        Text(
            text = if (errorType == "IOException") stringResource(R.string.connectionError) else if(errorDetails.contains("429")) stringResource(R.string.apiRequestOver) else stringResource(R.string.httpError),
        )
    }
}


@Preview
@Composable
fun PreviewSuccessScreen1() {
    val fakeBookShelfList: List<Items?> = listOf(
        Items(),
        Items(
            volumeInfo = VolumeInfo(
                title = "図解 始まりの科学",
                imageLinks = ImageLinks(
                    thumbnail = "R.drawable.ic_launcher_foreground"
                )
            )
        ),
        Items(
            volumeInfo = VolumeInfo(
                title = "科学者たちの選択　発想と行動力の源泉",
                imageLinks = ImageLinks(
                    thumbnail = "R.drawable.ic_launcher_foreground"
                )
            )
        ),
        Items(
            volumeInfo = VolumeInfo(
                title = "手軽に学べる科学の重要テーマ200（サイエンス・アイ新書）",
                imageLinks = ImageLinks(
                    thumbnail = "R.drawable.ic_launcher_foreground"
                )
            )
        )
    )
    val searchUiState = SearchUiState(
        bookShelfItems = fakeBookShelfList
    )

    BookShelfTheme {
        HomeContents(
            pagerState = rememberPagerState { 0 },
            searchUiState = searchUiState,
            homeUiState = HomeUiState(),
            navController = rememberNavController(),
            animatedVisibilityScope = null,
            research = {},
            bestImage = {fake5 -> "" },
            favoriteBookList = emptyList(),
            isFirstSearch ={},
            toggleFavoriteBook = {}
        )
    }
}