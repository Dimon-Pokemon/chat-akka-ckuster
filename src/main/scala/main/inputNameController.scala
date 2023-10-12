import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.scene.input.KeyEvent
import javafx.stage.WindowEvent

class inputNameController {

  @FXML
  var userName: TextField = null

  @FXML
  var button: Button = null

  private var mainChat: mainChat = null // Ссылка на основной класс приложения

  /**
   * Установка ссылки на основной класс приложения для доступ к его методам и атрибутам
   * @param mainChat - ссылка на основной класс mainChat
   */
  def setMainChat(mainChat: mainChat): Unit = {
    this.mainChat = mainChat
  }


  /**
   * Метод, определяющий операции, производимые при нажатии на крестик
   * @return EventHandler[WindowEvent]
   */
  def closeEventHandler: EventHandler[WindowEvent] = new EventHandler[WindowEvent](){
    override def handle(event: WindowEvent): Unit = {
      mainChat.getInputNameStage.close()
      System.exit(0) // Завершение работы приложения.

    }
  }
  
  /**
   * Функция вызывает метод setUserName при возникновении события нажатия клавиши Enter
   * @return EventHandler[KeyEvent]
   */
  def sendUserNameAfterEnterPressed: EventHandler[KeyEvent] = new EventHandler[KeyEvent]:
    override def handle(event: KeyEvent): Unit = {
      if(event.getCode.toString.equals("ENTER")) setUserName
    }

  /**
   * Метод для получения введенного на GUI имени пользователя и
   * установки его в качестве атрибута userName класса mainChat.
   * Это имя будет отображаться на GUI.
   */
  @FXML
  protected def setUserName: Unit = {
    val textUserName = userName.getText // Получение введенного имени с GUI.

    if(!textUserName.isEmpty){
      mainChat.userName = textUserName // Установка введенного имени в атрибут userName.
      mainChat.getInputNameStage.close() // Закрыаем окно ввода имени пользователя.

    }
  }

}
