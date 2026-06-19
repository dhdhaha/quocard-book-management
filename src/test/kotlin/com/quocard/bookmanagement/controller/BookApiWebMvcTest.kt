package com.quocard.bookmanagement.controller

import com.quocard.bookmanagement.domain.PublicationStatus
import com.quocard.bookmanagement.dto.request.BookRequest
import com.quocard.bookmanagement.exception.BusinessRuleViolationException
import com.quocard.bookmanagement.exception.GlobalExceptionHandler
import com.quocard.bookmanagement.service.BookService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(BookController::class)
@Import(GlobalExceptionHandler::class)
class BookApiWebMvcTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var bookService: BookService

    @Test
    fun `負の価格で 400`() {
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"title":"テスト","price":-1,"publicationStatus":"UNPUBLISHED","authorIds":[1]}""",
                ),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `出版済みから未出版への更新で 400`() {
        whenever(bookService.updateBook(eq(1L), any<BookRequest>()))
            .thenThrow(BusinessRuleViolationException("出版済みの書籍は未出版に変更できません"))

        mockMvc.perform(
            put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"title":"テスト","price":1000,"publicationStatus":"UNPUBLISHED","authorIds":[1]}""",
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("出版済みの書籍は未出版に変更できません"))
    }
}
