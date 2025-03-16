package com.example.bookshelf.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookshelf.R
import com.example.bookshelf.model.ImageLinks
import com.example.bookshelf.model.IndustryIdentifiers
import com.example.bookshelf.model.Items
import com.example.bookshelf.model.RetailPrice
import com.example.bookshelf.model.SaleInfo
import com.example.bookshelf.model.VolumeInfo
import com.example.bookshelf.ui.AppViewModelProvider
import com.example.bookshelf.ui.TopAppBar
import com.example.bookshelf.ui.navigation.LocalSharedTransitionScope
import com.example.bookshelf.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object DetailsDestination: NavigationDestination {
    override val route = "details"
    override val titleRes = R.string.details
    const val ITEM_ID_ARG = "itemId"
    const val SCREEN_NAME_ARG = "screenName"
    val routeWithArgs = "$route/{$ITEM_ID_ARG}/{$SCREEN_NAME_ARG}"
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Suppress("functionName")
@Stable
@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    sharedElementKey: String,
    judgeScreen: String,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    homeViewModel: HomeViewModel,
    detailsViewModel: DetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    BackHandler {
        if(navController.currentBackStackEntry != null) navController.popBackStack()
    }

    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    var screenType by remember { mutableStateOf(judgeScreen) }
    val uiState by detailsViewModel.uiState.collectAsState()
    val imageUrl = uiState.item.volumeInfo?.imageLinks?.thumbnail?.replace("http","https") ?: R.drawable.no_image

    val coroutineScope = rememberCoroutineScope()

    with(sharedTransitionScope) {
        if (animatedVisibilityScope != null) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        scrollBehavior = null,
                        titleText = stringResource(R.string.details),
                        canNavigateBack = true,
                        navController = navController,
                    )
                }
            ) { contentPadding ->
                Surface(
                    modifier = modifier
                        .sharedBounds(
                            rememberSharedContentState(key = "Card${screenType}${sharedElementKey}"),
                            animatedVisibilityScope,
                        )
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding())
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .height(24.dp)
                                    .fillMaxWidth()
                                    .padding(end = 32.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            homeViewModel.toggleFavoriteBook(uiState.item)
                                            if(judgeScreen == "favorite" && navController.currentBackStackEntry != null) navController.popBackStack()
                                        }
                                    }
                                ) {
                                    if (
                                        uiState.isFavorite
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
                            AsyncImage(
                                model = ImageRequest.Builder(context = LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .placeholderMemoryCacheKey("image-key${sharedElementKey}")
                                    .memoryCacheKey("image-key${sharedElementKey}")
                                    .build(),
                                contentDescription = uiState.item.volumeInfo?.title ?: stringResource(R.string.noBookPhoto),
                                error = painterResource(R.drawable.ic_broken_image),
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier
                                    .sharedBounds(
                                        rememberSharedContentState(key = "BookImage${screenType}${sharedElementKey}"),
                                        animatedVisibilityScope,
                                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                    )
                                    .height(200.dp)
                                    .width(150.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .padding(end = 32.dp)
                            ) {
                                if (uiState.isFavorite) {
                                    FilledIconButton(
                                        onClick = {
                                            screenType = "Edit"
                                            navController.navigate("${EditDestination.route}/${uiState.item.id}")
                                        }
                                    ){
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.edit)
                                        )
                                    }
                                }
                            }
                            ColumnList(
                                items = uiState.item,
                                authorsListUp = ::authorsListUp,
                                showPrice = ::showPrice,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .skipToLookaheadSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Suppress("functionName")
@Stable
@Composable
fun ColumnList(
    items: Items?,
    authorsListUp: (List<String?>?) -> String?,
    showPrice: (Double?, String?) -> String?,
    modifier: Modifier = Modifier
) {
    //ISBNがISBN10やISBN13、otherで返されるので、優先的にISBN13を表示できるようにしている
    val isbn: IndustryIdentifiers? = items?.volumeInfo?.industryIdentifiers?.find {it?.type == "ISBN_13"}
        ?: items?.volumeInfo?.industryIdentifiers?.find { it?.type == "ISBN_10" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, top = 32.dp ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedContentList(
            category = stringResource(R.string.title),
            content = items?.volumeInfo?.title ?: stringResource(R.string.noData),
        )
        AnimatedContentList(
            category = stringResource(R.string.author),
            content = authorsListUp(items?.volumeInfo?.authors) ?: stringResource(R.string.noData),
        )
        AnimatedContentList(
            category = stringResource(R.string.description),
            content = items?.volumeInfo?.description ?: stringResource(R.string.noData),
        )
        AnimatedContentList(
            category = stringResource(R.string.category),
            content = items?.volumeInfo?.categories?.get(0) ?: stringResource(R.string.noData),
        )
        AnimatedContentList(
            category = stringResource(R.string.publishedDate),
            content = items?.volumeInfo?.publishedDate ?: stringResource(R.string.noData),
        )
        AnimatedContentList(
            category = stringResource(R.string.publisher),
            content = items?.volumeInfo?.publisher ?: stringResource(R.string.noData),
        )
        AnimatedContentList(
            category = stringResource(R.string.price),
            content = showPrice(items?.saleInfo?.retailPrice?.amount, items?.saleInfo?.retailPrice?.currencyCode)
                ?: stringResource(R.string.noData),
        )
        AnimatedContentList(
            category = stringResource(R.string.page),
            content = items?.volumeInfo?.pageCount?.toString() ?: stringResource(R.string.noData),
        )
        AnimatedContentList(
            category = isbn?.type ?: stringResource(R.string.isbn),
            content = isbn?.identifier ?: stringResource(R.string.noData)
        )
        Spacer(
            modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("functionName")
@Stable
@Composable
fun AnimatedContentList(
    category: String,
    content: String,
    modifier: Modifier = Modifier
) {
    var openedContentListId by rememberSaveable{ mutableStateOf("") }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = openedContentListId,
            label = "AnimatedContentList"
        ) { targetState ->
            if(targetState != category) {
                ClosedContentList(
                    category = category,
                    content = content,
                    openedContentListIdChange = { key -> openedContentListId = key },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
            } else {
                OpenedContentList(
                    category = category,
                    content = content,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    changeToClosedContent = { openedContentListId = "" }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("functionName")
@Stable
@Composable
fun ClosedContentList(
    category: String,
    content: String,
    openedContentListIdChange: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Row(
            modifier = modifier
                .sharedBounds(
                    rememberSharedContentState(key = "Layout${category}"),
                    animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "Category${category}"),
                        animatedVisibilityScope
                    )
                    .wrapContentHeight()
                    .width(100.dp)
                ,
                style = MaterialTheme.typography.titleMedium
            )
            VerticalDivider(
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "Divider${category}"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .height(60.dp)
                    .width(1.dp)
            )
            Surface(
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "Content${category}"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .weight(1F)
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        openedContentListIdChange(category)
                    },
                tonalElevation = 4.dp
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .skipToLookaheadSize()
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("functionName")
@Stable
@Composable
fun OpenedContentList(
    category: String,
    content: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    changeToClosedContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .sharedBounds(
                    rememberSharedContentState(key = "Layout${category}"),
                    animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Text(
                text = category,
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "Category${category}"),
                        animatedVisibilityScope,
                    )
                    .width(100.dp)
                    .wrapContentHeight()
                    .sizeIn(maxHeight = 80.dp)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider(
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "Divider${category}"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .fillMaxWidth()
                    .height(1.dp)
            )
            Surface(
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "Content${category}"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable {
                        changeToClosedContent()
                    }
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .skipToLookaheadSize()
                        .wrapContentHeight()
                        .sizeIn(maxHeight = 300.dp)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.Start)
                        .verticalScroll(rememberScrollState())

                )
            }
        }
    }
}

@Preview
@Suppress("functionName")
@Stable
@Composable
fun PreviewDetailsScreen() {
    val fakeItem = Items(
        volumeInfo = VolumeInfo(
            title = "タイトル",
            authors = listOf("筆者"),
            publisher = "出版社",
            publishedDate = "出版日",
            description = "説明",
            pageCount = 122,
            categories = listOf("メインカテゴリー"),
            imageLinks = ImageLinks(
                thumbnail = ""
            )
        ),
        saleInfo = SaleInfo(
            retailPrice = RetailPrice(
                amount = 10.0,
                currencyCode = "JPA"
            )
        )
    )
    MaterialTheme {
        ColumnList(
            items = fakeItem,
            authorsListUp = {""},
            showPrice = {_, _ -> ""},
        )
    }
}