package com.quocard.bookmanagement.dto.response

import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.jooq.tables.records.AuthorsRecord
import com.quocard.bookmanagement.jooq.tables.records.BooksRecord
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository

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

fun buildBookResponse(
    book: BooksRecord,
    bookRepository: BookRepository,
    authorRepository: AuthorRepository,
): BookResponse {
    val authorIds = bookRepository.findAuthorIdsByBookId(book.id!!)
    val authors = authorRepository.findAllByIds(authorIds).map { it.toAuthorResponse() }
    return book.toBookResponse(authors)
}

fun buildBookResponses(
    books: List<BooksRecord>,
    bookRepository: BookRepository,
    authorRepository: AuthorRepository,
): List<BookResponse> {
    if (books.isEmpty()) {
        return emptyList()
    }
    val bookIds = books.mapNotNull { it.id }
    val authorIdsByBookId = bookRepository.findAuthorIdsGroupedByBookIds(bookIds)
    val allAuthorIds = authorIdsByBookId.values.flatten().toSet()
    val authorsById = authorRepository.findAllByIds(allAuthorIds).associateBy { it.id!! }

    return books.map { book ->
        val authors = authorIdsByBookId[book.id!!].orEmpty()
            .mapNotNull { authorId -> authorsById[authorId]?.toAuthorResponse() }
        book.toBookResponse(authors)
    }
}
