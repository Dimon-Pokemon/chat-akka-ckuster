import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.geometry.{Insets, Pos}
import javafx.scene.control
import javafx.scene.input.KeyEvent
import javafx.scene.control.{Button, ScrollPane, TableColumn, TableView, TextField}
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.text.{Text, TextFlow}
import javafx.scene.paint.Color
import javafx.stage.WindowEvent

import java.time.LocalTime
import java.time.format.DateTimeFormatter

class rootGUIController {
  @FXML
  private var button: Button = null // кнопка для отправки сообщения "send"

  @FXML
  private var message: TextField = null // текстовое поле ввода

  @FXML
  private var messageHistory: VBox = null // сообщения текущего чата( не путать с chatsHistory - ВСЕ сообщения со ВСЕХ чатов)

  @FXML
  private var scroll: ScrollPane = null

  @FXML
  private var contacts: TableView[user] = null // таблица для отображения списка подключенных пользователей с которыми можно общаться (т.е. список чатов)

  @FXML
  private var contactsColumn: TableColumn[user, String] = null // столбец таблицы contacts для отображения списка подключенных пользователей с которыми можно общаться (т.е. список чатов)

  private var mainChat: mainChat = null // ссылка на главный класс mainChat

  private var typeChat: String = "public" // тип выбранного чата. По умолчанию это общий чат
  
  private var selectedUser: String = "publicChat" // ссылка на актора выбранного из таблицы contacts пользователя. Если выбран общий чат, равен "publicChat"


