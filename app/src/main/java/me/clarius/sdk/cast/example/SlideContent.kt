package me.clarius.sdk.cast.example

class SlideContent(// New field to represent the slide number
    val slideNumber: Int,
    val title: String,
    val textLeft: String,
    val textRight: String, // Image resource ID for left image
    val imageLeftResId: Int, // Image resource ID for right image
    val imageRightResId: Int
) 