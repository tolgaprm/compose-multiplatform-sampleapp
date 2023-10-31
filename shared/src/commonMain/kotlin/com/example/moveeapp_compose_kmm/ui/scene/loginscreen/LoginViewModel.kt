package com.example.moveeapp_compose_kmm.ui.scene.loginscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.moveeapp_compose_kmm.core.viewModelScope
import com.example.moveeapp_compose_kmm.domain.account.SessionSettings
import com.example.moveeapp_compose_kmm.core.ViewModel
import com.example.moveeapp_compose_kmm.data.repository.LoginRepository
import com.example.moveeapp_compose_kmm.data.repository.LoginState
import com.example.moveeapp_compose_kmm.domain.account.GetAccountDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val sessionSettings: SessionSettings,
    private val getAccountDetailUseCase: GetAccountDetailUseCase
) : ViewModel {

    private val _isLoggedIn = MutableStateFlow(loginRepository.getLoginState())
    val isLoggedIn: StateFlow<LoginState>
        get() = _isLoggedIn
    var loginUiState by mutableStateOf(LoginUiState())
        private set

    fun onUserNameChange(username: String) {
        loginUiState = loginUiState.copy(userName = username, loginError = null)
    }

    fun onPasswordChange(password: String) {
        loginUiState = loginUiState.copy(password = password, loginError = null)
    }

    private fun validateLoginForm() =
        loginUiState.userName.isNotBlank() && loginUiState.password.isNotBlank()
    //todo username ve passworda gore farklı farklı mesaj eklenebilir


    fun login() {
        viewModelScope.launch {
            if (!validateLoginForm()) {
                loginUiState = loginUiState.copy(loginError = "Email and password can not be empty")
                return@launch
            }
            loginUiState = loginUiState.copy(isLoading = true, loginError = null)
            loginRepository.login(
                loginUiState.userName,
                loginUiState.password
            )
                .onSuccess {
                    getAccountDetail()
                }
                .onFailure { e ->
                    loginUiState = loginUiState.copy(isLoading = false, loginError = e.message)
                    e.printStackTrace()
                }
        }
    }

    private fun getAccountDetail() {
        viewModelScope.launch {
            val result = getAccountDetailUseCase.execute()
            if (result.isSuccess) {
                sessionSettings.setAccountId(result.getOrNull()?.id ?: 0)
                loginUiState = loginUiState.copy(isLoading = false, isSuccessLogin = true)
                _isLoggedIn.value = LoginState.LOGGED_IN
            }
        }
    }
}
