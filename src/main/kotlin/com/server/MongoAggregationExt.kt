package com.server

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Aggregates.*
import com.mongodb.client.model.Projections.computed
import com.mongodb.client.model.Projections.fields
import org.bson.Document
import org.bson.conversions.Bson

fun projectWithIdConversion(vararg fields: Pair<String, Any>): Bson {
    val projections = fields.map { (fieldName, expression) ->
        computed(fieldName, when (expression) {
            is String -> if (expression == "_id")
                mapOf("\$toString" to "\$$expression")
            else "\$$expression"
            else -> expression
        })
    }
    return project(fields(*projections.toTypedArray()))
}
