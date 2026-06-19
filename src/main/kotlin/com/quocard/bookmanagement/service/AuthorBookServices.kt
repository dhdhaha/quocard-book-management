package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.dto.request.CreateAuthorRequest
import com.quocard.bookmanagement.dto.request.CreateBookRequest
import com.quocard.bookmanagement.dto.request.UpdateAuthorRequest
import com.quocard.bookmanagement.dto.request.UpdateBookRequest
import com.quocard.bookmanagement.dto.response.AuthorResponse
import com.quocard.bookmanagement.dto.response.BookResponse
import com.quocard.bookmanagement.dto.response.toAuthorResponse
import com.quocard.bookmanagement.dto.response.toBookResponse
import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.exception.BusinessRuleViolationException
import com.quocard.bookmanagement.exception.NotFoundException
import com.quocard.bookmanagement.jooq.tables.records.BooksRecord
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
) {

    @Transactional
    fun createAuthor(request: CreateAuthorRequest): AuthorResponse {
        val id = authorRepository.insert(request.name, request.birthDate)
        return requireNotNull(authorRepository.findById(id)).toAuthorResponse()
    }

    @Transactional
    fun updateAuthor(id: Long, request: UpdateAuthorRequest): AuthorResponse {
        if (!authorRepository.existsById(id)) {
            throw NotFoundException("著者が見つかりません: id=$id")
        }
        authorRepository.update(id, request.name, request.birthDate)
        return requireNotNull(authorRepository.findById(id)).toAuthorResponse()
    }

    @Transactional(readOnly = true)
    fun getBooksByAuthorId(authorId: Long): List<BookResponse> {
        if (!authorRepository.existsById(authorId)) {
            throw NotFoundException("著者が見つかりません: id=$authorId")
        }
        return bookRepository.findBookIdsByAuthorId(authorId)
            .map { bookId -> buildBookResponse(requireNotNull(bookRepository.findById(bookId))) }
    }

    private fun buildBookResponse(book: BooksRecord): BookResponse {
        val authors = authorRepository.findAllByIds(bookRepository.findAuthorIdsByBookId(book.id!!))
            .map { it.toAuthorResponse() }
        return book.toBookResponse(authors)
    }
}

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {

    @Transactional
    fun createBook(request: CreateBookRequest): BookResponse {
        validateAuthorsExist(request.authorIds)
        val bookId = bookRepository.insert(
            title = request.title,
            price = request.price,
            publicationStatus = request.publicationStatus.name,
        )
        bookRepository.linkAuthors(bookId, request.authorIds)
        return buildBookResponse(requireNotNull(bookRepository.findById(bookId)))
    }

    @Transactional
    fun updateBook(id: Long, request: UpdateBookRequest): BookResponse {
        val existing = bookRepository.findById(id)
            ?: throw NotFoundException("書籍が見つかりません: id=$id")

        val currentStatus = PublicationStatus.valueOf(existing.publicationStatus!!)
        if (currentStatus == PublicationStatus.PUBLISHED && request.publicationStatus == PublicationStatus.UNPUBLISHED) {
            throw BusinessRuleViolationException("出版済みの書籍は未出版に変更できません")
        }

        validateAuthorsExist(request.authorIds)
        bookRepository.update(id, request.title, request.price, request.publicationStatus.name)
        bookRepository.replaceAuthors(id, request.authorIds)
        return buildBookResponse(requireNotNull(bookRepository.findById(id)))
    }

    @Transactional(readOnly = true)
    fun getBook(id: Long): BookResponse {
        val book = bookRepository.findById(id)
            ?: throw NotFoundException("書籍が見つかりません: id=$id")
        return buildBookResponse(book)
    }

    private fun validateAuthorsExist(authorIds: List<Long>) {
        val authors = authorRepository.findAllByIds(authorIds)
        if (authors.size != authorIds.toSet().size) {
            val foundIds = authors.mapNotNull { it.id }.toSet()
            val missingIds = authorIds.filterNot { it in foundIds }
            throw NotFoundException("著者が見つかりません: ids=$missingIds")
        }
    }

    private fun buildBookResponse(book: BooksRecord): BookResponse {
        val authors = authorRepository.findAllByIds(bookRepository.findAuthorIdsByBookId(book.id!!))
            .map { it.toAuthorResponse() }
        return book.toBookResponse(authors)
    }
}
