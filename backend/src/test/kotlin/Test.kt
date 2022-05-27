import model.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import routes.auth.Role

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnauthenticatedRoutesTest() {

    @BeforeAll
    fun setupTestingEnvironment() {
        testWithApplicationContext { db ->
            transaction(db) {
                SchemaUtils.drop(MenuElementTable, TablesTable, UsersBillsTable, BillsTable, CoursesTable, DishesTable, UsersTable, inBatch = false)
                SchemaUtils.createMissingTablesAndColumns(UsersTable)
                SchemaUtils.createMissingTablesAndColumns(BillsTable)
                SchemaUtils.createMissingTablesAndColumns(UsersBillsTable)
                SchemaUtils.createMissingTablesAndColumns(TablesTable)
                SchemaUtils.createMissingTablesAndColumns(CoursesTable)
                SchemaUtils.createMissingTablesAndColumns(DishesTable)
                SchemaUtils.createMissingTablesAndColumns(MenuElementTable)
            }
        }

    }

    @Test
    fun testLogin() {
        assumeTrue(createMockUser("test", "test", "test@test.test", Role.CLIENT))
    }
}


