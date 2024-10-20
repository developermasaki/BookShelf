package com.example.bookshelf.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bookshelf.R
import com.example.bookshelf.data.local.DialogListModel
import com.example.bookshelf.data.local.LocalDataProvider
import kotlinx.coroutines.launch

/*
本当はDialogで対応しようと思ったが、DialogをFABからアニメーションすることができないと分かり、HomeContent全体に表示
する方法に切り替えた。また、Dialogの背景の薄黒い色が気にいらなかったのも理由である。
 */

// TODO 全文検索で複数の言葉をANDで検索できるようにする
// TODO 検索欄を×で消せるようにする

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    bookShelfContentUiState: BookShelfContentUiState,
    updateSearchShowOn: () -> Unit,
    navController: NavHostController,
    state: LazyGridState,
    onValueChanged: (EditTextField, String) -> Unit,
    research: (Boolean) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val freeFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        focusManager.clearFocus()
        navController.navigateUp()
        updateSearchShowOn()
    }

    with(sharedTransitionScope) {
        Column(
            modifier = modifier
                .sharedElement(
                    rememberSharedContentState(key = "FAB"),
                    animatedVisibilityScope
                )
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .imePadding()
                .imeNestedScroll()
                .verticalScroll(rememberScrollState())
                .clickable(
                    interactionSource = null,
                    indication = null
                ) {
                    focusManager.clearFocus()
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
                DialogList(
                    dialogList = LocalDataProvider.list,
                    bookShelfContentUiState = bookShelfContentUiState,
                    onValueChanged = onValueChanged,
                    freeFocusRequester = freeFocusRequester,
                    research = research,
                    navController = navController,
                    keyboardController = keyboardController,
                    modifier = Modifier
                )
            }
            Button(
                onClick = {
                    coroutineScope.launch{
                        keyboardController?.hide()
                        navController.navigateUp()
                        research(true)
                        state.scrollToItem(0)
                    }
                },
                enabled = bookShelfContentUiState.judgeIsNoInput,
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

@Composable
fun DialogList(
    dialogList: List<DialogListModel>,
    bookShelfContentUiState: BookShelfContentUiState,
    onValueChanged: (EditTextField, String) -> Unit,
    freeFocusRequester: FocusRequester,
    research: (Boolean) -> Unit,
    navController: NavHostController,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchToast = stringResource(R.string.searchToast)

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CustomTextField(
            value = bookShelfContentUiState.fullTextSearch,
            leadingIcon = dialogList[0].icon,
            label = stringResource(dialogList[0].name),
            key = EditTextField.FullTextSearch,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(),
            onValueChanged = onValueChanged,
            modifier = Modifier.focusRequester(freeFocusRequester)
        )
        CustomTextField(
            value = bookShelfContentUiState.titleSearch,
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
            value = bookShelfContentUiState.authorSearch,
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
            value = bookShelfContentUiState.publishingCompany,
            leadingIcon = dialogList[3].icon,
            label = stringResource(dialogList[3].name),
            key = EditTextField.PublishingCompany,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (bookShelfContentUiState.judgeIsNoInput) {
                        keyboardController?.hide()
                        navController.navigateUp()
                        research(true)
                    } else {
                        Toast.makeText(context, searchToast, Toast.LENGTH_LONG ).show()
                    }
                }
            ),
            onValueChanged = onValueChanged,
            )
    }
}

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
    OutlinedTextField(
        value = value,
        onValueChange = {
            inputText: String -> onValueChanged(key,inputText)
        },
        leadingIcon = {
            Icon(painter = painterResource(leadingIcon), contentDescription = null)
        },
        label = {
            Text(
                text = label
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {}
            ){

            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier
            .sizeIn(minWidth = 600.dp)
    )
}