package com.server.config

object DatabaseConfig {
    val DATABASE_NAME: String = System.getenv("DATABASE_NAME")
    val CONNECTION_STRING =
        "mongodb+srv://vladkldeveloper:${System.getenv("MONGO_PASSWORD")}@cluster0.9mnfn.mongodb.net/${DATABASE_NAME}?retryWrites=true&w=majority&appName=Cluster0"
}