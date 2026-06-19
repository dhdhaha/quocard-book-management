package com.quocard.bookmanagement.repository

import com.quocard.bookmanagement.jooq.tables.Authors.AUTHORS
import com.quocard.bookmanagement.jooq.tables.BookAuthors.BOOK_AUTHORS
import com.quocard.bookmanagement.jooq.tables.Books.BOOKS
import com.quocard.bookmanagement.jooq.tables.records.AuthorsRecord
import com.quocard.bookmanagement.jooq.tables.records.BooksRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AuthorRepository(
    private val dsl: DSLContext,
) {

    fun insert(name: String, birthDate: LocalDate): Long =
        dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .returningResult(AUTHORS.ID)
            .fetchSingleInto(Long::class.java)

    fun update(id: Long, name: String, birthDate: LocalDate): Boolean =
        dsl.update(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .where(AUTHORS.ID.eq(id))
            .execute() > 0

    fun findById(id: Long): AuthorsRecord? =
        dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()

    fun existsById(id: Long): Boolean =
        dsl.fetchExists(
            dsl.selectOne()
                .from(AUTHORS)
                .where(AUTHORS.ID.eq(id)),
        )

    fun findAllByIds(ids: Collection<Long>): List<AuthorsRecord> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetch()
    }
}

@Repository
class BookRepository(
    private val dsl: DSLContext,
) {

    fun insert(title: String, price: Int, publicationStatus: String): Long =
        dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus)
            .returningResult(BOOKS.ID)
            .fetchSingleInto(Long::class.java)

    fun update(id: Long, title: String, price: Int, publicationStatus: String): Boolean =
        dsl.update(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus)
            .where(BOOKS.ID.eq(id))
            .execute() > 0

    fun findById(id: Long): BooksRecord? =
        dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne()

    fun linkAuthors(bookId: Long, authorIds: List<Long>) {
        authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }
    }

    fun replaceAuthors(bookId: Long, authorIds: List<Long>) {
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()
        linkAuthors(bookId, authorIds)
    }

    fun findAuthorIdsByBookId(bookId: Long): List<Long> =
        dsl.select(BOOK_AUTHORS.AUTHOR_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .fetch(BOOK_AUTHORS.AUTHOR_ID)

    fun findBookIdsByAuthorId(authorId: Long): List<Long> =
        dsl.select(BOOK_AUTHORS.BOOK_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch(BOOK_AUTHORS.BOOK_ID)
}
