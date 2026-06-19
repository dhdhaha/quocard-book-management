package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.dto.request.CreateAuthorRequest
import com.quocard.bookmanagement.dto.request.CreateBookRequest
import com.quocard.bookmanagement.dto.request.UpdateAuthorRequest
import com.quocard.bookmanagement.dto.request.UpdateBookRequest
import com.quocard.bookmanagement.exception.NotFoundException
import com.quocard.bookmanagement.jooq.tables.records.AuthorsRecord
import com.quocard.bookmanagement.jooq.tables.records.BooksRecord
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AuthorServiceTest {

    @Mock
    private lateinit var authorRepository: AuthorRepository

    @Mock
    private lateinit var bookRepository: BookRepository

    @InjectMocks
    private lateinit var authorService: AuthorService

    @Test
    fun `著者を登録できる`() {
        val request = CreateAuthorRequest(name = "山田太郎", birthDate = LocalDate.of(1980, 1, 15))
        val record = AuthorsRecord().apply {
            id = 1L
            name = request.name
            birthDate = request.birthDate
        }

        whenever(authorRepository.insert(request.name, request.birthDate)).thenReturn(1L)
        whenever(authorRepository.findById(1L)).thenReturn(record)

        val response = authorService.createAuthor(request)

        assertEquals(1L, response.id)
        assertEquals("山田太郎", response.name)
        assertEquals(LocalDate.of(1980, 1, 15), response.birthDate)
    }

    @Test
    fun `存在しない著者を更新すると例外が発生する`() {
        val request = UpdateAuthorRequest(name = "山田太郎", birthDate = LocalDate.of(1980, 1, 15))
        whenever(authorRepository.existsById(99L)).thenReturn(false)

        assertThrows(NotFoundException::class.java) {
            authorService.updateAuthor(99L, request)
        }

        verify(authorRepository, never()).update(any(), any(), any())
    }

    @Test
    fun `著者の書籍一覧を取得できる`() {
        val author = AuthorsRecord().apply {
            id = 1L
            name = "山田太郎"
            birthDate = LocalDate.of(1980, 1, 15)
        }
        val book = BooksRecord().apply {
            id = 10L
            title = "Kotlin入門"
            price = 2800
            publicationStatus = PublicationStatus.PUBLISHED.name
        }

        whenever(authorRepository.existsById(1L)).thenReturn(true)
        whenever(bookRepository.findBookIdsByAuthorId(1L)).thenReturn(listOf(10L))
        whenever(bookRepository.findById(10L)).thenReturn(book)
        whenever(bookRepository.findAuthorIdsByBookId(10L)).thenReturn(listOf(1L))
        whenever(authorRepository.findAllByIds(listOf(1L))).thenReturn(listOf(author))

        val responses = authorService.getBooksByAuthorId(1L)

        assertEquals(1, responses.size)
        assertEquals("Kotlin入門", responses[0].title)
        assertEquals(1, responses[0].authors.size)
    }

    @Test
    fun `存在しない著者の書籍一覧を取得すると例外が発生する`() {
        whenever(authorRepository.existsById(99L)).thenReturn(false)

        assertThrows(NotFoundException::class.java) {
            authorService.getBooksByAuthorId(99L)
        }
    }
}
