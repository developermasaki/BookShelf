package com.example.bookshelf.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bookshelf.R
import com.example.bookshelf.data.local.LocalDataProvider
import com.example.bookshelf.ui.TopAppBar
import com.example.bookshelf.ui.navigation.LocalSharedTransitionScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class
)
@Suppress("functionName")
@Stable
@Composable
fun SearchScreen(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    val searchUiState = searchViewModel.searchUiState
    val coroutineScope = rememberCoroutineScope()

    var openedSearchScreen by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(openedSearchScreen, !sharedTransitionScope.isTransitionActive) {
        if(!sharedTransitionScope.isTransitionActive){
            if (openedSearchScreen == 0) {
                openedSearchScreen += 1
                focusRequester.requestFocus()
            }
        }
    }

    BackHandler {
        coroutineScope.launch {
            keyboardController?.hide()
            delay(100)
            focusManager.clearFocus()
            if(navController.currentBackStackEntry != null) navController.popBackStack()
        }
    }

    with(sharedTransitionScope) {
        Scaffold(
            topBar = {
                TopAppBar(
                    scrollBehavior = null,
                    titleText = stringResource(R.string.search),
                    navController = navController,
                )
            }
        ) { contentPadding ->
            Column(
                modifier = modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "FAB"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .fillMaxSize()
                    .padding(end = 32.dp, start = 32.dp, top = contentPadding.calculateTopPadding())
                    .imePadding()
                    .imeNestedScroll()
                    .verticalScroll(rememberScrollState())
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) {
                        coroutineScope.launch {
                            keyboardController?.hide()
                            delay(100)
                            focusManager.clearFocus()
                        }
                    },
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dialogDescription),
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.weight(1F)) {
                    TextFieldList(
                        searchUiState = searchUiState,
                        navController = navController,
                        keyboardController = keyboardController,
                        focusRequester = focusRequester,
                        onValueChanged = searchViewModel::updateEditTextField,
                        research = searchViewModel::getBookShelfItems,
                        isShowFavoriteScreen = homeViewModel::isShowFavoriteScreen,
                    )
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            keyboardController?.hide()
                            if(navController.currentBackStackEntry != null) navController.popBackStack()
                            searchViewModel.getBookShelfItems(true)
                            homeViewModel.isShowFavoriteScreen(false)
                        }
                    },
                    enabled = searchUiState.judgeIsNoInput,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .sizeIn(maxWidth = 200.dp)
                ) {
                    Text(
                        text = stringResource(R.string.search)
                    )
                }
                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }
}

@Suppress("functionName")
@Stable
@Composable
fun TextFieldList(
    searchUiState: SearchUiState,
    navController: NavHostController,
    keyboardController: SoftwareKeyboardController?,
    focusRequester: FocusRequester,
    onValueChanged: (EditTextField, String) -> Unit,
    research: suspend (Boolean) -> Unit,
    isShowFavoriteScreen: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchToast = stringResource(R.string.searchToast)
    val dialogList = LocalDataProvider.list
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CustomTextField(
            value = searchUiState.fullTextSearch,
            leadingIcon = dialogList[0].icon,
            label = stringResource(dialogList[0].name),
            key = EditTextField.FullTextSearch,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(),
            onValueChanged = onValueChanged,
            modifier = Modifier.focusRequester(focusRequester)
        )
        CustomTextField(
            value = searchUiState.titleSearch,
            leadingIcon = dialogList[1].icon,
            label = stringResource(dialogList[1].name),
            key = EditTextField.TitleSearch,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(),
            onValueChanged = onValueChanged,
        )
        CustomTextField(
            value = searchUiState.authorSearch,
            leadingIcon = dialogList[2].icon,
            label = stringResource(dialogList[2].name),
            key = EditTextField.AuthorSearch,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(),
            onValueChanged = onValueChanged
        )
        CustomTextField(
            value = searchUiState.publishingCompany,
            leadingIcon = dialogList[3].icon,
            label = stringResource(dialogList[3].name),
            key = EditTextField.PublishingCompany,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchUiState.judgeIsNoInput) {
                        coroutineScope.launch {
                            keyboardController?.hide()
                            if(navController.currentBackStackEntry != null) {navController.popBackStack()}
                            research(true)
                            isShowFavoriteScreen(false)
                        }
                    } else {
                        Toast.makeText(context, searchToast, Toast.LENGTH_LONG ).show()
                    }
                }
            ),
            onValueChanged = onValueChanged,
        )
    }
}

@Suppress("functionName")
@Stable
@Composable
fun CustomTextField(
    value: String,
    leadingIcon: Int,
    label: String,
    key: EditTextField,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    onValueChanged: (EditTextField, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { inputText ->
            textFieldValue = inputText
            onValueChanged(key,inputText.text)
        },
        leadingIcon = {
            Icon(painter = painterResource(leadingIcon), contentDescription = null)
        },
        label = {
            Text(
                text = label
            )
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier
            .sizeIn(minWidth = 600.dp)
    )
}