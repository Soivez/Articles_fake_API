package com.example.fakeapi

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TextElements {
        @GET("/posts/")
        suspend fun listElements() : Response<List<Element>>

        @POST("/posts/")
        suspend fun postElement(@Body body : Element) : Response<Element>

        @DELETE("posts/{id}")
        suspend fun deleteElement(@Path("id") id : Int) : Response<Element>
}