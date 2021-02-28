package com.project.imageapp.api

import com.project.imageapp.data.UnsplashPhoto

data class UnsplashResponse(
    val results: List<UnsplashPhoto>
)