  @FXML
  private def initialize(): Unit = {
    try {
      messageHistory.heightProperty().addListener((observable, oldValue, newValue) => scroll.setVvalue(newValue.asInstanceOf[Double]))
      contactsColumn.setCellValueFactory((cellData) => cellData.getValue().nameProperty)

      contacts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) => change(newValue))
    }catch {
      case e: IllegalStateException => println("Возникла ошибка при выходе одного из узлов из кластера!")
    }
  }

  /**
   * Метод для получения ссылки на главную часть приложения mainChat и
   * для установки модели данных для таблицы contacts
   * @param mainChat ссылка на главный класс
   */
  def setMainChat(mainChat: mainChat): Unit = {
    this.mainChat = mainChat

    contacts.setItems(mainChat.getConnectionList)
  }


  /**
   * Функция, вызываемая при выборе пользователем конкретного контакта из списка контактов (в GUI).
   * Очищает chatsHistory от сообщений из диалога с выбранным ранее контактом из списка контактов, а затем
   * заполняет chatsHistory сообщениями из диалога с тем контактом, который выбран на данный момент.
   * Сообщения берутся из map-структуры messageHistory, которая хранит историю сообщений со всеми участниками чата
   * в виде пары "ссылка_на_актора":"массив_с_сообщениями_в_виде_HBox"
   * @param user - выбранный объект типа user, содержащий имя(поле name), ip-адрес(поле ip) и хост(поле host) узла на котором создан,
   * ссылка на актора(поле actorReference).
   */
  def change(user: user): Unit ={
    println("Selected user " + user.getName + ". His ActorReference is " + user.getActorReference)
    if(user.getActorReference.equals("publicChat")){
      typeChat = "public"
      selectedUser = "publicChat"
      messageHistory.getChildren.clear()
      mainChat.chatsHistory(selectedUser).foreach(messageHistory.getChildren.add)
    }else{
      typeChat = "private"
      selectedUser = user.getActorReference
      messageHistory.getChildren.clear()
      mainChat.chatsHistory(selectedUser).foreach(messageHistory.getChildren.add)
    }
  }

  /**
   * Метод, определяющий операции, производимые при нажатии на крестик
   * @return
   */
  def closeEventHandler: EventHandler[WindowEvent] = new EventHandler[WindowEvent](){
    override def handle(event: WindowEvent): Unit = {
      mainChat.getPrimaryStage.close() // закрытие окна
      System.exit(0) // завершение работы программы

    }
  }

  /**
   * Функция вызывает метод sendMessage при возникновении события нажатия клавиши Enter
   * @return EventHandler[KeyEvent]
   */
  def sendMessageAfterEnterPressed: EventHandler[KeyEvent] = new EventHandler[KeyEvent]:
    override def handle(event: KeyEvent): Unit = {
      if(event.getCode.toString.equals("ENTER")) sendMessage
    }


  /**
   * Функционал кнопки
   */
  @FXML
  protected def sendMessage: Unit = { //было private
    // получение текста в строке ввода
    val messageToSend = message.getText //LocalTime.now().toString

    // Т.к. чат работает в локальной сети, то это значит, что все компьютеры находятся в одном часовом поясе
    var t = LocalTime.now() // получаем текущее время в формате часы:минуты:секунды:наносекунды
    t = t.minusNanos(t.getNano) // форматируем полученное время путем вычитания наносекунд, чтобы получить время в формате чч:мм:сс
    t = t.minusSeconds(t.getSecond) // форматируем полученное время путем вычитания секунд, чтобы получить время в формате чч:мм
    val time = t.toString // получаем строковое представление объекта типа LocalTime

    // проверка пустоты строки ввода, т.к.
    // пользователь может ничего не вводить и
    // нажать на кнопку отправки просто так
    if(!messageToSend.isEmpty){
      // отправляем сообщение в зависимости от выбранного типа чата
      if(typeChat.equals("public")) {
        // отправка сообшения в личный чат
        mainChat.sendingMessage((messageToSend, mainChat.actor1, mainChat.userName, selectedUser, time))
      }else{
        // отправка личного сообщения
        mainChat.privateSendingMessage((messageToSend, mainChat.actor1, mainChat.userName, selectedUser, time) )
      }
      // горизонтальный контейнер для упаковывания сообщения пользователя
      var hBox: HBox = new HBox()
      var hBoxForTime = createHBoxForTime("RIGHT", time)
      // определение свойств контейнеров:
      // - отображение сообщения справа
      // - границы
      hBox.setAlignment(Pos.CENTER_RIGHT)
      hBox.setPadding(new Insets(0, 5, 5, 10))

      // создание текста для отображения форматирования и
      // последующего отображения в GUI (т.к. с обычной строкой String нельзя работать в fx)
      val text: Text = new Text(messageToSend)
      // т.к. обычный текст Text имеет ограниченное форматирование,
      // создается TextFlow, расширяющий возможности форматирования,
      // а главное, позволяющий автоматически переносить длинный текст
      // на следующую строку
      val textFlow: TextFlow = new TextFlow(text)

      // цвет текста
      textFlow.setStyle(" -fx-background-color: rgb(15, 125, 242);" + " -fx-background-radius: 20px;")//"-fx-color: rgb(239, 242, 255);" +

      // установка отступов
      textFlow.setPadding(new Insets(5, 10, 5, 10))
      // тут что-то непонятное. Зачем использовать устанавливать цвет для text: Text, если есть textFlow: TextFlow?
      text.setFill(Color.color(0.934, 0.945, 0.996))

      messageHistory.getChildren.add(hBoxForTime)
      hBox.getChildren().add(textFlow)
      messageHistory.getChildren.add(hBox)


      mainChat.chatsHistory(selectedUser).append(hBoxForTime) // добавляем HBox, содержащий время, в историю сообщений
      mainChat.chatsHistory(selectedUser).append(hBox) // добавляем HBox, содержащий отформатированный текст сообщения, в историю сообщений

      // очищаем поле ввода TextField
      message.clear()
    }
  }


  /**
   * Метод для создания графического элемента-контейнера HBox, который содержит время получения полученного сообщения
   * и который будет отображаться в messageHistory: VBox, т.е. будет отображаться в самом диалоге
   * @param pos положение HBox в контейнере( в данном случае в messageHistory: VBox)
   * @param time строковое представление объекта LocalTime времени
   * @return контейнер HBox
   */
  def createHBoxForTime(pos: String, time: String): HBox = {
    val hBox: HBox = new HBox()
    if(pos.equals("RIGHT")) {
      hBox.setAlignment(Pos.CENTER_RIGHT)
      hBox.setPadding(new Insets(10, 15, 0, 10))
    }else{
      hBox.setAlignment(Pos.CENTER_LEFT)
      hBox.setPadding(new Insets(10, 10, 0, 15))
    }
    val timeText: Text = new Text(time)
    hBox.getChildren().add(timeText)

    hBox

  }


  /**
   * Метод для создания графического элемента-контейнера HBox, который содержит текст полученного сообщения и имя его отправителя
   * и который будет отображаться в messageHistory: VBox, т.е. будет отображаться в самом диалоге
   * @param message текст полученного сообщения
   * @param user имя отправителя
   * @return контейнер HBox
   */
  def createHBox(message: String, user: String): HBox = {
      val hBox: HBox = new HBox() // новый горизонтальный контейнер типа HBox, в который будет упаковывается сообщение от пользователя-собеседника
      hBox.setAlignment(Pos.CENTER_LEFT) // позиция hBox на макете (в центре слева)
      hBox.setPadding(new Insets(5, 5, 5, 10))

      val userNameText: Text = new Text(user+": ") // Text объект из имя пользователя (userName), который ПРИСЛАЛ сообщение
      val userNameTextFlow: TextFlow = new TextFlow(userNameText) // TextFlow объект из имя пользователя (userNameText) класса Text
      userNameTextFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" + "-fx-background-radius: 10px 0px 0px 10px;")
      userNameTextFlow.setPadding(new Insets(5, 0, 5, 10))
      userNameText.setFill(Color.rgb(15, 125, 242))

      val text: Text  = new Text(message) // Text объект из сообщения пользователя-собеседника (тот, кто прислал новое сообщение)
      val textFlow: TextFlow = new TextFlow(text)  // TextFlow объект из сообщения пользователя-собеседника
      textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" + "-fx-background-radius: 0px 10px 10px 0px;") // установка стиля textFlow
      textFlow.setPadding(new Insets(5, 10, 5, 0))

      hBox.getChildren.add(userNameTextFlow)
      hBox.getChildren.add(textFlow) // помещаем в горизонтальный контейнер hBox: HBox сообщение пользователя-собеседника textFlow

      hBox
  }

  /**
   * Функция добавляет в chatsHistory сообщение, полученное актором, но НЕ ОТОБРАЖАЕТ его на экране.
   * Нужно для ситуации, когда пользователь, находясь в чате X, получает сообщение из чата Y
   * @param messageFromClient сообщение(текст), которое получил актор
   * @param userName имя пользователя, отправившего сообщение актору, получившему сообщение
   */
  def addLabelIntoChatsHistory(messageFromClient: String, userName: String, senderReference: String, time: String): Unit = {
    val hBox: HBox = createHBox(messageFromClient, userName)
    val hBoxForTime: HBox = createHBoxForTime("LEFT", time)
    mainChat.chatsHistory(senderReference).append(hBoxForTime)
    mainChat.chatsHistory(senderReference).append(hBox)
  }


  /**
   * Функция выводит сообщение, полученное актором, на экран и добавляет его в chatsHistory
   * @param messageFromClient сообщение(текст), которое получил актор
   * @param vBox объект-контейнер вертикального типа VBox для отображения всех сообщений
   * @param userName имя пользователя, отправившего сообщение актору, получившему сообщение
   * @return
   */
  def addLabel(messageFromClient: String, vBox: VBox = this.messageHistory, userName: String, time: String) = {
    val hBox: HBox = createHBox(messageFromClient, userName)
    val hBoxForTime: HBox = createHBoxForTime("LEFT", time)
    Platform.runLater(new Runnable(){
      override def run(): Unit = {
        vBox.getChildren().add(hBoxForTime)
        vBox.getChildren().add(hBox)
      }
    })

    mainChat.chatsHistory(selectedUser).append(hBoxForTime)
    mainChat.chatsHistory(selectedUser).append(hBox)
  }


  def getMessageHistory: VBox = messageHistory

  def getSelectedUser: String = selectedUser

}
