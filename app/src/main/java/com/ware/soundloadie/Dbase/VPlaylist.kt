package com.ware.soundloadie.Dbase

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class VPlaylist(
        @Id var id: Long = 0,
        var titles:String? = "",
        var link:String? = "",
        var idx:String? = "",
        var published:Long? = 0
)