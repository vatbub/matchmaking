/*-
 * #%L
 * matchmaking.server
 * %%
 * Copyright (C) 2016 - 2019 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.matchmaking.server.logic.roomproviders.database

import java.sql.Connection
import java.sql.Types

data class Schema(val tables: List<Table>) {
    fun verifyIntegrity(connection: Connection): Boolean {
        for (table in tables) {
            if (!hasTable(connection, table))
                return false
            for (column in table.columns) {
                if (!hasColumn(connection, table, column))
                    return false
            }
        }

        return true
    }

    private fun hasTable(connection: Connection, table: Table): Boolean {
        val tableResultSet = connection.metaData.getTables(null, null, "%${table.name}%", null)!!
        return tableResultSet.next()
    }

    private fun hasColumn(connection: Connection, table: Table, column: Column): Boolean {
        val columnResultSet =
            connection.metaData.getColumns(null, null, "%$${table.name}%", "%${column.name}%")!!
        if (!columnResultSet.next())
            return false
        val type = columnResultSet.getInt("DATA_TYPE")
        return columnResultSet.next() && column.type.typesValue == type
    }

    fun create(connection: Connection) {
        for (table in tables) {
            connection.createStatement().executeUpdate("DROP TABLE IF EXISTS ${table.name} CASCADE")
        }

        for (table in tables) {
            connection.createStatement().executeUpdate(table.createTableStatement())
        }
    }

    fun createIfNecessary(connection: Connection) {
        if (!verifyIntegrity(connection))
            create(connection)
    }
}

data class Table(val name: String, val columns: ColumnList) {
    fun createTableStatement(): String {
        return "CREATE TABLE $name (${columns.joinToString(", ")})"
    }

    fun insertInto(connection: Connection, vararg values: Any?) {
        if (values.size != columns.size)
            throw IllegalArgumentException("The list of values must have the same size as this tables column list.")
        val statement =
            connection.prepareStatement("INSERT INTO $name (${columns.joinToString { it.name }}) VALUES (${values.joinToString { "?" }})")

        values.forEachIndexed { index, obj -> statement.setObject(index + 1, obj, columns[index].type.typesValue) }
        statement.execute()
    }
}

data class Column(
        val name: String,
        val type: Type,
        val additionalTypeArgs: List<Any>? = null,
        val constraints: List<Constraint> = listOf()
) {
    override fun toString(): String {
        val resultBuilder = StringBuilder("$name $type")
        if (additionalTypeArgs != null)
            resultBuilder.append(" (${additionalTypeArgs.joinToString(", ")})")
        if (constraints.isNotEmpty())
            resultBuilder.append(" ").append(constraints.joinToString(" "))
        return resultBuilder.toString()
    }
}

abstract class Constraint

class PrimaryKeyConstraint : Constraint() {
    override fun toString(): String {
        return "PRIMARY KEY"
    }
}

class ForeignKeyConstraint(
        private val referencedTable: Table,
        private val referencedColumn: Column,
        private val onDeleteAction: OnModificationAction
) :
    Constraint() {
    init {
        if (!referencedTable.columns.contains(referencedColumn))
            throw IllegalArgumentException("The referenced table must contain the referenced column")
    }

    override fun toString(): String {
        return "REFERENCES ${referencedTable.name} (${referencedColumn.name}) ON DELETE ${onDeleteAction.toString().replace(
            "_",
            " "
        )}"
    }
}

enum class OnModificationAction {
    NO_ACTION, DELETE, CASCADE, RESTRICT, SET_NULL, SET_DEFAULT
}

class ColumnList(contents: Collection<Column>) : ArrayList<Column>(contents) {
    constructor(vararg contents: Column) : this(contents.asList())
    constructor() : this(emptyList())

    operator fun get(name: String): Column? {
        for (column in this) {
            if (column.name == name) return column
        }
        return null
    }
}

enum class Type(val typesValue: Int) {
    BIT(Types.BIT),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    REAL(Types.REAL),
    DOUBLE(Types.DOUBLE),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    CHAR(Types.CHAR),
    VARCHAR(Types.VARCHAR),
    LONGVARCHAR(Types.LONGVARCHAR),
    DATE(Types.DATE),
    TIME(Types.TIME),
    TIMESTAMP(Types.TIMESTAMP),
    BINARY(Types.BINARY),
    VARBINARY(Types.VARBINARY),
    LONGVARBINARY(Types.LONGVARBINARY),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    JAVA_OBJECT(Types.JAVA_OBJECT),
    DISTINCT(Types.DISTINCT),
    STRUCT(Types.STRUCT),
    ARRAY(Types.ARRAY),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    REF(Types.REF),
    DATALINK(Types.DATALINK),
    BOOLEAN(Types.BOOLEAN),
    ROWID(Types.ROWID),
    NCHAR(Types.NCHAR),
    NVARCHAR(Types.NVARCHAR),
    LONGNVARCHAR(Types.LONGNVARCHAR),
    NCLOB(Types.NCLOB),
    SQLXML(Types.SQLXML),
    REF_CURSOR(Types.REF_CURSOR),
    TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE),
    TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE)
}
