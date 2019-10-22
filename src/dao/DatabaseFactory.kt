package com.percomp.assistant.core.dao

import com.percomp.assistant.core.domain.Devices
import com.percomp.assistant.core.domain.Registry
import com.percomp.assistant.core.domain.Relation
import com.percomp.assistant.core.domain.Status
import com.percomp.assistant.core.model.User
import com.percomp.assistant.core.util.Credentials
import com.percomp.assistant.core.util.communication.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect(hikari())
        transaction {

            // create the tables
            create(Users)
            create(Devices)
            create(Relation)
            create(Registry)
            create(Status)


            // insert admin account
            val check = (Users).select{
                Users.username eq Credentials.ADMIN_USERNAME.value
            }.map{
                User(it[Users.username])
            }.singleOrNull()
            if(check == null){
                Users.insert {
                    it[Users.username] = Credentials.ADMIN_USERNAME.value
                    it[Users.password] = Credentials.ADMIN_PASSWORD.value
                    it[Users.name] = Credentials.ADMIN_NAME.value
                }
            }
        }
    }

    /**
     * PAY ATTENTION!
     *
     * When connect Ktor to the same container, or server, than the DB, the url to the DB must be like:
     * 'jdbc:postgresql://localhost:EXTERNAL_DB_PORT/NAME_OF_DATABASE'
     *
     * but if the DB is deployed in different container than the KtorProject, for example using Docker-Compose:
     *
     * services:
     *      postgres:
     *          image:  "postgres:VERSION"
     *          ports:
     *              -   "EXTERNAL_DB_PORT:INTERNAL_DB_PORT"
     *          environment:
     *              -   POSTGRES_DB=NAME_OF_DATABASE
     *              -   POSTGRES_USER=username
     *              -   POSTGERS_PASSWORD=username-password
     *      web:
     *          image:  "my-application"
     *          ports:
     *              -   "EXTERNAL_API_PORT:INTERNAL_API_PORT"
     *
     * the URI to the database must be like:
     * 'jdbc:postgresql://NAME_OF_THE_DATABASE_SERVICE:INTERNAL_DB_PORT/NAME_OF_DATABASE'
     *
     */
    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName =  "org.postgresql.Driver"
        config.jdbcUrl = "jdbc:postgresql://" + Credentials.DB_DOMAIN.value + ":" + Credentials.DB_PORT.value + "/" + Credentials.DB_NAME.value
        config.maximumPoolSize = Credentials.DB_MAXPOOLSIZE.value.toInt()
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.username = Credentials.DB_USERNAME.value
        config.password = Credentials.DB_PASSWORD.value
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(
        block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }

}