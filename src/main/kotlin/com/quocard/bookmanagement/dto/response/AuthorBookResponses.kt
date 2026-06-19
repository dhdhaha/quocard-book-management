package com.quocard.bookmanagement.dto.response

import com.quocard.bookmanagement.domain.PublicationStatus
import java.time.LocalDate

data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
)

data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
    val authors: List<AuthorResponse>,
)
