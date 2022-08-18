import javafx.application.Application
import javafx.collections.FXCollections
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.{AnchorPane, BorderPane, HBox, VBox}
import javafx.stage.{Modality, Stage}
import javafx.collections.ObservableList
import javafx.scene.text.TextFlow

import java.io.IOException
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster
import akka.cluster.ClusterEvent.*
import akka.event.LoggingReceive
import com.typesafe.config.{Config, ConfigFactory}
import akka.cluster.pubsub.*
import akka.cluster.pubsub.DistributedPubSubMediator.Put

import java.lang.Thread.sleep
import scala.collection.mutable.{HashMap, ArrayBuffer}
import scala.util.Random.alphanumeric



case class send(message: (String, ActorRef, String, String))

case class privateSend(message: (String, ActorRef, String, String))

case class setConnectionList(connList: ObservableList[user])

//case class setChatsHistory(chatsHistory: HashMap[String, ObservableList[HBox]] )
//case class setChatsHistory(chatsHist: HashMap[String, ArrayBuffer[HBox]])
case class setChatsHistory(chatsHist: ObservableList[HashMap[String, ArrayBuffer[HBox]]])

case class myNameIs(name: String, host: String, port: Integer, actorReference: ActorRef)

case class setUserNameActor(uN: String)

case class subscribe(topic: String)

case class unsubscribe(topic: String)



//case class privateMessage(message: (String, ActorRef, String, String))

//case class myNameIs(data: (String, String, Integer, String))

//case class giveMeYouName()
//
//case class hereIsMyName(name: String)


class actor extends Actor with ActorLogging {

  var controller: rootGUIController = _

  var userName: String = ""

  var connectionList: ObservableList[user] = _

  //var chatsHistory: HashMap[String, ObservableList[HBox]] = _
  //var chatsHistory: HashMap[String, ArrayBuffer[HBox]] = _
  var chatsHistory: ObservableList[HashMap[String, ArrayBuffer[HBox]]] = null

  //var chatsHistory = _

  //var history: Map[String -> ObservableList[TextFlow]] = null

  var mediator: ActorRef = _
  var mediatorForConnectionList: ActorRef = _

  var topic: String = "publicChat"

  //val connectionSelf: ObservableList[user] = FXCollections.observableArrayList()


//  var host: String = null
//
//  var port: Integer = null

  //var nameNewUser: String = ""

  //def getConnectionList: ObservableList[user] = connectionList
  println("NO PRESTART")
  val actorCluster = cluster.Cluster(context.system)



  //println(self)

  //println(context.system)
  //actorCluster.join(Address("tcp", "ActorSystem", "127.0.0.1", 2554))

