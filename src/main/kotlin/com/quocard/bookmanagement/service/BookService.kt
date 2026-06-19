package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.dto.request.BookRequest
import com.quocard.bookmanagement.dto.response.BookResponse
import com.quocard.bookmanagement.dto.response.buildBookResponse
import com.quocard.bookmanagement.exception.BusinessRuleViolationException
import com.quocard.bookmanagement.exception.NotFoundException
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {

    @Transactional
    fun createBook(request: BookRequest): BookResponse {
        validateAuthorsExist(request.authorIds)
        val bookId = bookRepository.insert(
            title = request.title,
            price = request.price,
            publicationStatus = request.publicationStatus.name,
        )
        bookRepository.linkAuthors(bookId, request.authorIds)
        return buildBookResponse(requireNotNull(bookRepository.findById(bookId)), bookRepository, authorRepository)
    }

    @Transactional
    fun updateBook(id: Long, request: BookRequest): BookResponse {
        val existing = bookRepository.findById(id)
            ?: throw NotFoundException("書籍が見つかりません: id=$id")

        val currentStatus = PublicationStatus.valueOf(existing.publicationStatus!!)
        if (currentStatus == PublicationStatus.PUBLISHED && request.publicationStatus == PublicationStatus.UNPUBLISHED) {
            throw BusinessRuleViolationException("出版済みの書籍は未出版に変更できません")
        }

        validateAuthorsExist(request.authorIds)
        bookRepository.update(id, request.title, request.price, request.publicationStatus.name)
        bookRepository.replaceAuthors(id, request.authorIds)
        return buildBookResponse(requireNotNull(bookRepository.findById(id)), bookRepository, authorRepository)
    }

    @Transactional(readOnly = true)
    fun getBook(id: Long): BookResponse {
        val book = bookRepository.findById(id)
            ?: throw NotFoundException("書籍が見つかりません: id=$id")
        return buildBookResponse(book, bookRepository, authorRepository)
    }

    private fun validateAuthorsExist(authorIds: List<Long>) {
        val authors = authorRepository.findAllByIds(authorIds)
        if (authors.size != authorIds.toSet().size) {
            val foundIds = authors.mapNotNull { it.id }.toSet()
            val missingIds = authorIds.filterNot { it in foundIds }
            throw NotFoundException("著者が見つかりません: ids=$missingIds")
        }
    }
}
