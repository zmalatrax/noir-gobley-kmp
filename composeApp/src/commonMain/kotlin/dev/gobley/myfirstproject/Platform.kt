package dev.gobley.myfirstproject

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform