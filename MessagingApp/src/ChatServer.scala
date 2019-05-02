import java.net.ServerSocket
import java.io.PrintStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

object ChatServer extends App {
  case class User(userName: String, socket: Socket, userInput: BufferedReader, userOutput: PrintStream)
  val people = new ConcurrentHashMap[String, User]().asScala
  var name = ""

  Future { createConnection() }
  while (true) {
    for ((userName, person) <- people) {
      message(person)
    }
    Thread.sleep(100)
  }

  def createConnection(): Unit = {
    val server = new ServerSocket(4444)
    while (true) {
      val socket = server.accept()
      val userInput = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val userOutput = new PrintStream(socket.getOutputStream)
      Future {
        userOutput.println("What is your name?")
        var userName = userInput.readLine()
        while(people.contains(userName)){
          userOutput.println("That username is already in use")
          userOutput.println("Please choose another one")
          userOutput.println("What is your name?")
          userName = userInput.readLine()
        }
        val user = User(userName, socket, userInput, userOutput)
        people += userName -> user
        userOutput.println("Welcome " + userName)
        joinDisplay(user)
        //userOutput.println("Please type a message")
      }
    }
  }

  def checkRead(input: BufferedReader): Option[String] = {
    if (input.ready()) Some(input.readLine()) else None
  }

  def joinDisplay(person: User): Unit = {
    for((name, p) <- people){
       p.userOutput.println(person.userName+ " has joined the chat!") 
    }
  }


  
  def message(person: User): Unit = {
    checkRead(person.userInput).foreach { input =>
      if (input == ":quit") {
        person.socket.close()
        for((name, p) <- people){
          p.userOutput.println(person.userName+ " has left the chat!")
        }

        people -= person.userName
      } else {
        for ((n, u) <- people) {
          u.userOutput.println(person.userName + " : " + input)
        }
      }
    }
  }
}
