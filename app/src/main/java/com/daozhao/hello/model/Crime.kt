package com.daozhao.hello.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Crime(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = id.toString(),
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var requiresPolice: Boolean = false,
                 var suspect: String = ""
)