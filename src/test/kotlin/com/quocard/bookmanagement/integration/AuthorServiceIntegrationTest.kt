package com.quocard.bookmanagement.integration

import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.dto.request.CreateAuthorRequest
import com.quocard.bookmanagement.dto.request.CreateBookRequest
import com.quocard.bookmanagement.dto.request.UpdateAuthorRequest
import com.quocard.bookmanagement.dto.request.UpdateBookRequest
import com.quocard.bookmanagement.exception.BusinessRuleViolationException
import com.quocard.bookmanagement.exception.NotFoundException
import com.quocard.bookmanagement.service.AuthorService
import com.quocard.bookmanagement.service.BookService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 著者サービスの統合テスト（実 DB + 実 Repository + 実 Service）
 */
@SpringBootTest
@Transactional
class AuthorServiceIntegrationTest {

    @Autowired
    private lateinit var authorService: AuthorService

    @Autowired
    private lateinit var bookService: BookService

    @Test
    fun `著者を登録して更新できる`() {
        val created = authorService.createAuthor(
            CreateAuthorRequest(name = "山田太郎", birthDate = LocalDate.of(1980, 1, 15)),
        )
        assertEquals("山田太郎", created.name)

        val updated = authorService.updateAuthor(
            created.id,
            UpdateAuthorRequest(name = "山田次郎", birthDate = LocalDate.of(1980, 1, 15)),
        )
        assertEquals("山田次郎", updated.name)
    }

    @Test
    fun `存在しない著者を更新すると NotFoundException`() {
        assertThrows(NotFoundException::class.java) {
            authorService.updateAuthor(
                999_999L,
                UpdateAuthorRequest(name = "存在しない", birthDate = LocalDate.of(1990, 1, 1)),
            )
        }
    }

    @Test
    fun `著者に紐づく書籍一覧を取得できる`() {
        val author1 = authorService.createAuthor(
            CreateAuthorRequest(name = "著者A", birthDate = LocalDate.of(1980, 1, 1)),
        )
        val author2 = authorService.createAuthor(
            CreateAuthorRequest(name = "著者B", birthDate = LocalDate.of(1985, 5, 5)),
        )

        bookService.createBook(
            CreateBookRequest(
                title = "共通の本",
                price = 2000,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author1.id, author2.id),
            ),
        )
        bookService.createBook(
            CreateBookRequest(
                title = "著者Aのみの本",
                price = 1500,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author1.id),
            ),
        )

        val books = authorService.getBooksByAuthorId(author1.id)

        assertEquals(2, books.size)
        assertEquals(setOf("共通の本", "著者Aのみの本"), books.map { it.title }.toSet())
    }

    @Test
    fun `存在しない著者の書籍一覧取得で NotFoundException`() {
        assertThrows(NotFoundException::class.java) {
            authorService.getBooksByAuthorId(999_999L)
        }
    }
}
