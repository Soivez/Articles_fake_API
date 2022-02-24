package com.example.fakeapi

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Element(
    @ColumnInfo(name = "userId") val userId : Int,
    @PrimaryKey val id : Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "body") val body: String
    ) : Parcelable
