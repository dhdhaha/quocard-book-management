package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.dto.request.AuthorRequest
import com.quocard.bookmanagement.dto.response.AuthorResponse
import com.quocard.bookmanagement.dto.response.BookResponse
import com.quocard.bookmanagement.dto.response.buildBookResponses
import com.quocard.bookmanagement.dto.response.toAuthorResponse
import com.quocard.bookmanagement.exception.NotFoundException
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
    fun createAuthor(request: AuthorRequest): AuthorResponse {
        val id = authorRepository.insert(request.name, request.birthDate)
        return requireNotNull(authorRepository.findById(id)).toAuthorResponse()
    }

    @Transactional
    fun updateAuthor(id: Long, request: AuthorRequest): AuthorResponse {
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
        val bookIds = bookRepository.findBookIdsByAuthorId(authorId)
        if (bookIds.isEmpty()) {
            return emptyList()
        }
        val books = bookRepository.findAllByIds(bookIds)
        return buildBookResponses(books, bookRepository, authorRepository)
    }
}
