package com.prostaterehab.app.ui.register

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.prostaterehab.app.ProstateRehabApp
import com.prostaterehab.app.databinding.ActivityRegisterBinding
import com.prostaterehab.app.reminder.ReminderManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.prostaterehab.app.R.string.register_title)

        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val nickname = binding.etNickname.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.etUsername.error = getString(com.prostaterehab.app.R.string.please_enter_username)
                return@setOnClickListener
            }
            if (nickname.isEmpty()) {
                binding.etNickname.error = getString(com.prostaterehab.app.R.string.please_enter_nickname)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = getString(com.prostaterehab.app.R.string.please_enter_password)
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                binding.etConfirmPassword.error = getString(com.prostaterehab.app.R.string.password_not_match)
                return@setOnClickListener
            }

            register(username, password, nickname)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun register(username: String, password: String, nickname: String) {
        val app = application as ProstateRehabApp
        lifecycleScope.launch {
            val user = app.userRepository.register(username, password, nickname)
            if (user != null) {
                // 设置随访提醒
                ReminderManager.setAllReminders(this@RegisterActivity, user.registerDate)
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterActivity,
                        "注册成功！您的编号：${user.patientNumber}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(com.prostaterehab.app.R.string.username_exists),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
