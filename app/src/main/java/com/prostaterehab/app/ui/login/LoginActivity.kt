package com.prostaterehab.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.databinding.ActivityLoginBinding
import com.prostaterehab.app.ui.main.MainActivity
import com.prostaterehab.app.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 检查是否已登录
        val app = application as ProstateRehabApp
        if (app.getCurrentUserId() != -1L) {
            goToMain()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.etUsername.error = getString(com.prostaterehab.app.R.string.please_enter_username)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = getString(com.prostaterehab.app.R.string.please_enter_password)
                return@setOnClickListener
            }

            login(username, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login(username: String, password: String) {
        val app = application as ProstateRehabApp
        lifecycleScope.launch {
            val user = app.userRepository.login(username, password)
            if (user != null) {
                app.setCurrentUserId(user.id)
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    goToMain()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(com.prostaterehab.app.R.string.login_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
