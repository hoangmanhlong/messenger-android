package com.android.kotlin.familymessagingapp.model

import com.android.kotlin.familymessagingapp.R

data class Story(
    val id: String,
    val title: String,
    val photoUrl: Int,
    val author: String,
    val num_comments: Int,
    val story_text: String?
)

val fakeStories = listOf(
    Story(
        id = "1",
        title = "The Adventure Begins",
        photoUrl = R.drawable.baohong, // Replace with actual drawable resource IDs
        author = "John Doe",
        num_comments = 15,
        story_text = "It was a dark and stormy night..."
    ),
    Story(
        id = "2",
        title = "Lost in the Wilderness",
        photoUrl = R.drawable.baohong,
        author = "Jane Smith",
        num_comments = 8,
        story_text = "The sun beat down on the parched desert..."
    ),
    Story(
        id = "3",
        title = "The Secret of the Old Mansion",
        photoUrl = R.drawable.baohong,
        author = "Robert Brown",
        num_comments = 22,
        story_text = null // Example of a story without text
    )
    ,
    Story(
        id = "4",
        title = "Lost in the Wilderness",
        photoUrl = R.drawable.baohong,
        author = "Jane Smith",
        num_comments = 8,
        story_text = "The sun beat down on the parched desert..."
    )
    ,
    Story(
        id = "5",
        title = "Lost in the Wilderness",
        photoUrl = R.drawable.baohong,
        author = "Jane Smith",
        num_comments = 8,
        story_text = "The sun beat down on the parched desert..."
    )
    // Add more fake stories as needed
)