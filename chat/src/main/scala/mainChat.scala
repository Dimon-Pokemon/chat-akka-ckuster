import javafx.application.{Application, Platform}
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
import javafx.scene.image.Image

import java.lang.Thread.sleep
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.util.Random.alphanumeric



case class send(message: (String, ActorRef, String, String, String))

case class publicMessage(message: (String, ActorRef, String, String, String))

case class privateSend(message: (String, ActorRef, String, String, String))

case class privateMessage(message: (String, ActorRef, String, String, String))

case class setConnectionList(connList: ObservableList[user])

//case class setChatsHistory(chatsHistory: HashMap[String, ObservableList[HBox]] )
case class setChatsHistory(chatsHist: HashMap[String, ArrayBuffer[HBox]])
//case class setChatsHistory(chatsHist: ObservableList[HashMap[String, ArrayBuffer[HBox]]])

case class myNameIs(name: String, host: String, port: Integer, actorReference: ActorRef)

case class setUserNameActor(uN: String)



class actor extends Actor with ActorLogging {

  var controller: rootGUIController = _

  var userName: String = ""

  var connectionList: ObservableList[user] = _

  //var chatsHistory: HashMap[String, ObservableList[HBox]] = _
  var chatsHistory: HashMap[String, ArrayBuffer[HBox]] = _
  //var chatsHistory: ObservableList[HashMap[String, ArrayBuffer[HBox]]] = null

  //var history: Map[String -> ObservableList[TextFlow]] = null

  var mediator: ActorRef = _
  var mediatorForConnectionList: ActorRef = _

  var topic: String = "publicChat"

  val actorCluster = cluster.Cluster(context.system)



