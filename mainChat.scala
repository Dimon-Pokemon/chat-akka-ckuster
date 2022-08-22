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
import javafx.scene.image.Image

import java.lang.Thread.sleep
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.util.Random.alphanumeric



case class send(message: (String, ActorRef, String, String))

case class publicMessage(message: (String, ActorRef, String, String))

case class privateSend(message: (String, ActorRef, String, String))

case class privateMessage(message: (String, ActorRef, String, String))

case class setConnectionList(connList: ObservableList[user])

//case class setChatsHistory(chatsHistory: HashMap[String, ObservableList[HBox]] )
//case class setChatsHistory(chatsHist: HashMap[String, ArrayBuffer[HBox]])
case class setChatsHistory(chatsHist: ObservableList[HashMap[String, ArrayBuffer[HBox]]])

case class myNameIs(name: String, host: String, port: Integer, actorReference: ActorRef)

case class setUserNameActor(uN: String)



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
  */
//  def searchDuplicate(userConnectionList: ObservableList[user], actorReference: String): Boolean = {
//    val iteratorUserConnectionList = userConnectionList.iterator()
//    while(iteratorUserConnectionList.hasNext){
//      val element: user = iteratorUserConnectionList.next
//      if(element.getActorReference.equals(actorReference)) {
//        return true // без return выдает ошибку
//      }
//    }
//    false
//  }
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

