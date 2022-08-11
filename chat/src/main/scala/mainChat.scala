import javafx.application.Application
import javafx.collections.FXCollections
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.{AnchorPane, BorderPane, VBox}
import javafx.stage.{Modality, Stage}
import javafx.collections.ObservableList

import java.io.IOException
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster
import akka.cluster.ClusterEvent.*
import akka.event.LoggingReceive
import com.typesafe.config.{Config, ConfigFactory}
import akka.cluster.pubsub.*

import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn.readLine
//import rootGUIController.addLabel

case class send(message: (String, ActorRef, String))


class actor extends Actor with ActorLogging {

  var controller: rootGUIController = null

  var userName: String = ""

  val connectionList = new ArrayBuffer[user]() // список подключений

  def getConnectionList: ArrayBuffer[user] = connectionList

  val actorCluster = cluster.Cluster(context.system)

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! DistributedPubSubMediator.Subscribe("publicChat", self)
  println(self)

  //println(context.system)
  //actorCluster.join(Address("tcp", "ActorSystem", "127.0.0.1", 2554))

  override def preStart() = {
    actorCluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember], classOf[MemberUp])
  }

  override def postStop() = {
    actorCluster.unsubscribe(self)
  }

  def receive = LoggingReceive {
    case s: String => userName = s
    case s: rootGUIController => controller = s
    case s: (String, ActorRef, String) =>
      if(!(s._2 == self)){
        controller.addLabel(s._1, controller.getMessageHistory, s._3)
        log.info("Got {}", s)
        println(s._1)
      }else{print(s)}
    case "test" => println("It is a test")
    case send(message: (String, ActorRef, String))=>
      mediator ! DistributedPubSubMediator.Publish("publicChat", message)
    case MemberUp(member) =>
      log.info(s"[Listener] node is up: $member")
      connectionList.append(new user("login", member.address.getHost().toString, member.address.getPort().get())) // добавление нового пользователя в список подключений
      println(member.toString)
    case UnreachableMember(member) =>
      log.info(s"[Listener] node is unreachable: $member")
      println(member.toString)
    case MemberRemoved(member, prevStatus) =>
      log.info(s"[Listener] node is removed: $member")
      println(member.toString)
    case ev: MemberEvent =>
      log.info(s"[Listener] event: $ev")
      println(ev.toString)
    case _ => println("Unknown message")
  }
}


object mainApp{
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[mainChat], args: _*)
  }
}

class mainChat extends Application{

  private var primaryStage: Stage = null
  private var setNameUserStage: Stage = null // сцена для ввода имени пользователя
  private var rootLayout: BorderPane = null
  private var controller: rootGUIController = null

//  var conf: Config = null
//  var system: ActorSystem = null
//  var actor1: ActorRef = null
  var userName: String = "actor1"


  def getSetNameUserStage: Stage = setNameUserStage
  
  def getPrimaryStage: Stage = primaryStage

  def sendingMessage(message: (String, ActorRef, String)): Unit = {
    //val message2 = (message._1, message._2, controller)
    //val vbbox: VBox = controller.getMessageStory
    actor1 ! send(message)
  }

  def initCluster: Unit = {

  }

  val conf = ConfigFactory.load()
  //ConfigFactory.systemProperties()


  val system = ActorSystem("ActorSystem", conf)

  val actor1 = system.actorOf(Props[actor](), userName)


//  def receiveMessageFromClient(vBox: VBox): Unit ={
//    new Thread(new Runnable(){
//      override def run() = {
//        rootGUIController.addLabel()
//      }
//    })
//  }
  override def start(primaryStage: Stage): Unit = {
    this.primaryStage = primaryStage
    initSetNameUser // устанавливаем имя пользователя (переменная userName)
    this.primaryStage.setTitle("Чат. Пользователь: "+userName) // устанавливаем title окна с именем пользователя
//    this.conf = ConfigFactory.load()//new
//    this.system = ActorSystem("ActorSystem", conf)//new
//    this.actor1 = system.actorOf(Props[actor](), userName) //new
    initRootLayout

  }

  def initSetNameUser: Unit = {
    try{
      val loader: FXMLLoader = new FXMLLoader
      loader.setLocation(classOf[mainChat].getResource("inputName.fxml"))
      var rootLayoutSetNameUser: AnchorPane = loader.load.asInstanceOf[AnchorPane]

      this.setNameUserStage = new Stage()
      setNameUserStage.setTitle("Введите имя пользователя на латинице")
      setNameUserStage.initModality(Modality.WINDOW_MODAL)
      setNameUserStage.initOwner(primaryStage)

      val scene: Scene = new Scene(rootLayoutSetNameUser)
      setNameUserStage.setScene(scene)


      val controller: inputNameController = loader.getController
      controller.setMainChat(this)

      setNameUserStage.showAndWait()

    }catch {
      case e: IOException => println(e)
    }

  }
  def initRootLayout: Unit = {
    try{
      println("it is ok")
      val loader: FXMLLoader = new FXMLLoader
      loader.setLocation(classOf[mainChat].getResource("rootGUI2.fxml"))
      rootLayout = loader.load.asInstanceOf[BorderPane]

      val scene: Scene = new Scene(rootLayout)
      primaryStage.setScene(scene)
      primaryStage.show

      this.controller = loader.getController
      primaryStage.setOnCloseRequest(controller.closeEventHadler)
      controller.setMainChat(this)
      actor1 ! controller // передаем актору контроллер, чтобы актор мог вызвать метод addLabel и добавить полученное сообщение на экран
      actor1 ! userName // передаем актору имя пользователя, чтобы актор мог отобразить его на экране
      //controller.addLabel("hello", controller.getMessageStory)
      //controller.setSend(send.)
    }catch{
      case e: Exception => e.printStackTrace
    }
  }

  //System.exit(0)
}