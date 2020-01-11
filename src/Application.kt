package com.example

import com.example.noteitem.NoteItem
import com.example.noteitem.NoteItemController
import com.example.noteitem.NoteItemDTO
import com.example.user.UserController
import com.example.user.UserDTO
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.mustachejava.DefaultMustacheFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.http.content.*
import io.ktor.auth.*
import io.ktor.sessions.*
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.jackson.jackson
import io.ktor.mustache.Mustache
import io.ktor.mustache.MustacheContent
import io.ktor.util.hex
import org.jetbrains.exposed.sql.Database
import java.util.*
import javax.crypto.Mac
import kotlin.collections.HashMap
import javax.crypto.*
import javax.crypto.spec.*
//fun test() = print("Test!")
//fun test1() = test()
val hexValue = hex("6819b57a326945c1968f45236589")
val hashedKey = SecretKeySpec(hexValue, "HmacSHA1")

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module1(testing: Boolean = false) {
    initDB()
    users = HashMap(userController.getAll().map{ it.username to it.password}.toMap())

    println("All registered users:")
    println(users)

    install(Authentication) {
        form("login") {
            userParamName = "username"
            passwordParamName = "password"
            @Suppress("DEPRECATION")
            challenge = FormAuthChallenge.Redirect(url = {"/"})
            validate { credentials ->
                println(credentials)
//                println(users.keys)
//                println(users.values)
                println(users[credentials.name])
                println(hashPassword(credentials.password))
                if (users.containsKey(credentials.name) && users[credentials.name] == hashPassword(credentials.password)) UserIdPrincipal(credentials.name) else null }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
        //x-www-form-urlencoded
    }

    install(Sessions) {
        cookie<MySession>("SESSION")
    }

    install(FreeMarker){
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }

    install(StatusPages) {
        exception<AuthenticationException> { cause ->
            println("AuthenticationException")
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<AuthorizationException> { cause ->
            println("AuthorizationException")
            call.respond(HttpStatusCode.Forbidden)
        }
    }


    routing {
//        route("/login") {
//            get {
//                call.respond(FreeMarkerContent("login.ftl", null))
//            }
//            post {
//                val post = call.receiveParameters()
//                if (post["username"] != null && post["username"] == post["password"]) {
//                    call.respondRedirect("/", permanent = false)
//                } else {
//                    call.respond(FreeMarkerContent("login.ftl", mapOf("error" to "Invalid login")))
//                }
//            }
//        }

        route("/logout") {
            get {
                call.sessions.clear<MySession>()
                call.respondRedirect("/", permanent = false)
            }
        }
        route("/additem") {
            post {
                val post = call.receiveParameters()
                println(post)
                val noteItemDTO = NoteItemDTO(
                    post["title"].toString(),
                    post["details"].toString(),
                    post["assignedTo"].toString(),
                    post["importance"].toString()
                );
                noteController.insert(noteItemDTO)
                call.respondRedirect("/", permanent = false)
            }
        }
        route("/deleteitem") {
            post {
                val post = call.receiveParameters()
                println("In /deleteitem")
                println(post)
                val uuid = UUID.fromString(post["id"].toString())
                noteController.delete(uuid)
                call.respondRedirect("/", permanent = false)
            }
        }

        route("/login") {
            get {
                call.respond(FreeMarkerContent("login.ftl", null))
            }
            authenticate("login") {
                post {
                    val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                    call.sessions.set("SESSION", MySession(principal.name))
                    call.respondRedirect("/", permanent = false)
                    println("Found!")
                }
            }
        }

        route("/register") {
            get {
                call.respond(FreeMarkerContent("register.ftl", null))
            }
            post {
                //val userDto = call.receive<UserDTO>()
                val post = call.receiveParameters()

                val password = post["password"].toString()
                val passwordHash = hashPassword(password)
                if (post["username"] != null && post["password"] != null && !users.containsKey(post["username"].toString())) {
                    val userDto = UserDTO(
                        post["username"].toString(),
                        passwordHash
                    )
                    userController.insert(userDto)
                    call.respondText("Created!")
                    users = HashMap(userController.getAll().map{ it.username to it.password}.toMap())
                } else {
                    call.respondText("Not created!")
                }
            }
        }

        get("/") {
            val session = call.sessions.get<MySession>()
            if (session != null) {
                notes = noteController.getAll(session.username)
                val vm = NoteItemVM(notes, session.username)
                call.respond(MustacheContent("index.hbs", mapOf("notes" to vm)))
            } else {
                call.respondRedirect("/login", permanent = false)
            }
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }
        get("/users") {
            call.respond(userController.getAll())
        }
        get("/session/increment") {
            val session = call.sessions.get<MySessionInt>() ?: MySessionInt(0)
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }
}
fun initDB() {
    val config = HikariConfig("/static/hikari.properties")
    config.schema = "public"
    val ds = HikariDataSource(config)
    Database.connect(ds)
}

val userController = UserController()
val noteController = NoteItemController()
var users = HashMap<String, String>()

var notes  = listOf<NoteItem>()

data class MySession(val username: String)
data class MySessionInt(val count: Int)

data class IndexData(val items: List<Int>)

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}

fun hashPassword(password: String): String {
    val algorithm = Mac.getInstance("HmacSHA1")
    algorithm.init(hashedKey)
    return hex(algorithm.doFinal(password.toByteArray(Charsets.UTF_8)))
}