//  /**
//   * Функция ищет в списке подключений userConnectionList: ObservableList[user] объект user с атрибутом attribute,
//   * который равен параметру value: String. Возвращает true, если найден такой объект user, и false - в противном случае
//   * @param userConnectionList список с объектами user
//   * @param actorReference значение атрибута объекта user, которое требуется найти в списке userConnectionList
//   */
//  def searchDuplicate(userConnectionList: ObservableList[user], actorReference: String): Boolean = {
//    val iteratorUserConnectionList = userConnectionList.iterator()
//    while(iteratorUserConnectionList.hasNext){
//      val element: user = iteratorUserConnectionList.next
//      if(element.getActorReference.equals(actorReference)) {
//        return true // без return выдает ошибку
//      }
//    }
//    false
//  }


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
      val usr: user = new user("Общий чат", "publicChat", 0000, "publicChat")
      val selectedChatHistory: ArrayBuffer[HBox] = ArrayBuffer.empty[HBox] // пустой массив контейнеров HBox.
      chatsHistory.get(0).update(usr.getActorReference, selectedChatHistory) // создание новой пары ключ(ссылка на актора)-значение(массив selectedChatHistory)
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
      if(!(self==actorReference) && !(searchDuplicate(connectionList, usr.getActorReference)>0)) { // !searchDuplicate(connectionList, usr.getActorReference)
        connectionList.add(usr) // добавление нового пользователя в список подключений
        //val selectedChatHistory: ObservableList[HBox] = FXCollections.observableArrayList()
        val selectedChatHistory: ArrayBuffer[HBox] = ArrayBuffer.empty[HBox]
        chatsHistory.get(0).update(usr.getActorReference, selectedChatHistory)
        println(chatsHistory.get(0)("publicChat"))
      }
    case s: rootGUIController => controller = s // если актор получает объект типа rootGUIController, то этот объект - контроллер => устанавливаем controller = s
    case publicMessage(message: (String, ActorRef, String, String)) => // если актор получает кортеж(нужно уточнить) из трех элементов String, ActorRef, String, String, то этот кортеж - данные, отправленные актором, на который подписан текущий актор
      // s._4 - контакт, выбранный отправителем
      // s._3 - имя пользователя, который прислал сообщение
      val stringLocalActorReference: String = "/"+message._2.path.parent.name+"/"+message._2.path.name // ссылка на актора вида /user/actor#3243 без указания системы акторов, хоста и порта
      //val myStringLocalActorReference: String = "/"+self.path.parent.name+"/"+self.path.name
      if(!(message._2 == self)){
        val controllerSelectedUser: String = controller.getSelectedUser // получаем из класса-контроллера значение атрибута selectedUser(ссылка на актора выбранного пользователя из списка чатов(контактов))
        if(controllerSelectedUser.equals("publicChat")){
          controller.addLabel(message._1, controller.getMessageHistory, message._3)
        }else if(message._4.equals("publicChat")){
          controller.addLabelIntoChatsHistory(message._1, message._3, "publicChat")
        }else{
          controller.addLabelIntoChatsHistory(message._1, message._3, stringLocalActorReference)
        }
        // если получатель и отправитель находятся в одном чате, т.е. либо в общим чате, либо в личном чате друг с другом
//        if((controllerSelectedUser.equals("publicChat") && (s._4.equals("publicChat"))) || (controllerSelectedUser.equals(stringLocalActorReference) && s._4.equals(myStringLocalActorReference))) {
//          controller.addLabel(s._1, controller.getMessageHistory, s._3) // добавляем в чат(HBox) новую метку(Label) с помощью метода класса-контроллера(controller)
//          log.info("Got {}", s)
//          //println(s._1) // выводим сообщение, полученное от актора, на которого подписан текущий актор}
//        }else{
//          controller.addLabelIntoChatsHistory(s._1, s._3, stringLocalActorReference)
//        }
      }
    case privateMessage(message: (String, ActorRef, String, String)) =>
      val stringLocalActorReference: String = "/"+message._2.path.parent.name+"/"+message._2.path.name // ссылка на актора вида /user/actor#3243 без указания системы акторов, хоста и порта
      if(!(message._2 == self)){
        val controllerSelectedUser: String = controller.getSelectedUser // получаем из класса-контроллера значение атрибута selectedUser(ссылка на актора выбранного пользователя из списка чатов(контактов))
        if(controllerSelectedUser.equals(stringLocalActorReference)){
          controller.addLabel(message._1, controller.getMessageHistory, message._3)
        }else{
          controller.addLabelIntoChatsHistory(message._1, message._3, stringLocalActorReference)
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
    case privateSend(message: (String, ActorRef, String, String))=>
      mediator ! DistributedPubSubMediator.Send(message._4, privateMessage(message), localAffinity=false)
    case send(message: (String, ActorRef, String, String))=> // если актор получает объект кейс класса send с единственным аргументом-кортежом(message: (String, ActorRef, String)), то актор рассылает аргумент message всем подписчикам
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
        //mediator ! DistributedPubSubMediator.Subscribe(sender().path.toString, self)
        sleep(2000) // КОСТЫЛЬ. ОГРОМНЫЙ КОСТЫЛЬ
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
      }
    case UnreachableMember(member) =>
      log.info(s"[Listener] node is unreachable: $member")
      //println(member.toString)
//    case MemberDowned(member) =>
//      val senderRef: ActorRef = sender()
//      val shortActorReferenceWhoLeft = "/"+senderRef.path.parent.name+"/"+senderRef.path.name
//      val index: Integer = searchDuplicate(connectionList, shortActorReferenceWhoLeft)
//      connectionList.remove(index)
//
//      log.info(s"[Listener] node is removed: $member")
//      println("Node"+member.toString+" leave")
    case MemberRemoved(member, prevStatus) => // NEW
      //println(sender().path)

      val senderRef: ActorRef = sender()
      val shortActorReferenceWhoLeft = "/"+senderRef.path.parent.name+"/"+senderRef.path.name
      val index: Integer = searchDuplicate(connectionList, shortActorReferenceWhoLeft)
      println("Hello")
      println(connectionList.remove(index.toInt))

      println("###################################################################################################################################################################\n")
      println("Node leave!\n")
      println("###################################################################################################################################################################\n")

      log.info(s"[Listener] node is removed: $member")
      println("Node"+member.toString+" leave")
//    case MemberExited(member) =>
//      val senderRef: ActorRef = sender()
//      val shortActorReferenceWhoLeft = "/"+senderRef.path.parent.name+"/"+senderRef.path.name
//      val index: Integer = searchDuplicate(connectionList, shortActorReferenceWhoLeft)
//      connectionList.remove(index)
//
//      println("###################################################################################################################################################################\n")
//      println("Node leave!\n")
//      println("###################################################################################################################################################################\n")
//
//      log.info(s"[Listener] node is removed: $member")
//      println("Node"+member.toString+" leave")
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

  private val iconUrl = "icon.png"
  private var primaryStage: Stage = _ // сцена чата
  private var inputDataToConnectToClusterStage: Stage = _
  private var inputNameStage: Stage = _ // сцена для ввода имени пользователя
  private var rootLayout: BorderPane = _
  private var controller: rootGUIController = _

  private val connectionList: ObservableList[user] = FXCollections.observableArrayList()
  //val chatsHistory: HashMap[String, ObservableList[HBox]] = HashMap[String, ObservableList[HBox]]()
  var chatsHistory: ObservableList[HashMap[String, ArrayBuffer[HBox]]] = FXCollections.observableArrayList()
  chatsHistory.add(HashMap[String, ArrayBuffer[HBox]]())
  var actor1: ActorRef = _ // ссылка на актора
  private var myHost: String = "127.0.0.1"
  private var myPort: Integer = 2559
  private var hostToConnect: String = myHost
  private var portToConnect: Integer = myPort
  var userName: String = "actor1"

  /**
   * Геттер. Возвращает переменную myHost
   * @return myHost: String
   */
  def getMyHost: String = myHost

  /**
   * Сеттер. Устанавливает новое значение переменной myHost
   * @param newValue новое значение
   */
  def setMyHost(newValue: String): Unit = {
    this.myHost = newValue
  }

  /**
   * Геттер. Возвращает переменную myPort
   * @return myPort: Integer
   */
  def getMyPort: Integer = myPort

  /**
   * Сеттер. Устанавливает новое значение переменной myPort
   * @param newValue новое значение
   */
  def setMyPort(newValue: Integer): Unit = {
    this.myPort = newValue
  }

  /**
   * Геттер. Возвращает значение переменной hostToConnect
   * @return hostToConnect: String
   */
  def getHostToConnect: String = hostToConnect

  /**
   * Сеттер. Устанавливает новое значение переменной hostToConnect
   * @param newValue новое значение
   */
  def setHostToConnect(newValue: String): Unit = {
    this.hostToConnect = newValue
  }

  /**
   * Геттер. Возвращает значение переменной portToConnect
   * @return portToConnect: Integer
   */
  def getPortToConnect: Integer = portToConnect

  /**
   * Сеттер. Устанавливает новое значение переменной portToConnect
   * @param newValue новое значение
   */
  def setPortToConnect(newValue: Integer) = {
    this.portToConnect = newValue
  }

  /**
   * Геттер. Возвращает корневую сцену inputNameStage
   * @return inputNameStage: Stage
   */
  def getInputNameStage: Stage = inputNameStage

  /**
   * Геттер. Возвращает корневую сцену primaryStage
   * @return primaryStage: Stage
   */
  def getPrimaryStage: Stage = primaryStage

  /**
   * Геттер. Возвращает корневую сцену inputDataToConnectToClusterStage
   * @return inputDataToConnectToClusterStage: Stage
   */
  def getInputDataToConnectToClusterStage: Stage = inputDataToConnectToClusterStage

  /**
   * Геттер. Возвращает список подключений
   * @return primaryStage: Stage
   */
  def getConnectionList: ObservableList[user] = connectionList // функция возврата массива connectionList


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


  override def start(primaryStage: Stage): Unit = {
    this.primaryStage = primaryStage

    initInputAndSetDataToConnectToCluster()
  
    initInputAndSetNameUser() // форма ввода имени пользователя и связывание его с переменной userName
    initCluster() // создание кластера, создание системы акторов, создание актора и передача ему имени(userName) и списка подключенных акторов(connectionList)
  
    this.primaryStage.setTitle("Чат. Пользователь: "+userName) // устанавливаем title окна с именем пользователя
  
    initRootLayout() // отображение основного окна чата

    sleep(500)
  }

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

      val scene: Scene = new Scene(rootLayoutInputDataToConnectToCluster)

      inputDataToConnectToClusterStage.setScene(scene)

      inputDataToConnectToClusterStage.showAndWait()

    }catch{
      case e: IOException =>println(e)
    }
  }

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

      val scene: Scene = new Scene(rootLayoutSetNameUser)
      scene.setOnKeyPressed(controller.sendUserNameAfterEnterPressed)
      inputNameStage.setScene(scene)

      inputNameStage.showAndWait()

    }catch {
      case e: IOException => println(e)
    }

  }

  def initCluster(): Unit = {
    val conf1 = ConfigFactory.load() // загружаем основной конфигурационный файл
    val conf2 = ConfigFactory.parseString( // конфигурация на основе введенных пользователем данных
      f"""akka.remote.artery.canonical{
         hostname="$myHost"
         port=$myPort}

         cluster.seed-nodes=["akka://ActorSystem@$hostToConnect:$portToConnect"]
        """)
    val conf = conf2.withFallback(conf1)
    //ConfigFactory.systemProperties()
    //-DHOST="127.0.0.1" -DPORT=2551


    val system: ActorSystem = ActorSystem("ActorSystem", conf)


    actor1 = system.actorOf(Props[actor](), alphanumeric.take(10).mkString("")) // создаем актора с уникальным, сгенерированным случайно именем
    println("###################################################################################################################################################################\n")
    println("Actor created!\n")
    println("###################################################################################################################################################################\n")

    actor1 ! setUserNameActor(userName) // отправляем актору сообщение с именем пользователя userName(для каждого пользователя свой актор)

    actor1 ! setChatsHistory(chatsHistory) // отправляем актору ссылку на историю сообщений

    actor1 ! setConnectionList(connectionList) // отправляем актору ссылку на список подключений(ObservableList[user])



    //actor1 ! "Ты подключился"
  }

  def initRootLayout(): Unit = {
    try{
      //println(chatsHistory.get(0).foreach(println)) //"publicChat"
      //println(chatsHistory("publicChat"))
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

      //controller.addLabel("hello", controller.getMessageStory)
      //controller.setSend(send.)
    }catch{
      case e: Exception => e.printStackTrace()
    }
  }

  //System.exit(0)
}