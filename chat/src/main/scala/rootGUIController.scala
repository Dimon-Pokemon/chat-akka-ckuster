import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.geometry.{Insets, Pos}
import javafx.scene.control
import javafx.scene.control.{Button, ScrollPane, TableColumn, TableView, TextField}
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.text.{Text, TextFlow}
import javafx.scene.paint.Color
import javafx.stage.WindowEvent

import scala.collection.mutable.ArrayBuffer

//object rootGUIController{
//  def addLabel(messageFromClient: String, vBox: VBox) = {
//    val hBox: HBox = new HBox()
//    hBox.setAlignment(Pos.CENTER_LEFT)
//    hBox.setPadding(new Insets(5, 5, 5, 10))
//
//    val text: Text  = new Text(messageFromClient)
//    val textFlow: TextFlow = new TextFlow(text)
//    textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" + "-fx-background-radius: 20px")
//    textFlow.setPadding(new Insets(5, 10, 5, 10))
//
//    hBox.getChildren.add(textFlow)
//
//    vBox.getChildren.add(hBox)
//
//    Platform.runLater(new Runnable(){
//      override def run(): Unit = {
//        vBox.getChildren().add(hBox)
//      }
//    })
//  }
//
//}

class rootGUIController {
  @FXML
  private var button: Button = null

  @FXML
  private var message: TextField = null

  @FXML
  private var messageHistory: VBox = null

  @FXML
  private var scroll: ScrollPane = null

  @FXML
  private var contacts: TableView[user] = null

  private var mainChat: mainChat = null


  @FXML
  private def initialize(): Unit = {
  messageHistory.heightProperty().addListener((observable, oldValue, newValue) => scroll.setVvalue(newValue.asInstanceOf[Double]))
  }

  def setMainChat(mainChat: mainChat): Unit = {
    this.mainChat = mainChat

    //contacts.setItems() надо придумать класс пользователя для отображения его в таблице контактов
  }


  def closeEventHadler: EventHandler[WindowEvent] = new EventHandler[WindowEvent](){
    override def handle(event: WindowEvent): Unit = {
      mainChat.getPrimaryStage.close()
      System.exit(0)

    }
  }
  /**
   * Функционал кнопки
   */
  @FXML
  private def sendMessage: Unit = {
    // получение текста в строке ввода
    val messageToSend = message.getText

    // проверка пустоты строки ввода, т.к.
    // пользователь может ничего не вводить и
    // нажать на кнопку отправки просто так, по приколу
    if(!messageToSend.isEmpty){
      // горизонтальный контейнер для упаковывания сообщения пользователя
      var hBox: HBox = new HBox()
      // определение свойств контейнера:
      // - отображение сообщения справа
      // - границы
      hBox.setAlignment(Pos.CENTER_RIGHT)
      hBox.setPadding(new Insets(5, 5, 5, 10))

      // создание текста для отображения форматирования и
      // последующего отображения в GUI (т.к. с обычной строкой String нельзя работать в fx)
      val text: Text = new Text(messageToSend)
      // т.к. обычный текст Text имеет скромной форматирование,
      // создается TextFlow, расширяющий возможности форматирования,
      // а главное, позволяющий автоматически переносить длинный текст
      // на другую строку
      val textFlow: TextFlow = new TextFlow(text)

      // цвет текста
      textFlow.setStyle(" -fx-background-color: rgb(15, 125, 242);" + " -fx-background-radius: 20px;")//"-fx-color: rgb(239, 242, 255);" +

      // установка отступов
      textFlow.setPadding(new Insets(5, 10, 5, 10))
      // тут что-то непонятное. Зачем использовать Text, если есть TextField?
      text.setFill(Color.color(0.934, 0.945, 0.996))

      hBox.getChildren().add(textFlow)
      messageHistory.getChildren.add(hBox)

      // далее отправляем сообщение другим пользователям
      // в классе mainChat написана функция sendingMessage, которая отправляет актору сообщение с кейс классом case class send(message: (String, ActorRef))
      // пришлось так сделать, т.к. сам класс send определен за пределами класса mainChat
      // а при определении класса send внутри mainChat, он становится недоступным для класса-актора actor
      // При определении внутри mainChat и класса-актора actor, и кейс класса send jvm выбрасывает ошибку
      mainChat.actor1 ! mainChat.sendingMessage((messageToSend, mainChat.actor1, mainChat.userName))

      // в конце очищаем поле ввода
      message.clear()
    }
  }

    def addLabel(messageFromClient: String, vBox: VBox = this.messageHistory, userName: String) = {
//      val hBox1: HBox = new HBox() // новый горизонтальный контейнер типа HBox, в который будет упаковываться имя пользователя-собеседника
//      hBox1.setAlignment(Pos.CENTER_LEFT) // позиция hBox1 на макете (в центре слева)
//      hBox1.setPadding(new Insets(5, 5, 0, 10))

      val hBox: HBox = new HBox() // новый горизонтальный контейнер типа HBox, в который будет упаковывается сообщение от пользователя-собеседника
      hBox.setAlignment(Pos.CENTER_LEFT) // позиция hBox на макете (в центре слева)
      hBox.setPadding(new Insets(5, 5, 5, 10))

      val userNameText: Text = new Text(userName+": ") // Text объект из имя пользователя (userName), который ПРИСЛАЛ сообщение
      val userNameTextFlow: TextFlow = new TextFlow(userNameText) // TextFlow объект из имя пользователя (userNameText) класса Text
      userNameTextFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" + "-fx-background-radius: 10px 0px 0px 10px;")
      userNameTextFlow.setPadding(new Insets(5, 0, 5, 10))
      userNameText.setFill(Color.rgb(15, 125, 242))

      val text: Text  = new Text(messageFromClient) // Text объект из сообщения пользователя-собеседника (тот, кто прислал новое сообщение)
      val textFlow: TextFlow = new TextFlow(text)  // TextFlow объект из сообщения пользователя-собеседника
      textFlow.setStyle("-fx-background-color: rgb(233, 233, 235);" + "-fx-background-radius: 0px 10px 10px 0px;") // установка стиля textFlow
      textFlow.setPadding(new Insets(5, 10, 5, 0))

      // два TextFlow, один содержит имя отправителя сообщения (собеседник), а второй текст сообщения отправителя
      // располагаются они в HBox, который, в свою очередь, располагается в VBox
      // эти два TextFlow должны выглядеть как один элемент (см. оформление сообщения в ВК)

      //hBox1.getChildren.add(userNameTextFlow) // помещаем в горизонтальный контейнер hBox1: HBox имя пользователя-собеседника textFlow

      hBox.getChildren.add(userNameTextFlow)
      hBox.getChildren.add(textFlow) // помещаем в горизонтальный контейнер hBox: HBox сообщение пользователя-собеседника textFlow

      //vBox.getChildren.add(hBox)

      Platform.runLater(new Runnable(){
        override def run(): Unit = {
          //vBox.getChildren().add(hBox1)
          vBox.getChildren().add(hBox)
        }
      })
    }


  def getMessageHistory: VBox = messageHistory

}
