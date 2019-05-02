import java.net.Socket
import java.io.PrintStream
import java.io.InputStreamReader
import java.io.BufferedReader
import io.StdIn._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.awt.BorderLayout
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField

object ChatClient extends App {
  
    val window = new JFrame("Scala Message App")
    val textMsg = new JTextField(50)
    val msgBoard = new JTextArea(16, 50)
  
  def init(): Unit = {
    window.setVisible(true)
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    msgBoard.setEditable(false)
    window.getContentPane().add(textMsg, BorderLayout.SOUTH);
    window.getContentPane().add(new JScrollPane(msgBoard), BorderLayout.CENTER);
    window.pack();
    
    textMsg.addActionListener(new ActionListener{
      override def actionPerformed(e: ActionEvent): Unit = {
        out.println(textMsg.getText())
        textMsg.setText("")
      }
    })
  }
  
  init()
  val sock = new Socket("192.168.43.220", 4444)
  val in = new BufferedReader(new InputStreamReader(sock.getInputStream))
  val out = new PrintStream(sock.getOutputStream)
  var stopped = false
  Future {
    textMsg.setEditable(true)
    while (!stopped) {
      val p = in.readLine()
      if(msgBoard.getText().startsWith("What is your name?"))
          msgBoard.setText("")
      else if (p != null) {
        println(p)
        msgBoard.append(p + "\n")
      }
    }
    window.setVisible(false)
    window.dispose()
  }
  var input = ""
  while (input != ":quit") {
    val input = readLine
    out.println(input)
  }
  stopped = true
  sock.close()
}