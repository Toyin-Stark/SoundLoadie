package com.ware.soundloadie.Dbase

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Playlist(
        @Id var id: Long = 0,
        var player:String? = "",
        var titles:String? = "",
        var genre:String? = "",
        var author:String? = "",
        var art:String? = "",
        var link:String? = ""
)