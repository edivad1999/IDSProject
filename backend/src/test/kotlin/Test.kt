import di.DIModules
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertEquals
import model.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.ktor.KodeinDISession

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest() {
    val db: Database by instance()

    val di:DI()

    @BeforeAll
    fun setupTestingEnvironment() {
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(UsersTable)
            SchemaUtils.createMissingTablesAndColumns(BillsTable)
            SchemaUtils.createMissingTablesAndColumns(UsersBillsTable)
            SchemaUtils.createMissingTablesAndColumns(TablesTable)
            SchemaUtils.createMissingTablesAndColumns(CoursesTable)
            SchemaUtils.createMissingTablesAndColumns(DishesTable)
            SchemaUtils.createMissingTablesAndColumns(MenuElementTable)
        }
    }

    @Test
    fun testRoot() {
        withTestApplication(Application::managerModule) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello, world!", response.content)
            }
        }
    }
}