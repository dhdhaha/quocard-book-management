package com.quocard.bookmanagement.repository

import com.quocard.bookmanagement.jooq.tables.Authors.AUTHORS
import com.quocard.bookmanagement.jooq.tables.records.AuthorsRecord
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
