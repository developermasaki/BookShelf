package com.example.bookshelf.ui.screen

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil.ImageLoader
import com.example.bookshelf.network.VolumeInfo
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.bookshelf.R
import com.example.bookshelf.network.Items
import com.example.bookshelf.network.ImageLinks
import com.example.bookshelf.ui.theme.BookShelfTheme
import kotlinx.coroutines.launch
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Stable
@Composable
fun HomeContent(
    screenUiState: ScreenUiState,
    bookShelfContentUiState: BookShelfContentUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    navController: NavHostController,
    onValueChanged: (EditTextField, String) -> Unit,
    pressFabButton: () -> Unit,
    research: (Boolean) -> Unit,
    authorsListUp: (List<String?>?) -> String?,
    showPrice: (Double?, String?) -> String?,
    isShowDetailsScreen: () -> Unit,
    pickUpItem: (String?) -> Items?,
    bestImage: (Items?) -> String?,
    toggleFavoriteBook: suspend (Items) -> Unit,
    favoriteBookList: List<Items?>,
    isShowFavoriteScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = screenUiState,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(1000)
            ) togetherWith fadeOut(animationSpec = tween(1000))
        },
        label = "Animated HomeContent",
        modifier = modifier
    ) { targetState ->
        when(targetState) {
            is ScreenUiState.Loading -> LoginScreen()
            is ScreenUiState.Success -> SuccessScreen(
                bookShelfItems = bookShelfContentUiState.bookShelfItems,
                bookShelfContentUiState = bookShelfContentUiState,
                scrollBehavior = scrollBehavior,
                navController = navController,
                updateSearchShowOn = pressFabButton,
                onValueChanged = onValueChanged,
                research = research,
                authorsListUp = authorsListUp,
                showPrice = showPrice,
                isShowDetailsScreen = isShowDetailsScreen,
                pickUpItem = pickUpItem,
                bestImage = bestImage,
                favoriteBookList = favoriteBookList,
                toggleFavoriteBook = toggleFavoriteBook,
                isShowFavoriteScreen = isShowFavoriteScreen
            )
            is ScreenUiState.Error -> ErrorScreen(targetState.errorType, targetState.errorDetails)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }


/*
BookCardがネストされすぎて、引数ではスコープを渡すことが出来なかった。そこで、CompositionLocalProviderで渡すことにした。
ただ、NavHostではAnimatedVisibilityScopeを直接渡すことが出来ないので、AnimatedContentに変えようと思った。
しかし、NavHostでルートを定義できた方が後々画面を追加する際に便利かと思い、NavHostで対応しようと考えた。
だが、LazyVerticalGridには独自のScopeがあり、CompositionLocalProviderでは指定できず、AnimatedContentでScopeを渡そうとしたが、うまくアニメーション
できなかった。
そこで、SharedTransitionScopeだけCompositionLocalProviderを使用し、AnimatedVisibilityScopeはthis@Composableで指定できることに気づき、他のComposableも
同様の対応をした。
数日にわたる長い戦いだったが、なんとか思い描いた動きになった。
 */


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SuccessScreen(
    bookShelfItems: List<Items?>,
    bookShelfContentUiState: BookShelfContentUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    navController: NavHostController,
    updateSearchShowOn: () -> Unit,
    onValueChanged: (EditTextField, String) -> Unit,
    research: (Boolean) -> Unit,
    authorsListUp: (List<String?>?) -> String?,
    showPrice: (Double?, String?) -> String?,
    isShowDetailsScreen: () -> Unit,
    pickUpItem: (String?) -> Items?,
    bestImage: (Items?) -> String?,
    toggleFavoriteBook: suspend (Items) -> Unit,
    favoriteBookList: List<Items?>,
    isShowFavoriteScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var researchedIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var every40Index by rememberSaveable { mutableIntStateOf(40) }
    val state = rememberLazyGridState()
    val options = listOf("Favorite", "Search")


    // ある程度スクロールしたら、自動でAPIの読み込みをするため
    Log.d("SuccessScreen", "${bookShelfContentUiState.showOnFavoriteScreen},$bookShelfItems")
    LaunchedEffect(state, bookShelfContentUiState.firstSearch) {
        snapshotFlow { state.firstVisibleItemIndex }
            .collect {
                Log.d("gridState1", "hello${bookShelfContentUiState.firstSearch}, $it, $researchedIndex, $every40Index")
                if (it > every40Index - 20 && it > researchedIndex) {
                    research(false)
                    researchedIndex = it
                    every40Index += 40
                    Log.d(
                        "gridState2",
                        "research${bookShelfContentUiState.firstSearch}, ${it}, $researchedIndex"
                    )
                } else if (bookShelfContentUiState.firstSearch) {
                    Log.d("gridState3", "Search")
                    researchedIndex = 0
                    every40Index = 0
                }
            }
    }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.Grid,
                modifier = Modifier.fillMaxSize()
            ) {
                composable<Route.Grid> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (bookShelfContentUiState.showOnFavoriteScreen) {
                            OriginalLazyVertiaclGrid(
                                state = rememberLazyGridState(),
                                bookShelfContentUiState = bookShelfContentUiState,
                                scrollBehavior = scrollBehavior,
                                navController = navController,
                                isShowDetailsScreen = isShowDetailsScreen,
                                bestImage = bestImage,
                                toggleFavoriteBook = toggleFavoriteBook,
                                favoriteBookList = favoriteBookList,
                                showedItemsList = favoriteBookList,
                                animatedVisibilityScope = this@composable
                            )
                        } else {
                            OriginalLazyVertiaclGrid(
                                state = state,
                                bookShelfContentUiState = bookShelfContentUiState,
                                scrollBehavior = scrollBehavior,
                                navController = navController,
                                isShowDetailsScreen = isShowDetailsScreen,
                                bestImage = bestImage,
                                toggleFavoriteBook = toggleFavoriteBook,
                                favoriteBookList = favoriteBookList,
                                showedItemsList = bookShelfItems,
                                animatedVisibilityScope = this@composable
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .height(56.dp)
                                .wrapContentWidth()
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp),
                            shadowElevation = 4.dp,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .height(40.dp)
                            ){
                                options.forEachIndexed { index, s ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                        onClick = {
                                            selectedIndex = index
                                            isShowFavoriteScreen()
                                        },
                                        selected = index == if(bookShelfContentUiState.showOnFavoriteScreen) 0 else 1
                                    ){
                                        Text(s)
                                    }
                                }
                            }
                        }
                        FABfunction(
                            onClick = updateSearchShowOn,
                            navController = navController,
                            animatedVisibilityScope = this@composable,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }
                }
                composable(
                    "details/{itemId}",
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                ) { backStackEntry ->
                    DetailsScreen(
                        items = pickUpItem(backStackEntry.arguments?.getString("itemId")) ?: Items(),
                        authorsListUp = authorsListUp,
                        showPrice = showPrice,
                        sharedElementKey = backStackEntry.arguments?.getString("itemId")!!,
                        animatedVisibilityScope = this@composable,
                        navHostController = navController,
                        isShowDetailsScreen = isShowDetailsScreen,
                        bestImage = bestImage,
                        favoriteBookList = favoriteBookList,
                        toggleFavoriteBook = toggleFavoriteBook
                    )
                }
                composable<Route.Search> {
                    SearchScreen(
                        bookShelfContentUiState = bookShelfContentUiState,
                        state = state,
                        navController = navController,
                        updateSearchShowOn = updateSearchShowOn,
                        onValueChanged = onValueChanged,
                        research = research,
                        animatedVisibilityScope = this@composable,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun OriginalLazyVertiaclGrid(
    state: LazyGridState,
    bookShelfContentUiState: BookShelfContentUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    navController: NavHostController,
    isShowDetailsScreen: () -> Unit,
    bestImage: (Items?) -> String?,
    toggleFavoriteBook: suspend (Items) -> Unit,
    favoriteBookList: List<Items?>,
    showedItemsList: List<Items?>,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    if (showedItemsList.isNotEmpty()) {
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = modifier.padding(horizontal = 32.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(
                bottom = WindowInsets.safeDrawing.asPaddingValues()
                    .calculateBottomPadding()
            )
        ) {
            Log.d("items", "$showedItemsList")
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(48.dp))
            }
            items(
                items = showedItemsList.toList(),
                key = { UUID.randomUUID().toString() })
            { book ->
                BookCard(
                    items = book ?: Items(),
                    onClick = {
                        isShowDetailsScreen()
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
        AnimatedVisibility(
            visible = true
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if(bookShelfContentUiState.showOnFavoriteScreen) stringResource(R.string.noFavorite) else stringResource(R.string.noBook),
                )
            }
        }
    }
}

// TODO 画像の大きさをDetailScreenとの間で設定する

@OptIn(ExperimentalSharedTransitionApi::class)
@Stable
@Composable
private fun BookCard(
    modifier: Modifier = Modifier,
    items: Items,
    sharedElementKey: String?,
    onClick: () -> Unit,
    bestImage: (Items?) -> String?,
    toggleFavoriteBook: suspend (Items) -> Unit,
    favoriteBookList: List<Items?>,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found"),
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    var isLoading by remember{ mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    with(sharedTransitionScope) {
        Card(
            modifier = modifier
                .sharedBounds(
                    rememberSharedContentState(key = "BookCard${sharedElementKey}"),
                    animatedVisibilityScope,
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
                                Log.d("Icon", "Tap")
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
                            .size(40.dp)
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

@Stable
@Composable
private fun LoginScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 本当はCircularProgressIndicatorを使いたかったが、LoginScreenからSuccessScreenに移る途中でかたつく問題が発生し、gifに切り替えた。
        LoadingImage(size = 40)
//        CircularProgressIndicator(
//            modifier = Modifier.size(100.dp)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = stringResource(R.string.loading)
//        )
    }
}

@Stable
@Composable
fun ErrorScreen(
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

@Stable
@Composable
fun LoadingImage(
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Stable
@Composable
fun FABfunction(
    onClick: () -> Unit,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")

    with(sharedTransitionScope) {
        FloatingActionButton(
            onClick = {
                navController.navigate(Route.Search)
                onClick()
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewSuccessScreen() {
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

    BookShelfTheme {
        SuccessScreen(
            bookShelfItems = fakeBookShelfList,
            bookShelfContentUiState = BookShelfContentUiState(),
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            navController = rememberNavController(),
            updateSearchShowOn = {},
            onValueChanged = {editTextField, string -> run {} },
            research = {},
            authorsListUp = { fake1 -> "" },
            showPrice = { fake2, fake3 -> ""},
            isShowDetailsScreen = {},
            pickUpItem = {fake4 -> Items() },
            bestImage = {fake5 -> "" },
            favoriteBookList = emptyList(),
            isShowFavoriteScreen = {},
            toggleFavoriteBook = {}
        )
    }
}