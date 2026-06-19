package com.quocard.bookmanagement.controller

import com.quocard.bookmanagement.dto.request.CreateAuthorRequest
import com.quocard.bookmanagement.dto.request.UpdateAuthorRequest
import com.quocard.bookmanagement.dto.response.AuthorResponse
import com.quocard.bookmanagement.dto.response.BookResponse
import com.quocard.bookmanagement.service.AuthorService
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
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService,
) {

    @PostMapping
    fun createAuthor(@Valid @RequestBody request: CreateAuthorRequest): ResponseEntity<AuthorResponse> {
        val response = authorService.createAuthor(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    fun updateAuthor(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAuthorRequest,
    ): ResponseEntity<AuthorResponse> =
        ResponseEntity.ok(authorService.updateAuthor(id, request))

    @GetMapping("/{id}/books")
    fun getBooksByAuthor(@PathVariable id: Long): ResponseEntity<List<BookResponse>> =
        ResponseEntity.ok(authorService.getBooksByAuthorId(id))
}
