package com.example.plugins

import com.example.config.config
import com.example.models.ChatRooms
import com.example.models.ChatRoomPlayers
import com.example.models.Messages
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun Application.configureDatabases() {
    val logger = LoggerFactory.getLogger(this::class.java)

    val databaseName = config.property("database.name").getString()
    val databaseHost = config.property("database.host").getString()
    val databasePort = config.property("database.port").getString()
    val databaseUser = config.property("database.user").getString()
    val databasePassword = config.property("database.password").getString()

    val config = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = "jdbc:postgresql://$databaseHost:$databasePort/$databaseName"
        username = databaseUser
        password = databasePassword
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }
    val dataSource = HikariDataSource(config)
    val database = Database.connect(dataSource)

    // Create tables
    transaction(database) {
        SchemaUtils.create(ChatRooms)
        SchemaUtils.create(ChatRoomPlayers)
        SchemaUtils.create(Messages)
    }

    logger.info("Initialized database connection")
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }