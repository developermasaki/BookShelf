package com.example.bookshelf.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookshelf.R
import com.example.bookshelf.model.Items
import com.example.bookshelf.ui.AppViewModelProvider
import com.example.bookshelf.ui.TopAppBar
import com.example.bookshelf.ui.navigation.LocalSharedTransitionScope
import com.example.bookshelf.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object EditDestination: NavigationDestination {
    override val route = "edit"
    override val titleRes = R.string.edit
    const val ITEM_ID_ARG = "editItemId"
    val routeWithArgs = "$route/{$ITEM_ID_ARG}"
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Suppress("functionName")
@Stable
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    sharedElementKey: String,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    homeViewModel: HomeViewModel,
    editViewModel: EditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // 戻る動作をしたときの処理
    var isBack by remember { mutableStateOf(false) }
    BackHandler {
        isBack = true
    }
    if(isBack) {
        confirmCancel(navController)
    }

    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    val items: Items = homeViewModel.pickUpItemBookShelf(sharedElementKey)
    val imageUrl = items.volumeInfo?.imageLinks?.thumbnail?.replace("http","https") ?: R.drawable.no_image
    val coroutineScope = rememberCoroutineScope()


    with(sharedTransitionScope) {
        if (animatedVisibilityScope != null) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        scrollBehavior = null,
                        titleText = stringResource(R.string.details),
                        canNavigateBack = true,
                        isDisplayConfirmBack = true,
                        confirmIsBack = { confirmCancel(it)},
                        navController = navController,
                    )
                },
                bottomBar = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isBack = true
                                }
                            ){
                                Text(
                                    text = stringResource(R.string.cancel)
                                )
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        editViewModel.editFavoriteBook(sharedElementKey)
                                        if(navController.currentBackStackEntry != null) navController.popBackStack()
                                    }
                                }
                            ){
                                Text(
                                    text = stringResource(R.string.save)
                                )
                            }
                        }
                    }
                }
            ) { contentPadding ->
                Surface(
                    modifier = modifier
                        .sharedBounds(
                            rememberSharedContentState(key = "CardEdit${sharedElementKey}"),
                            animatedVisibilityScope,
                        )
                        .fillMaxSize()
                        .padding(
                            top = contentPadding.calculateTopPadding(),
                            bottom = 80.dp
                        )
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(
                            modifier = Modifier
                                .height(24.dp)
                                .fillMaxWidth()
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context = LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .placeholderMemoryCacheKey("image-key${sharedElementKey}")
                                .memoryCacheKey("image-key${sharedElementKey}")
                                .build(),
                            contentDescription = items.volumeInfo?.title ?: stringResource(R.string.noBookPhoto),
                            error = painterResource(R.drawable.ic_broken_image),
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .sharedBounds(
                                    rememberSharedContentState(key = "BookImageEdit${sharedElementKey}"),
                                    animatedVisibilityScope,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                )
                                .height(200.dp)
                                .width(150.dp)
                        )
                        Spacer(
                            modifier = Modifier
                                .height(24.dp)
                                .fillMaxWidth()
                        )
                        CreateEditSection(
                            editUiState = editViewModel.editUiState,
                            showCurrencyUnit = ::showCurrencyUnit,
                            onValueChange = editViewModel::updateEditUiState,
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

@Suppress("functionName")
@Stable
@Composable
fun CreateEditSection(
    editUiState: EditUiState,
    showCurrencyUnit: (String?) -> String?,
    onValueChange: (EditUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, top = 32.dp ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CreateEditItem(
            categoryName = stringResource(R.string.title),
            categoryContent = editUiState.title,
            placeHolder = stringResource(R.string.title),
            onValueChange = {onValueChange(editUiState.copy(title = it))}
        )
        CreateEditItem(
            categoryName = stringResource(R.string.author),
            categoryContent = editUiState.author,
            placeHolder = stringResource(R.string.author),
            onValueChange = { onValueChange(editUiState.copy(author = it)) }
        )
        CreateEditItem(
            categoryName = stringResource(R.string.description),
            categoryContent = editUiState.description,
            placeHolder = stringResource(R.string.description),
            onValueChange = { onValueChange(editUiState.copy(description = it)) }
        )
        CreateEditItem(
            categoryName = stringResource(R.string.category),
            categoryContent = editUiState.category,
            placeHolder = stringResource(R.string.category),
            onValueChange = { onValueChange(editUiState.copy(category = it)) }
        )
        CreateEditItem(
            categoryName = stringResource(R.string.publishedDate),
            categoryContent = editUiState.publishedDate,
            placeHolder = stringResource(R.string.publishedDate),
            onValueChange = { onValueChange(editUiState.copy(publishedDate = it)) }
        )
        CreateEditItem(
            categoryName = stringResource(R.string.publisher),
            categoryContent = editUiState.publisher,
            placeHolder = stringResource(R.string.publisherExample),
            onValueChange = { onValueChange(editUiState.copy(publisher = it)) }
        )
        CreateEditItem(
            categoryName = stringResource(R.string.price) + "(" + stringResource(R.string.unit) + "(${showCurrencyUnit(editUiState.currencyCode)})" + ")",
            categoryContent = editUiState.price,
            placeHolder = stringResource(R.string.priceExample),
            onValueChange = { onValueChange(editUiState.copy(price = it)) }
        )
        CreateEditItem(
            categoryName = stringResource(R.string.page),
            categoryContent = editUiState.page,
            placeHolder = stringResource(R.string.pageExample),
            keyboardType = KeyboardType.Number,
            onValueChange = { onValueChange(editUiState.copy(page = it)) }
        )
        CreateEditItem(
            categoryName = stringResource(R.string.isbn),
            categoryContent = editUiState.isbnType,
            placeHolder = stringResource(R.string.isbnCategoryExample),
            onValueChange = { onValueChange(editUiState.copy(isbnType = it)) }
        )
        CreateEditItem(
            categoryName = editUiState.isbnType,
            categoryContent = editUiState.isbnNumber,
            placeHolder = stringResource(R.string.isbnNumberExample),
            keyboardType = KeyboardType.Number,
            onValueChange = { onValueChange(editUiState.copy(isbnNumber = it)) }
        )
        Spacer(
            modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        )
    }
}

@Suppress("functionName")
@Composable
fun CreateEditItem(
    modifier: Modifier = Modifier,
    categoryName: String,
    categoryContent: String,
    placeHolder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = categoryName,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .sizeIn(maxHeight = 80.dp),
            style = MaterialTheme.typography.titleMedium
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            TextField(
                value = categoryContent,
                onValueChange = {
                    onValueChange(it)
                },
                placeholder = {
                    Text(
                        text = placeHolder
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType
                ),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            )
        }
    }
}

@Suppress("functionName")
@Composable
fun confirmCancel(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
): Boolean {
    var isOpen by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isBack by remember { mutableStateOf(true) }

    if (isOpen) {
        AlertDialog(
            onDismissRequest = {
                isOpen = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if(navHostController.currentBackStackEntry != null) {
                            isBack = true
                            isOpen = false
                            keyboardController?.hide()
                            navHostController.popBackStack()
                        }
                    }
                ){
                    Text(
                        text = stringResource(R.string.disposeEdit)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isBack = false
                        isOpen = false
                    }
                ){
                    Text(
                        text = stringResource(R.string.cancel)
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = stringResource(R.string.confirmBack),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.caution)
                )
            },
            modifier = modifier
        )
    }
    return isBack
}