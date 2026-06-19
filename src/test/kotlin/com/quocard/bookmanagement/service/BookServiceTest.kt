package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.dto.request.CreateBookRequest
import com.quocard.bookmanagement.dto.request.UpdateBookRequest
import com.quocard.bookmanagement.exception.BusinessRuleViolationException
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
class BookServiceTest {

    @Mock
    private lateinit var bookRepository: BookRepository

    @Mock
    private lateinit var authorRepository: AuthorRepository

    @InjectMocks
    private lateinit var bookService: BookService

    @Test
    fun `書籍を登録できる`() {
        val author = AuthorsRecord().apply {
            id = 1L
            name = "山田太郎"
            birthDate = LocalDate.of(1980, 1, 15)
        }
        val book = BooksRecord().apply {
            id = 10L
            title = "Kotlin入門"
            price = 2800
            publicationStatus = PublicationStatus.UNPUBLISHED.name
        }
        val request = CreateBookRequest(
            title = "Kotlin入門",
            price = 2800,
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(1L),
        )

        whenever(authorRepository.findAllByIds(listOf(1L))).thenReturn(listOf(author))
        whenever(bookRepository.insert("Kotlin入門", 2800, "UNPUBLISHED")).thenReturn(10L)
        whenever(bookRepository.findById(10L)).thenReturn(book)
        whenever(bookRepository.findAuthorIdsByBookId(10L)).thenReturn(listOf(1L))
        whenever(authorRepository.findAllByIds(listOf(1L))).thenReturn(listOf(author))

        val response = bookService.createBook(request)

        assertEquals(10L, response.id)
        assertEquals("Kotlin入門", response.title)
        assertEquals(1, response.authors.size)
        verify(bookRepository).linkAuthors(10L, listOf(1L))
    }

    @Test
    fun `存在しない著者IDで書籍を登録すると例外が発生する`() {
        val request = CreateBookRequest(
            title = "Kotlin入門",
            price = 2800,
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(99L),
        )
        whenever(authorRepository.findAllByIds(listOf(99L))).thenReturn(emptyList())

        assertThrows(NotFoundException::class.java) {
            bookService.createBook(request)
        }

        verify(bookRepository, never()).insert(any(), any(), any())
    }

    @Test
    fun `未出版から出版済みに更新できる`() {
        val existing = BooksRecord().apply {
            id = 10L
            title = "Kotlin入門"
            price = 2800
            publicationStatus = PublicationStatus.UNPUBLISHED.name
        }
        val updated = BooksRecord().apply {
            id = 10L
            title = "Kotlin入門 改訂版"
            price = 3000
            publicationStatus = PublicationStatus.PUBLISHED.name
        }
        val author = AuthorsRecord().apply {
            id = 1L
            name = "山田太郎"
            birthDate = LocalDate.of(1980, 1, 15)
        }
        val request = UpdateBookRequest(
            title = "Kotlin入門 改訂版",
            price = 3000,
            publicationStatus = PublicationStatus.PUBLISHED,
            authorIds = listOf(1L),
        )

        whenever(bookRepository.findById(10L)).thenReturn(existing, updated)
        whenever(authorRepository.findAllByIds(listOf(1L))).thenReturn(listOf(author))
        whenever(bookRepository.findAuthorIdsByBookId(10L)).thenReturn(listOf(1L))

        val response = bookService.updateBook(10L, request)

        assertEquals(PublicationStatus.PUBLISHED, response.publicationStatus)
        verify(bookRepository).replaceAuthors(10L, listOf(1L))
    }

    @Test
    fun `出版済みから未出版に更新すると例外が発生する`() {
        val existing = BooksRecord().apply {
            id = 10L
            title = "Kotlin入門"
            price = 2800
            publicationStatus = PublicationStatus.PUBLISHED.name
        }
        val request = UpdateBookRequest(
            title = "Kotlin入門",
            price = 2800,
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(1L),
        )

        whenever(bookRepository.findById(10L)).thenReturn(existing)

        assertThrows(BusinessRuleViolationException::class.java) {
            bookService.updateBook(10L, request)
        }

        verify(bookRepository, never()).update(any(), any(), any(), any())
    }

    @Test
    fun `存在しない書籍を更新すると例外が発生する`() {
        val request = UpdateBookRequest(
            title = "Kotlin入門",
            price = 2800,
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(1L),
        )
        whenever(bookRepository.findById(99L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            bookService.updateBook(99L, request)
        }
    }

    @Test
    fun `書籍詳細を取得できる`() {
        val book = BooksRecord().apply {
            id = 10L
            title = "Kotlin入門"
            price = 2800
            publicationStatus = PublicationStatus.PUBLISHED.name
        }
        val author = AuthorsRecord().apply {
            id = 1L
            name = "山田太郎"
            birthDate = LocalDate.of(1980, 1, 15)
        }

        whenever(bookRepository.findById(10L)).thenReturn(book)
        whenever(bookRepository.findAuthorIdsByBookId(10L)).thenReturn(listOf(1L))
        whenever(authorRepository.findAllByIds(listOf(1L))).thenReturn(listOf(author))

        val response = bookService.getBook(10L)

        assertEquals("Kotlin入門", response.title)
        assertEquals("山田太郎", response.authors[0].name)
    }
}
