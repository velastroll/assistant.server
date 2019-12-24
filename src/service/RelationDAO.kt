package com.percomp.assistant.core.dao

import com.percomp.assistant.core.dao.DatabaseFactory.dbQuery
import com.percomp.assistant.core.model.Relations
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class RelationDAO {


    suspend fun getCurrentByUser(user: String) : com.percomp.assistant.core.model.Relation? = dbQuery {
        Relations.select({Relations.user eq user and (Relations.to.isNull())})
            .map {
                com.percomp.assistant.core.model.Relation(
                    device = it[Relations.device],
                    from = it[Relations.from]
                    )
            }.singleOrNull()
    }

}