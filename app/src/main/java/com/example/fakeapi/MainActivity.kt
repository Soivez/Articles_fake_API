package com.example.fakeapi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fakeapi.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private var elements : ArrayList<Element> = arrayListOf()

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapt: UserAdapter
    private val elementDao = MyApp.instance.db.elementDao()

    companion object {
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        }
    }

    private fun createRecyclerView() {
        val myRecyclerView: RecyclerView = findViewById(R.id.myRecyclerView)
        val viewManager = LinearLayoutManager(this)
        myRecyclerView.apply {
            layoutManager = viewManager

            adapter = UserAdapter(elements) {
                lifecycle.coroutineScope.launch(Dispatchers.Main) {
                    if (isOnline(context)) {
                        val res =
                            withContext(Dispatchers.IO) {
                                MyApp.instance.service.deleteElement(it.id)
                            }
                        if (res.isSuccessful) {
                            withContext(Dispatchers.IO) {
                                if (elementDao.isExists(it.id)) {
                                    elementDao.deleteAll(it)
                                    withContext(Dispatchers.Main) {
                                        adapt.deleteItem(it)
                                    }
                                }
                            }
                            Toast.makeText(
                                this@MainActivity,
                                "Элемент успешно удален\ncode: " + res.code(),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Не получилось удалить элемент, id = " + it.id.toString() + "\ncode: ${res.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.IO) {
                            if (elementDao.isExists(it.id)) {
                                elementDao.deleteAll(it)
                                withContext(Dispatchers.Main) {
                                    adapt.deleteItem(it)
                                }
                            }
                        }
                        Toast.makeText(
                            this@MainActivity,
                            "Элемент успешно удален, соединение с интернетом отсутствует, запрос на серевер не отправлен",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            adapt = myRecyclerView.adapter as UserAdapter
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val element = result.data!!.getParcelableExtra<Element>("element")!!
            adapt.addElement(element)
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                elementDao.insertALl(element)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Элемент с id " + element.id + " был успешно добавлен\n${result.data!!.getStringExtra("answer")}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                this@MainActivity,
                "Не удалось добавить элемент\n" + result.data!!.getStringExtra("answer"),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.addButton.setOnClickListener {
            val jump = Intent(this, NewPostActivity::class.java)
            jump.putExtra("id", adapt.itemCount + 1)
            startForResult.launch(jump)
        }
        binding.refreshButton.setOnClickListener {
            if (isOnline(this)) {
                binding.progressBar.isVisible = true
                lifecycle.coroutineScope.launch(Dispatchers.IO) {
                    val res = MyApp.instance.service.listElements()
                    val newElements = res.body() as ArrayList<Element>
                    elementDao.nukeTable()
                    elementDao.insertALl(*newElements.toTypedArray())
                    withContext(Dispatchers.Main) {
                        binding.progressBar.isVisible = false
                        adapt.setData(newElements)
                        Toast.makeText(
                            this@MainActivity,
                            "Данные успешно обновлены\ncode: ${res.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Не удалось подключиться к серверу, проверьте соединение",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.progressBar.isVisible = true
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            if (savedInstanceState == null) {
                elements =
                    elementDao.getAll() as ArrayList<Element>
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.isVisible = false
                createRecyclerView()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList("elements", elements)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        elements = savedInstanceState.getParcelableArrayList("elements")!!
    }

}