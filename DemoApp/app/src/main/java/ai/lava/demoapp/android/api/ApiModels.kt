package ai.lava.demoapp.android.api

data class LoginRequest(val email: String, val password: String)

data class RefreshTokenRequest(val email: String)

data class AuthResponse(val memberToken: String)