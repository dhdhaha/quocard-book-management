package com.quocard.bookmanagement.dto.response

import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.jooq.tables.records.AuthorsRecord
import com.quocard.bookmanagement.jooq.tables.records.BooksRecord

fun AuthorsRecord.toAuthorResponse(): AuthorResponse =
    AuthorResponse(
        id = id!!,
        name = name!!,
        birthDate = birthDate!!,
    )

fun BooksRecord.toBookResponse(
    authors: List<AuthorResponse>,
): BookResponse =
    BookResponse(
        id = id!!,
        title = title!!,
        price = price!!,
        publicationStatus = PublicationStatus.valueOf(publicationStatus!!),
        authors = authors,
    )
