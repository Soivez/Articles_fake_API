package com.example.fakeapi

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.coroutineScope
import com.example.fakeapi.MainActivity.Companion.isOnline
import com.example.fakeapi.databinding.ActivityNewPostBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class NewPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewPostBinding

    private fun checkInput() : Boolean {
        return binding.userId.text.toString().toIntOrNull() != null && binding.title.text.toString().isNotBlank() && binding.body.text.toString().isNotBlank()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.send.setOnClickListener {
            if (!checkInput()) {
                Toast.makeText(
                    this@NewPostActivity,
                    "Некоректный ввод",
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                    lifecycle.coroutineScope.launch(Dispatchers.Main) {
                        var answer: String = "Неудалось подключиться к серверу, проверьте интернет соединение. Данные записаны локально"
                        var result = Activity.RESULT_OK
                        val element: Element
                        withContext(Dispatchers.IO) {
                            element = Element(
                                binding.userId.text.toString().toInt(),
                                intent.getIntExtra("id", 0),
                                binding.title.text.toString(),
                                binding.body.text.toString()
                            )
                            if (isOnline(this@NewPostActivity)) {
                                val res = MyApp.instance.service.postElement(element)
                                answer = "code: ${res.code()}"

                                if (!res.isSuccessful) {
                                    result = Activity.RESULT_CANCELED
                                }
                            }
                        }
                        val data = Intent()
                        data.putExtra("element", element)
                        data.putExtra("answer", answer)
                        setResult(result, data)
                        finish()
                    }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("userId", binding.userId.text.toString())
        outState.putString("title", binding.title.text.toString())
        outState.putString("body", binding.body.text.toString())
    }

    override fun onRestoreInstanceState(outState: Bundle) {
        super.onRestoreInstanceState(outState)
        binding.userId.setText(outState.getString("userId"))
        binding.title.setText(outState.getString("title"))
        binding.body.setText(outState.getString("body"))
    }
}