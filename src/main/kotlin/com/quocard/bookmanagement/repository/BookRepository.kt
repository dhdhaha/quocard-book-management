package com.quocard.bookmanagement.repository

import com.quocard.bookmanagement.jooq.tables.BookAuthors.BOOK_AUTHORS
import com.quocard.bookmanagement.jooq.tables.Books.BOOKS
import com.quocard.bookmanagement.jooq.tables.records.BooksRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

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

    fun findAllByIds(ids: Collection<Long>): List<BooksRecord> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        return dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.`in`(ids))
            .fetch()
    }

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

    fun findAuthorIdsGroupedByBookIds(bookIds: Collection<Long>): Map<Long, List<Long>> {
        if (bookIds.isEmpty()) {
            return emptyMap()
        }
        return dsl.select(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.`in`(bookIds))
            .fetch()
            .groupBy(
                { record -> record.get(BOOK_AUTHORS.BOOK_ID)!! },
                { record -> record.get(BOOK_AUTHORS.AUTHOR_ID)!! },
            )
    }

    fun findBookIdsByAuthorId(authorId: Long): List<Long> =
        dsl.select(BOOK_AUTHORS.BOOK_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch(BOOK_AUTHORS.BOOK_ID)
}