  override def preStart() = {
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
   * @return index: Integer
  */
  def searchDuplicate(userConnectionList: ObservableList[user], actorReference: String): Integer = {
    val iteratorUserConnectionList = userConnectionList.iterator()
    var index: Integer = 0
    while(iteratorUserConnectionList.hasNext){
      val element: user = iteratorUserConnectionList.next
      if(element.getActorReference.equals(actorReference)) {
        return index // без return выдает ошибку
      }
      index+=1
    }
    0
  }

  /**
   * Функция ищет в списке подключений userConnectionList: ObservableList[user] объект user с адресом, который получается
   * из значения поля ip и port и равный параметру функции addressActorWhoExit.
   * Возвращает true, если найден такой объект user, и false - в противном случае
   * @param userConnectionList список с объектами user
   * @param addressActorWhoExit значение адреса, который требуется найти у объекта user списка userConnectionList
   * @return index: Integer
   */
  def searchDuplicateAddress(userConnectionList: ObservableList[user], addressActorWhoExit: String): (Integer, String) = {
    val iteratorUserConnectionList = userConnectionList.iterator()
    var index: Integer = 0
    while(iteratorUserConnectionList.hasNext){
      val element: user = iteratorUserConnectionList.next
      val addressUser: String = element.getIp + element.getPort.toString
      if(addressUser.equals(addressActorWhoExit)) {
        return (index, element.getActorReference) // без return выдает ошибку
      }
      index+=1
    }
    (0, "0")
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
    case setChatsHistory(chatsHist: HashMap[String, ArrayBuffer[HBox]]) => //chatsHist: ObservableList[HashMap[String, ArrayBuffer[HBox]]]
      chatsHistory = chatsHist
      val usr: user = new user("Общий чат", "publicChat", 0000, "publicChat")
      val selectedChatHistory: ArrayBuffer[HBox] = ArrayBuffer.empty[HBox] // пустой массив контейнеров HBox.
      chatsHistory.update(usr.getActorReference, selectedChatHistory) // создание новой пары ключ(ссылка на актора)-значение(массив selectedChatHistory)
    case setConnectionList(connList: ObservableList[user]) =>
      connectionList = connList
      val usr: user = new user("Общий чат", "publicChat", 0000, "publicChat")
      connectionList.add(usr)
      //println(chatsHistory("publicChat"))
      //val labels: ObservableList[HBox] = FXCollections.observableArrayList()
    case setUserNameActor(uN: String) => // установка имени пользователя актора. Аббревиатура uN - userName
      userName = uN
    case myNameIs(name: String, host: String, port: Integer, actorReference: ActorRef) =>
      val usr: user = new user(name, host, port, "/"+actorReference.path.parent.name+"/"+actorReference.path.name) //создание объекта класса user с информацией об акторе, который отправил сообщение myNameIs
      
      // проверка неполучения сообщения от самого себя и отсутствие данного объекта класса user (переменная usr выше) в списке подключений connectionList
      if(!(self==actorReference) && !(searchDuplicate(connectionList, usr.getActorReference)>0)) { // !searchDuplicate(connectionList, usr.getActorReference)
        connectionList.add(usr) // добавление нового пользователя в список подключений
        //val selectedChatHistory: ObservableList[HBox] = FXCollections.observableArrayList()
        val selectedChatHistory: ArrayBuffer[HBox] = ArrayBuffer.empty[HBox]
        chatsHistory.update(usr.getActorReference, selectedChatHistory)
        println(chatsHistory("publicChat"))
      }
    case s: rootGUIController => controller = s // если актор получает объект типа rootGUIController, то этот объект - контроллер => устанавливаем controller = s
    case publicMessage(message: (String, ActorRef, String, String, String)) => // если актор получает кортеж(нужно уточнить) из трех элементов String, ActorRef, String, String, то этот кортеж - данные, отправленные актором, на который подписан текущий актор
      // s._4 - контакт, выбранный отправителем
      // s._3 - имя пользователя, который прислал сообщение
      val stringLocalActorReference: String = "/"+message._2.path.parent.name+"/"+message._2.path.name // ссылка на актора вида /user/actor#3243 без указания системы акторов, хоста и порта
      //val myStringLocalActorReference: String = "/"+self.path.parent.name+"/"+self.path.name
      if(!(message._2 == self)){
        val controllerSelectedUser: String = controller.getSelectedUser // получаем из класса-контроллера значение атрибута selectedUser(ссылка на актора выбранного пользователя из списка чатов(контактов))
        if(controllerSelectedUser.equals("publicChat")){
          controller.addLabel(message._1, controller.getMessageHistory, message._3, message._5)
        }else if(message._4.equals("publicChat")){
          controller.addLabelIntoChatsHistory(message._1, message._3, "publicChat", message._5)
        }else{
          controller.addLabelIntoChatsHistory(message._1, message._3, stringLocalActorReference, message._5)
        }
      }
    case privateMessage(message: (String, ActorRef, String, String, String)) =>
      val stringLocalActorReference: String = "/"+message._2.path.parent.name+"/"+message._2.path.name // ссылка на актора вида /user/actor#3243 без указания системы акторов, хоста и порта
      if(!(message._2 == self)){
        val controllerSelectedUser: String = controller.getSelectedUser // получаем из класса-контроллера значение атрибута selectedUser(ссылка на актора выбранного пользователя из списка чатов(контактов))
        if(controllerSelectedUser.equals(stringLocalActorReference)){
          controller.addLabel(message._1, controller.getMessageHistory, message._3, message._5)
        }else{
          controller.addLabelIntoChatsHistory(message._1, message._3, stringLocalActorReference, message._5)
        }
          //          controller.addLabel(message._1, controller.getMessageHistory, message._3)
//        if(controllerSelectedUser.equals("publicChat")){
//          controller.addLabelIntoChatsHistory(message._1, message._3, stringLocalActorReference)
//        }else if(controllerSelectedUser.equals(stringLocalActorReference)){
//          controller.addLabel(message._1, controller.getMessageHistory, message._3)
//        }else{
//          controller.addLabelIntoChatsHistory(message._1, message._3, stringLocalActorReference)
//        }
      }
//    case privateMessage(message: (String, ActorRef, String)) =>
//      val stringLocalActorReference: String = "/"+message._2.path.parent.name+"/"+message._2.path.name // ссылка на актора вида /user/actor#3243 без указания системы акторов, хоста и порта
//      if(controller.getSelectedUser.equals(stringLocalActorReference)) {
//        controller.addLabel(message._1, controller.getMessageHistory, message._3) // добавляем в чат(HBox) новую метку(Label) с помощью метода класса-контроллера(controller)
//      }else{
//        controller.addLabelIntoChatsHistory(stringLocalActorReference)
//      }
    case "test" => println("It is a test") // тест
    case privateSend(message: (String, ActorRef, String, String, String))=>
      mediator ! DistributedPubSubMediator.Send(message._4, privateMessage(message), localAffinity=false)
    case send(message: (String, ActorRef, String, String, String))=> // если актор получает объект кейс класса send с единственным аргументом-кортежом(message: (String, ActorRef, String)), то актор рассылает аргумент message всем подписчикам
      mediator ! DistributedPubSubMediator.Publish("publicChat", publicMessage(message))
      //println(DistributedPubSubMediator.CountSubscribers("connectionList"))
    case MemberUp(member) => // добавление нового пользователя, только подключившегося к кластеру, в список подключенных пользователей(connectionList)
      //println("SelfCluster "+actorCluster.selfAddress)
      //println("Member "+member.address)

      if(!(actorCluster.selfAddress == member.address)) {//!(actorCluster.selfAddress == member.address)
        log.info(s"[Listener] node is up: $member")
        println("###################################################################################################################################################################\n")
        println("New node is up!\n")
        println("###################################################################################################################################################################")
        sleep(2000) // КОСТЫЛЬ. ОГРОМНЫЙ КОСТЫЛЬ

        mediatorForConnectionList ! DistributedPubSubMediator.Publish("connectionList", myNameIs(userName, actorCluster.selfAddress.getHost().get, actorCluster.selfAddress.getPort().get, self))

        //connectionList.add(new user(userName, member.address.getHost().toString, member.address.getPort().get())) // добавление нового пользователя в список подключений
        println(member.toString)
      }
    case UnreachableMember(member) =>
      log.info(s"[Listener] node is unreachable: $member")
    case MemberRemoved(member, prevStatus) => // действия при отключении одного из узлов кластера
      val addressActorWhoExit = member.address.getHost().get()+member.address.getPort().get().toString // адрес узла, который отключился

      val indexAndActorReference: (Integer, String) = searchDuplicateAddress(connectionList, addressActorWhoExit) // индекс и ссылка на актора объекта user из списка connectionList с адресом addressActorWhoExit

      val index: Integer = indexAndActorReference._1 // индекс объекта user из списка connectionList с адресом addressActorWhoExit


      Platform.runLater(new Runnable(){
        override def run(): Unit = {
          connectionList.remove(index.toInt)
        }
      })

//      connectionList.forEach(println)
//      val iter = connectionList.iterator()
//      while(iter.hasNext){
//        val u = iter.next()
//        println(u.getActorReference)
//      }

      chatsHistory.remove(indexAndActorReference._2) // удаление истории сообщений с пользователем user с узла с адресом addressActorWhoExit


      println("###################################################################################################################################################################\n")
      println("Node leave!\n")
      println("###################################################################################################################################################################\n")

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

  private val iconUrl = "n/icon2.png"
  private var primaryStage: Stage = _ // сцена чата
  private var inputDataToConnectToClusterStage: Stage = _
  private var inputNameStage: Stage = _ // сцена для ввода имени пользователя
  private var rootLayout: BorderPane = _
  private var controller: rootGUIController = _

  private val connectionList: ObservableList[user] = FXCollections.observableArrayList()
  var chatsHistory: HashMap[String, ArrayBuffer[HBox]] = HashMap[String, ArrayBuffer[HBox]]()
  var actor1: ActorRef = _ // ссылка на актора
  private var myHost: String = "127.0.0.1"
  private var myPort: Integer = 2559
  private var hostToConnect: String = "127.0.0.1" // хост для подключения к кластеру
  private var portToConnect: Integer = 2559 // порт для подключения к кластеру
  var userName: String = "actor1"

  def getMyHost: String = myHost

  def setMyHost(newValue: String): Unit = {
    this.myHost = newValue
  }

  def getMyPort: Integer = myPort

  def setMyPort(newValue: Integer): Unit = {
    this.myPort = newValue
  }

  def getHostToConnect: String = hostToConnect

  def setHostToConnect(newValue: String): Unit = {
    this.hostToConnect = newValue
  }

  def getPortToConnect: Integer = portToConnect

  def setPortToConnect(newValue: Integer) = {
    this.portToConnect = newValue
  }

  def getInputNameStage: Stage = inputNameStage

  def getPrimaryStage: Stage = primaryStage

  def getInputDataToConnectToClusterStage: Stage = inputDataToConnectToClusterStage

  def getConnectionList: ObservableList[user] = connectionList // функция возврата массива connectionList


  /**
   * Функция отправки актору сообщения, которое нужно отослать всем участникам группового чата
   * @param message кортеж из трех элементов - текста сообщения(из GUI элемента TextField), ссылки на актора отправителя и имени отправителя
   */
  def sendingMessage(message: (String, ActorRef, String, String, String)): Unit = {
    actor1 ! send(message)
  }

  /**
   * Функция отправки актору сообщения, которое нужно отослать конкретному актору
   * @param message сообщение для отправки. Содержит сообщение, ссылку на отправителя и имя пользователя отправителя
   * @param recipient строковое представление ссылки на актора-получателя (ActorRef.toString)
   */
  def privateSendingMessage(message: (String, ActorRef, String, String, String)): Unit = {
    actor1 ! privateSend(message)
  }


  override def start(primaryStage: Stage): Unit = {
    this.primaryStage = primaryStage

    initInputAndSetDataToConnectToCluster()
  
    initInputAndSetNameUser() // форма ввода имени пользователя и связывание его с переменной userName
    initCluster() // создание кластера, создание системы акторов, создание актора и передача ему имени(userName) и списка подключенных акторов(connectionList)
  
    this.primaryStage.setTitle("Чат. Пользователь: "+userName) // устанавливаем title окна с именем пользователя
  
    initRootLayout() // отображение основного окна чата

    sleep(500) // подумать, нужно ли это
  }


  /**
   * Метод инициализации стартового окна, в котором требуется ввести данные для либо создания сеанса чата, либо для
   * подключения к уже созданному сеансу
   */
  def initInputAndSetDataToConnectToCluster(): Unit = {
    try{
      val loader: FXMLLoader = new FXMLLoader()
      loader.setLocation(classOf[mainChat].getResource("inputDataToConnectToCluster.fxml"))
      val rootLayoutInputDataToConnectToCluster: AnchorPane = loader.load.asInstanceOf[AnchorPane]

      this.inputDataToConnectToClusterStage = new Stage()
      inputDataToConnectToClusterStage.setTitle("Ввод данных для подключения к чату или его создания")
      inputDataToConnectToClusterStage.getIcons.add(new Image(iconUrl))

      inputDataToConnectToClusterStage.setResizable(false)

      val controller: inputDataToConnectToClusterController = loader.getController
      controller.setMainChat(this)
      inputDataToConnectToClusterStage.setOnCloseRequest(controller.closeEventHandler)

      val scene: Scene = new Scene(rootLayoutInputDataToConnectToCluster)

      inputDataToConnectToClusterStage.setScene(scene)

      inputDataToConnectToClusterStage.showAndWait()

    }catch{
      case e: IOException =>println(e)
    }
  }

  /**
   * Метод инициализации окна ввода имени пользователя, в котором требуется ввести имя пользователя
   */
  def initInputAndSetNameUser(): Unit = {
    try{
      val loader: FXMLLoader = new FXMLLoader
      loader.setLocation(classOf[mainChat].getResource("inputName.fxml"))
      val rootLayoutSetNameUser: AnchorPane = loader.load.asInstanceOf[AnchorPane]

      this.inputNameStage = new Stage()
      inputNameStage.setTitle("Введите имя пользователя")
      inputNameStage.getIcons.add(new Image(iconUrl))
      //inputNameStage.initModality(Modality.WINDOW_MODAL)
      //inputNameStage.initOwner(primaryStage)

      inputNameStage.setResizable(false) // запрет на изменение размера окна

      val controller: inputNameController = loader.getController
      controller.setMainChat(this)
      inputNameStage.setOnCloseRequest(controller.closeEventHandler)

      val scene: Scene = new Scene(rootLayoutSetNameUser)
      scene.setOnKeyPressed(controller.sendUserNameAfterEnterPressed)
      inputNameStage.setScene(scene)

      inputNameStage.showAndWait()

    }catch {
      case e: IOException => println(e)
    }

  }

  /**
   * Метод инициализации кластера
   */
  def initCluster(): Unit = {
    val conf1 = ConfigFactory.load() // загружаем основной конфигурационный файл
    val conf2 = ConfigFactory.parseString( // конфигурация на основе введенных пользователем данных
      f"""akka.remote.artery.canonical{
         hostname="$myHost"
         port=$myPort}

         akka.cluster.seed-nodes=["akka://ActorSystem@$hostToConnect:$portToConnect"]
        """)
    val conf = conf2.withFallback(conf1)

    val system: ActorSystem = ActorSystem("ActorSystem", conf)

    actor1 = system.actorOf(Props[actor](), alphanumeric.take(10).mkString("")) // создаем актора с уникальным, сгенерированным случайно именем
    println("###################################################################################################################################################################\n")
    println("Actor created!\n")
    println("###################################################################################################################################################################\n")

    actor1 ! setUserNameActor(userName) // отправляем актору сообщение с именем пользователя userName(для каждого пользователя свой актор)

    actor1 ! setChatsHistory(chatsHistory) // отправляем актору ссылку на историю сообщений

    actor1 ! setConnectionList(connectionList) // отправляем актору ссылку на список подключений(ObservableList[user])

  }

  /**
   * Метод инициализации основного окна, содержащего список пользователей, диалог и т.д.
   */
  def initRootLayout(): Unit = {
    try{
      val loader: FXMLLoader = new FXMLLoader
      loader.setLocation(classOf[mainChat].getResource("rootGUI2.fxml"))
      rootLayout = loader.load.asInstanceOf[BorderPane]

      primaryStage.getIcons.add(new Image(iconUrl))


      this.controller = loader.getController
      primaryStage.setOnCloseRequest(controller.closeEventHandler)
      controller.setMainChat(this)
      actor1 ! controller // передаем актору контроллер, чтобы актор мог вызвать метод addLabel и добавить полученное сообщение на экран


      val scene: Scene = new Scene(rootLayout)
      scene.setOnKeyPressed(controller.sendMessageAfterEnterPressed)
      primaryStage.setScene(scene)
      primaryStage.show()

    }catch{
      case e: Exception => e.printStackTrace()
    }
  }

}