  override def preStart() = {
    println("presStart")
    mediator = DistributedPubSub(context.system).mediator
    mediatorForConnectionList = DistributedPubSub(context.system).mediator

    mediator ! DistributedPubSubMediator.Subscribe("publicChat", self)
    mediatorForConnectionList ! DistributedPubSubMediator.Subscribe("connectionList", self)

    mediator ! Put(self)

    actorCluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember], classOf[MemberUp])
  }

  override def postStop() = {
    actorCluster.unsubscribe(self)
  }

  /**
   * Функция ищет в списке подключений userConnectionList: ObservableList[user] объект user с атрибутом actorReference,
   * который равен параметру actorReference: String. Возвращает true, если найден такой объект user, и false - в противном случае
   * @param userConnectionList список с объектами user
   * @param actorReference значение атрибута объекта user, которое требуется найти в списке userConnectionList
  */
  def searchDuplicate(userConnectionList: ObservableList[user], actorReference: String): Boolean = {
    val flag: Boolean = false
    val iteratorUserConnectionList = userConnectionList.iterator()
    while(iteratorUserConnectionList.hasNext){
      val element: user = iteratorUserConnectionList.next
      if(element.getActorReference.equals(actorReference)) {
        return true
      }
    }
    flag
  }


  def receive = LoggingReceive {
//    case setChatsHistory(chatsHistory: HashMap[String, ObservableList[HBox]]) =>
//      this.chatsHistory = chatsHistory
//    case setChatsHistory(chatsHist: HashMap[String, ArrayBuffer[HBox]]) =>
//      chatsHistory = chatsHist
//      println("it is ok")
//      val selectedChatHistory: ArrayBuffer[HBox] = ArrayBuffer.empty[HBox]
//      chatsHistory = HashMap(usr.getActorReference->selectedChatHistory)
//      println(chatsHistory("publicChat"))
    case setChatsHistory(chatsHist: ObservableList[HashMap[String, ArrayBuffer[HBox]]]) =>
      chatsHistory = chatsHist
      println("it is ok")
      val usr: user = new user("Общий чат", "publicChat", 0000, "publicChat")
      val selectedChatHistory: ArrayBuffer[HBox] = ArrayBuffer.empty[HBox]
      chatsHistory.get(0).update(usr.getActorReference, selectedChatHistory)
      println(chatsHistory.get(0)("publicChat"))
    case subscribe(topic: String) => mediator ! DistributedPubSubMediator.Subscribe(topic, self)
    case unsubscribe(topic: String) => mediator ! DistributedPubSubMediator.Unsubscribe(topic, self)
    case setConnectionList(connList: ObservableList[user]) =>
      connectionList = connList
      val usr: user = new user("Общий чат", "publicChat", 0000, "publicChat")
      connectionList.add(usr)
      //println(chatsHistory("publicChat"))
      //val labels: ObservableList[HBox] = FXCollections.observableArrayList()
    case setUserNameActor(uN: String) => // установка имени пользователя актора. Аббревиатура uN - userName
      userName = uN
    case myNameIs(name: String, host: String, port: Integer, actorReference: ActorRef) =>
      //println(actorReference.toString + " and " + self.toString)
//      println("\n#################################################################################################################\n")
//      println("ActorRefernce"+actorReference)
//      println("Path.address: "+actorReference.path.address)
//      println("Path.name: "+actorReference.path.name)
//      println("Path.root: "+actorReference.path.root.toString)
//      println("Path.parent: "+actorReference.path.parent.name.toString)
      val usr: user = new user(name, host, port, "/"+actorReference.path.parent.name+"/"+actorReference.path.name) //создание объекта класса user с информацией об акторе, который отправил сообщение myNameIs
      
      // проверка неполучения сообщения от самого себя и отсутствие данного объекта класса user (переменная usr выше) в списке подключений connectionList
      if(!(self==actorReference) && !searchDuplicate(connectionList, usr.getActorReference)) {
        connectionList.add(usr) // добавление нового пользователя в список подключений
        //val selectedChatHistory: ObservableList[HBox] = FXCollections.observableArrayList()
        val selectedChatHistory: ArrayBuffer[HBox] = ArrayBuffer.empty[HBox]
        chatsHistory.get(0).update(usr.getActorReference, selectedChatHistory)
        println(chatsHistory.get(0)("publicChat"))
      }
    case s: rootGUIController => controller = s // если актор получает объект типа rootGUIController, то этот объект - контроллер => устанавливаем controller = s
    case s: (String, ActorRef, String, String) => // если актор получает кортеж(нужно уточнить) из трех элементов String, ActorRef, String, то этот кортеж - данные, отправленные актором, на который подписан текущий актор
      val stringLocalActorReference: String = "/"+s._2.path.parent.name+"/"+s._2.path.name // ссылка на актора вида /user/actor#3243 без указания системы акторов, хоста и порта
      //val myStringLocalActorReference: String = "/"+self.path.parent.name+"/"+self.path.name
      if(!(s._2 == self)){
        val controllerSelectedUser: String = controller.getSelectedUser
        if(!controllerSelectedUser.equals(stringLocalActorReference) && !(controllerSelectedUser.equals("publicChat") && s._4.equals("publicChat"))){//!controllerSelectedUser.equals("publicChat") &&
          controller.addLabelIntoChatsHistory(s._1, s._3, stringLocalActorReference)
        }else{
          controller.addLabel(s._1, controller.getMessageHistory, s._3)
        }
        // если получатель и отправитель находятся в одном чате, т.е. либо в общим чате, либо в личном чате друг с другом
//        if((controllerSelectedUser.equals("publicChat") && (s._4.equals("publicChat"))) || (controllerSelectedUser.equals(stringLocalActorReference) && s._4.equals(myStringLocalActorReference))) {
//          controller.addLabel(s._1, controller.getMessageHistory, s._3) // добавляем в чат(HBox) новую метку(Label) с помощью метода класса-контроллера(controller)
//          log.info("Got {}", s)
//          //println(s._1) // выводим сообщение, полученное от актора, на которого подписан текущий актор}
//        }else{
//          controller.addLabelIntoChatsHistory(s._1, s._3, stringLocalActorReference)
//        }
      }else{print(s)}
//    case privateMessage(message: (String, ActorRef, String)) =>
//      val stringLocalActorReference: String = "/"+message._2.path.parent.name+"/"+message._2.path.name // ссылка на актора вида /user/actor#3243 без указания системы акторов, хоста и порта
//      if(controller.getSelectedUser.equals(stringLocalActorReference)) {
//        controller.addLabel(message._1, controller.getMessageHistory, message._3) // добавляем в чат(HBox) новую метку(Label) с помощью метода класса-контроллера(controller)
//      }else{
//        controller.addLabelIntoChatsHistory(stringLocalActorReference)
//      }
    case "test" => println("It is a test") // тест
    case privateSend(message: (String, ActorRef, String, String))=>
      mediator ! DistributedPubSubMediator.Send(message._4, message, localAffinity=false)
    case send(message: (String, ActorRef, String, String))=> // если актор получает объект кейс класса send с единственным аргументом-кортежом(message: (String, ActorRef, String)), то актор рассылает аргумент message всем подписчикам
      mediator ! DistributedPubSubMediator.Publish("publicChat", message)
      //println(DistributedPubSubMediator.CountSubscribers("connectionList"))
    case MemberUp(member) => // добавление нового пользователя, только подключившегося к кластеру, в список подключенных пользователей(connectionList)
      //println("SelfCluster "+actorCluster.selfAddress)
      //println("Member "+member.address)

      if(!(actorCluster.selfAddress == member.address)) {//!(actorCluster.selfAddress == member.address)
        log.info(s"[Listener] node is up: $member")
        println("###################################################################################################################################################################\n")
        println("New node is up!\n")
        println("###################################################################################################################################################################")
        //mediator ! DistributedPubSubMediator.Subscribe(sender().path.toString, self)
        sleep(2000)
        // Какая идея? Чтобы получить имя пользователя нового узла, подключившегося к кластеру
        // требуется отослать подключившемуся актору ? с просьбой отослать свое имя пользователя userName
        // но как это сделать, имея member, я не знаю
        // класс user описывает информацию о новом подключении
        //val newUserName: String = actorCluster.

        //self ! myNameIs((userName, actorCluster.selfAddress.getHost().get, actorCluster.selfAddress.getPort().get, self.toString))
//        host = member.address.getHost().toString
//        port = member.address.getPort().get()


        //РАЗОБРАТЬСЯ ПОЗЖЕ
//        if (!(connectionList.contains(new user((sender().ask(self =>"Твоё имя")(new Timeout(20.second))).toString, member.address.getHost().get, member.address.getPort().get, sender().toString))))
//          connectionList.add(new user((sender().ask(self =>"Твоё имя")(new Timeout(20.second))).toString, member.address.getHost().get, member.address.getPort().get, sender().toString))

        mediatorForConnectionList ! DistributedPubSubMediator.Publish("connectionList", myNameIs(userName, actorCluster.selfAddress.getHost().get, actorCluster.selfAddress.getPort().get, self))

        //connectionList.add(new user(userName, member.address.getHost().toString, member.address.getPort().get())) // добавление нового пользователя в список подключений
        println(member.toString)
      }else{
        //connectionSelf.add(new user("Общая комната", actorCluster.selfAddress.getHost().get(), actorCluster.selfAddress.getPort().get()))
        //connectionList.add(new user("Общая комната", actorCluster.selfAddress.getHost().get(), actorCluster.selfAddress.getPort().get()))
      }
    case UnreachableMember(member) =>
      log.info(s"[Listener] node is unreachable: $member")
      //println(member.toString)
    case MemberRemoved(member, prevStatus) =>
      log.info(s"[Listener] node is removed: $member")
      println("Node"+member.toString+" leave")

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

  private var primaryStage: Stage = _
  private var inputNameStage: Stage = _ // сцена для ввода имени пользователя
  private var rootLayout: BorderPane = _
  private var controller: rootGUIController = _
  private val connectionList: ObservableList[user] = FXCollections.observableArrayList()
  //val chatsHistory: HashMap[String, ObservableList[HBox]] = HashMap[String, ObservableList[HBox]]()
  var chatsHistory: ObservableList[HashMap[String, ArrayBuffer[HBox]]] = FXCollections.observableArrayList()
  chatsHistory.add(HashMap[String, ArrayBuffer[HBox]]())
  var actor1: ActorRef = _

  //connectionList.add(new user("test", "127.0.0.1", 2551))

//  var conf: Config = null
//  var system: ActorSystem = null
//  var actor1: ActorRef = null
  var userName: String = "actor1"

  def getInputNameStage: Stage = inputNameStage
  
  def getPrimaryStage: Stage = primaryStage

  /**
   * Функция отправки актору сообщения, которое нужно отослать всем участникам группового чата
   * @param message кортеж из трех элементов - текста сообщения(из GUI элемента TextField), ссылки на актора отправителя и имени отправителя
   */
  def sendingMessage(message: (String, ActorRef, String, String)): Unit = {
    //val message2 = (message._1, message._2, controller)
    //val vbbox: VBox = controller.getMessageStory
    actor1 ! send(message)
  }

  /**
   * Функция отправки актору сообщения, которое нужно отослать конкретному актору
   * @param message сообщение для отправки. Содержит сообщение, ссылку на отправителя и имя пользователя отправителя
   * @param recipient строковое представление ссылки на актора-получателя (ActorRef.toString)
   */
  def privateSendingMessage(message: (String, ActorRef, String, String)): Unit = {
    actor1 ! privateSend(message)
  }


  def subscribing(): Unit = {
    actor1 ! subscribe("publicChat")
    actor1 ! "test"
  }

  def unsubscribing(): Unit = {
    actor1 ! unsubscribe("publicChat")
    actor1 ! "test"
  }

  def getConnectionList: ObservableList[user] = connectionList // функция возврата массива connectionList



//  val conf = ConfigFactory.load()
//  //ConfigFactory.systemProperties()
//
//
//  val system = ActorSystem("ActorSystem", conf)
//
//  val actor1 = system.actorOf(Props[actor](), userName)
//  println("Актор создан!")
//
//
//  actor1 ! setConnectionList(connectionList)


//  def receiveMessageFromClient(vBox: VBox): Unit ={
//    new Thread(new Runnable(){
//      override def run() = {
//        rootGUIController.addLabel()
//      }
//    })
//  }
  override def start(primaryStage: Stage): Unit = {
    this.primaryStage = primaryStage
  
    initInputAndSetNameUser() // форма ввода имени пользователя и связывание его с переменной userName
    initCluster() // создание кластера, создание системы акторов, создание актора и передача ему имени(userName) и списка подключенных акторов(connectionList)
  
    this.primaryStage.setTitle("Чат. Пользователь: "+userName) // устанавливаем title окна с именем пользователя
  
    initRootLayout() // отображение основного окна чата

    sleep(500)
    println(chatsHistory.get(0).foreach(println)) //"publicChat"

  }

  def initInputAndSetNameUser(): Unit = {
    try{
      val loader: FXMLLoader = new FXMLLoader
      loader.setLocation(classOf[mainChat].getResource("inputName.fxml"))
      val rootLayoutSetNameUser: AnchorPane = loader.load.asInstanceOf[AnchorPane]

      this.inputNameStage = new Stage()
      inputNameStage.setTitle("Введите имя пользователя")
      inputNameStage.initModality(Modality.WINDOW_MODAL)
      inputNameStage.initOwner(primaryStage)

      val scene: Scene = new Scene(rootLayoutSetNameUser)
      inputNameStage.setScene(scene)


      val controller: inputNameController = loader.getController
      controller.setMainChat(this)

      inputNameStage.showAndWait()

    }catch {
      case e: IOException => println(e)
    }

  }

  def initCluster(): Unit = {
    val conf = ConfigFactory.load()
    //ConfigFactory.systemProperties()


    val system: ActorSystem = ActorSystem("ActorSystem", conf)

    actor1 = system.actorOf(Props[actor](), alphanumeric.take(10).mkString(""))
    println("###################################################################################################################################################################\n")
    println("Actor created!\n")
    println("###################################################################################################################################################################\n")

    actor1 ! setUserNameActor(userName)

    actor1 ! setChatsHistory(chatsHistory)

    actor1 ! setConnectionList(connectionList)



    //actor1 ! "Ты подключился"
  }

  def initRootLayout(): Unit = {
    try{
      //println(chatsHistory.get(0).foreach(println)) //"publicChat"
      //println(chatsHistory("publicChat"))
      val loader: FXMLLoader = new FXMLLoader
      loader.setLocation(classOf[mainChat].getResource("rootGUI2.fxml"))
      rootLayout = loader.load.asInstanceOf[BorderPane]

//      val scene: Scene = new Scene(rootLayout)
//      primaryStage.setScene(scene)
//      primaryStage.show

      this.controller = loader.getController
      primaryStage.setOnCloseRequest(controller.closeEventHadler)
      controller.setMainChat(this)
      actor1 ! controller // передаем актору контроллер, чтобы актор мог вызвать метод addLabel и добавить полученное сообщение на экран
      //actor1 ! userName // передаем актору имя пользователя, чтобы актор мог отобразить его на экране
      //actor1 ! myNameIs(userName)
//      actor1 ! userName // передаем актору имя пользователя, чтобы актор мог отобразить его на экране
//      actor1 ! connectionList

      val scene: Scene = new Scene(rootLayout)
      primaryStage.setScene(scene)
      primaryStage.show()

      //controller.addLabel("hello", controller.getMessageStory)
      //controller.setSend(send.)
    }catch{
      case e: Exception => e.printStackTrace()
    }
  }

  //System.exit(0)
}