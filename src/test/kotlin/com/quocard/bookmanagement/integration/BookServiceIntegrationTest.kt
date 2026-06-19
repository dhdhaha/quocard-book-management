package com.quocard.bookmanagement.integration

import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.dto.request.CreateAuthorRequest
import com.quocard.bookmanagement.dto.request.CreateBookRequest
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
 * 書籍サービスの統合テスト（実 DB + 実 Repository + 実 Service）
 */
@SpringBootTest
@Transactional
class BookServiceIntegrationTest {

    @Autowired
    private lateinit var authorService: AuthorService

    @Autowired
    private lateinit var bookService: BookService

    @Test
    fun `書籍を登録し複数著者を紐付けできる`() {
        val author1 = authorService.createAuthor(
            CreateAuthorRequest(name = "山田太郎", birthDate = LocalDate.of(1980, 1, 15)),
        )
        val author2 = authorService.createAuthor(
            CreateAuthorRequest(name = "鈴木花子", birthDate = LocalDate.of(1985, 6, 20)),
        )

        val book = bookService.createBook(
            CreateBookRequest(
                title = "Kotlin入門",
                price = 2800,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author1.id, author2.id),
            ),
        )

        assertEquals("Kotlin入門", book.title)
        assertEquals(2800, book.price)
        assertEquals(2, book.authors.size)
    }

    @Test
    fun `存在しない著者IDで書籍登録すると NotFoundException`() {
        assertThrows(NotFoundException::class.java) {
            bookService.createBook(
                CreateBookRequest(
                    title = "失敗する本",
                    price = 1000,
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(999_999L),
                ),
            )
        }
    }

    @Test
    fun `未出版から出版済みに更新できる`() {
        val author = authorService.createAuthor(
            CreateAuthorRequest(name = "山田太郎", birthDate = LocalDate.of(1980, 1, 15)),
        )
        val created = bookService.createBook(
            CreateBookRequest(
                title = "Kotlin入門",
                price = 2800,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author.id),
            ),
        )

        val updated = bookService.updateBook(
            created.id,
            UpdateBookRequest(
                title = "Kotlin入門 改訂版",
                price = 3000,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author.id),
            ),
        )

        assertEquals(PublicationStatus.PUBLISHED, updated.publicationStatus)
        assertEquals(3000, updated.price)
    }

    @Test
    fun `出版済みから未出版に更新すると BusinessRuleViolationException`() {
        val author = authorService.createAuthor(
            CreateAuthorRequest(name = "山田太郎", birthDate = LocalDate.of(1980, 1, 15)),
        )
        val created = bookService.createBook(
            CreateBookRequest(
                title = "Kotlin入門",
                price = 2800,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author.id),
            ),
        )

        assertThrows(BusinessRuleViolationException::class.java) {
            bookService.updateBook(
                created.id,
                UpdateBookRequest(
                    title = "Kotlin入門",
                    price = 2800,
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                    authorIds = listOf(author.id),
                ),
            )
        }
    }

    @Test
    fun `書籍詳細を取得できる`() {
        val author = authorService.createAuthor(
            CreateAuthorRequest(name = "山田太郎", birthDate = LocalDate.of(1980, 1, 15)),
        )
        val created = bookService.createBook(
            CreateBookRequest(
                title = "Kotlin入門",
                price = 2800,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author.id),
            ),
        )

        val found = bookService.getBook(created.id)

        assertEquals(created.id, found.id)
        assertEquals("山田太郎", found.authors.single().name)
    }

    @Test
    fun `1人の著者が複数の書籍を執筆できる`() {
        val author = authorService.createAuthor(
            CreateAuthorRequest(name = "多作作家", birthDate = LocalDate.of(1970, 3, 3)),
        )

        bookService.createBook(
            CreateBookRequest("第一作", 1000, PublicationStatus.PUBLISHED, listOf(author.id)),
        )
        bookService.createBook(
            CreateBookRequest("第二作", 1200, PublicationStatus.UNPUBLISHED, listOf(author.id)),
        )

        val books = authorService.getBooksByAuthorId(author.id)
        assertEquals(2, books.size)
    }
}
