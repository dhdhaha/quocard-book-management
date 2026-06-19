package com.quocard.bookmanagement.dto.request

import com.quocard.bookmanagement.domain.PublicationStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class AuthorRequest(
    @field:NotBlank val name: String,
    @field:NotNull @field:PastOrPresent val birthDate: LocalDate,
)

data class BookRequest(
    @field:NotBlank val title: String,
    @field:Min(0) val price: Int,
    @field:NotNull val publicationStatus: PublicationStatus,
    @field:NotEmpty val authorIds: List<Long>,
)
