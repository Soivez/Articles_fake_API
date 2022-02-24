package com.example.fakeapi

import android.app.Application
import android.os.Bundle
import androidx.room.Room
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MyApp : Application() {

    lateinit var service : TextElements
    lateinit var db : AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        service = retrofit.create(TextElements::class.java)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "posts-database").build()
    }

    companion object {
        lateinit var instance: MyApp
            private set
    }

}