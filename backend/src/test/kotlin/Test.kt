import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import model.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import routes.BillJoinRequest
import routes.KitchenCourse
import routes.SimpleStringResponse
import routes.auth.AuthTokenResponseData
import routes.auth.JavaBase64Encoder
import routes.auth.LoginRequestData
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
        val encoder = JavaBase64Encoder()
        assumeTrue(createMockUser("test", "test", "test@test.test", Role.CLIENT))
        runBlocking {
            assert(runCatching {
                provideClient().post<AuthTokenResponseData>(buildUrl("/login")) {
                    contentType(ContentType.Application.Json)

                    body = LoginRequestData(encoder.encodeString("test"), encoder.encodeString("test"))
                }
            }.isSuccess)
        }
    }

}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticatedTests() {
    lateinit var tokenClient: AuthTokenResponseData
    lateinit var tokenWaiter: AuthTokenResponseData

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
                mockMenu()
                mockTables()
            }
            tokenClient = retrieveAuthToken(Role.CLIENT)
            tokenWaiter = retrieveAuthToken(Role.WAITER)
        }

    }

    @Test
    fun testTableOpen() {
        runBlocking {
            assert(runCatching {
                provideClient(tokenWaiter).get<SimpleStringResponse>(buildUrl("/waiter/openBill")) {
                    contentType(ContentType.Application.Json)
                    parameter("tableNumber", 1)
                    parameter("coveredNumber", 3)
                }
            }.isSuccess)
        }
    }

    @Test
    fun testTableAssociation() {
        val data: BillJoinRequest? = getBillData(tokenWaiter)
        data?.let {
            runBlocking {
                assert(provideClient(tokenClient).post<HttpStatusCode>(buildUrl("/clients/joinTable")) {
                    contentType(ContentType.Application.Json)
                    body = it
                }.isSuccess())
            }
        }
    }

    @Test
    fun testTableClose() {
        val billId = getBillId(tokenWaiter)
        runBlocking {
            assert(provideClient(tokenWaiter).get<HttpStatusCode>(buildUrl("/waiter/closeBill")) {
                parameter("billId", billId)
            }.isSuccess())
        }
    }

    @Test
    fun testKitchenCourses() {
        val billId = getBillId(tokenWaiter)
        runBlocking {
            assert(runCatching {
                provideClient(tokenWaiter).get<List<KitchenCourse>>(buildUrl("/kitchen/openCourses"))
            }.isSuccess)
        }
    }
}





