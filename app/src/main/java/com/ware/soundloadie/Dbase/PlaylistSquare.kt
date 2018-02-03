package com.ware.soundloadie.Dbase

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class PlaylistSquare(
        @Id var id: Long = 0,
        var titles:String? = "",
        var author:String? = "",
        var link:String? = "",
        var art:String? = "",
        var idx:String? = "",
        var track:String? = "",
        var mime:String? = ""

)