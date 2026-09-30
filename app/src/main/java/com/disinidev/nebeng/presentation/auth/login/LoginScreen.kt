package com.disinidev.nebeng.presentation.auth.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disinidev.nebeng.core.auth.GoogleSignInHelper
import com.disinidev.nebeng.core.component.NebengButton
import com.disinidev.nebeng.core.component.NebengButtonStyle
import com.disinidev.nebeng.core.component.NebengTextField
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToHome()
        }
    }

    BackHandler {
        (context as? Activity)?.finish()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NebengColor.Primary0)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo Header: Black box with "N" + "Nebeng"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NebengColor.Primary900),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Nebeng",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Screen Title & Subtitle
            Text(
                text = "Masuk ke akun Anda",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Gray800
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Hemat ongkos komuter harian bersama komunitas.",
                fontSize = 14.sp,
                color = NebengColor.Gray600
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Email or Phone Field
            NebengTextField(
                value = uiState.identifier,
                onValueChange = viewModel::onIdentifierChange,
                label = "Email atau Nomor Ponsel",
                placeholder = "Masukkan email atau nomor ponsel",
                leadingIcon = Icons.Default.MailOutline
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Password Field
            NebengTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Kata Sandi",
                placeholder = "Masukkan kata sandi",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password Link
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Lupa kata sandi?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebengColor.Gray800,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Handle forgot password */ }
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    fontSize = 12.sp,
                    color = NebengColor.Danger600
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CTA Primary Button
            NebengButton(
                text = "Lanjutkan",
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = viewModel::login,
                enabled = !uiState.isLoading && !uiState.isGoogleLoading,
                isLoading = uiState.isLoading,
                style = NebengButtonStyle.PRIMARY
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divider "atau"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = NebengColor.Gray200)
                Text(
                    text = "atau",
                    fontSize = 12.sp,
                    color = NebengColor.Gray400,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = NebengColor.Gray200)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign In Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            viewModel.setGoogleLoading(true)
                            val idToken = GoogleSignInHelper.getGoogleIdToken(context)
                            if (idToken != null) {
                                viewModel.signInWithGoogle(idToken)
                            } else {
                                viewModel.onGoogleSignInError("Gagal mendapatkan token Google")
                            }
                        } catch (_: GetCredentialCancellationException) {
                            viewModel.setGoogleLoading(false)
                        } catch (e: Exception) {
                            viewModel.onGoogleSignInError(e.localizedMessage ?: "Gagal terhubung dengan akun Google")
                        }
                    }
                },
                enabled = !uiState.isLoading && !uiState.isGoogleLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.5.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg)),
                shape = RoundedCornerShape(NebengRadius.Lg),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NebengColor.Primary0,
                    contentColor = NebengColor.Primary900
                ),
                elevation = null
            ) {
                if (uiState.isGoogleLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NebengColor.Primary900,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Menghubungkan...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Lanjutkan dengan Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NebengColor.Primary900
                        )
                    }
                }
            }
        }

        // Bottom Footer: Belum memiliki akun? Daftar sekarang
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Belum memiliki akun? ",
                fontSize = 13.sp,
                color = NebengColor.Gray600
            )
            Text(
                text = "Daftar sekarang",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Primary900,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNavigateToRegister
                )
            )
        }
    }
}
