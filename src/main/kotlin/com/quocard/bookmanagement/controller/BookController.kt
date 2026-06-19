package com.quocard.bookmanagement.controller

import com.quocard.bookmanagement.dto.request.CreateBookRequest
import com.quocard.bookmanagement.dto.request.UpdateBookRequest
import com.quocard.bookmanagement.dto.response.BookResponse
import com.quocard.bookmanagement.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
) {

    @PostMapping
    fun createBook(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<BookResponse> {
        val response = bookService.createBook(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBookRequest,
    ): ResponseEntity<BookResponse> =
        ResponseEntity.ok(bookService.updateBook(id, request))

    @GetMapping("/{id}")
    fun getBook(@PathVariable id: Long): ResponseEntity<BookResponse> =
        ResponseEntity.ok(bookService.getBook(id))
}
