package de.unixkiwi.betterschool.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber

@Composable
fun AuthScreen(onSuccessfulLogin: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.handleAuthResult(result.data)
            } else {
                Timber.tag("AuthScreen").e("Intent result code: ${result.resultCode}")
                viewModel.onLoginCanceled(result.resultCode)
            }
        }

    AuthScreen(
        uiState = uiState,
        onClick = { viewModel.onLoginClicked { launcher.launch(it) } },
        onClear = { viewModel.onClearClicked() },
        onSuccessfulLogin = onSuccessfulLogin
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AuthScreen(
    uiState: AuthUiState,
    onClick: () -> Unit,
    onClear: () -> Unit,
    onSuccessfulLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Login") }) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                AuthUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is AuthUiState.Success -> onSuccessfulLogin()

                is AuthUiState.Idle -> {
                    LoginButton(onClick)
                }

                is AuthUiState.Canceled -> {
                    Text("Login canceled")
                    LoginButton(onClick, isRetry = true)
                }

                is AuthUiState.Error -> {
                    Text(
                        "Error: " + uiState.error.toString()
                    )
                    LoginButton(onClick, isRetry = true)
                }
            }
        }
    }
}