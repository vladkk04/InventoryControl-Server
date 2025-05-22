package com.server.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.server.config.DatabaseConfig

object DatabaseFactory {
    private val serverApi: ServerApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build()

    private val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(DatabaseConfig.CONNECTION_STRING))
        .serverApi(serverApi)
        .build()

    private val client = MongoClient.create(mongoClientSettings)

    val database = client.getDatabase(DatabaseConfig.DATABASE_NAME)
}