import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.stage.WindowEvent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

class inputDataToConnectToClusterController {

  @FXML
  private var myHostForConnect: TextField = _

  @FXML
  private var myPortForConnect: TextField = _

  @FXML
  private var connectHost: TextField = _

  @FXML
  private var connectPort: TextField = _

  @FXML
  private var connectionButton: Button = _

  @FXML
  private var createHost: TextField = _

  @FXML
  private var createPort: TextField = _

  @FXML
  private var createButton: Button = _

  private var mainChat: mainChat = _

  val regValidNumbers: String = """([01]?\d\d?|2[0-4]\d|25[0-5])"""

  val reg: String =  f"^$regValidNumbers\\.$regValidNumbers\\.$regValidNumbers\\.$regValidNumbers$$"

  def setMainChat(mainChat: mainChat): Unit = {
    this.mainChat = mainChat
  }

  /**
   * Метод, определяющий операции, производимые при нажатии на крестик
   * @return EventHandler[WindowEvent]
   */
  def closeEventHadler: EventHandler[WindowEvent] = new EventHandler[WindowEvent](){
    override def handle(event: WindowEvent): Unit = {
      mainChat.getInputDataToConnectToClusterStage.close()
      System.exit(0)

    }
  }

  def alert(title: String, headerText: String, contentText: String): Unit = {
    val alert: Alert = new Alert(AlertType.WARNING)
    alert.setTitle(title)
    alert.setHeaderText(headerText)
    alert.setContentText(contentText)

    alert.showAndWait()
  }

  def isCurrentDigit(string: String): Boolean = {
    try{
      val string2 = string.toInt
      if(string2>1023 && string2<49152){
        true
      }else{
        false
      }
    }catch{
      case _ => false
    }
  }

  @FXML
  private def connect: Unit = {
    val myHost = myHostForConnect.getText()
    val myPort = myPortForConnect.getText()//.toInt
    val hostToConnect = connectHost.getText()
    val portToConnect = connectPort.getText()//.toInt

//    val regVaidNumbers: String = """([01]?\d\d?|2[0-4]\d|25[0-5])"""
//
//    val reg: String =  f"^$regVaidNumbers\\.$regVaidNumbers\\.$regVaidNumbers\\.$regVaidNumbers$$"

    if(!myHost.isEmpty && !myPort.isEmpty && !hostToConnect.isEmpty && !portToConnect.isEmpty) {
      if(myHost.matches(reg) && hostToConnect.matches(reg) && isCurrentDigit(myPort) && isCurrentDigit(portToConnect)) {
        if (!(myHost + myPort).equals(hostToConnect + portToConnect)) {
          mainChat.setMyHost(myHost)
          mainChat.setMyPort(myPort.toInt)
          mainChat.setHostToConnect(hostToConnect)
          mainChat.setPortToConnect(portToConnect.toInt)

          mainChat.getInputDataToConnectToClusterStage.close()
        } else {
          val title = "Предупреждение! Некорректные данные"
          val headerText = "Предупреждение! Введены некорректные данные!"
          val contentText = f"Вы не можете подключиться к самому себе. Ваш адрес $myHost:$myPort. Адрес опорного узла, к которому вы пытались подключиться: $hostToConnect:$portToConnect.Если вы хотели создать чат, обратите внимание на поле ввода ниже."
          alert(title, headerText, contentText)

        }
      }else{
        val title = "Предупреждение! Некорректные данные"
        val headerText = "Предупреждение! Введены некорректные данные!"
        val contentText = f"IP-адрес должен иметь вид 0-255.0-255.0-255.0-255., например 127.0.0.1 или 255.255.136.26. Порт должен быть целым положительным числом в пределе 1024—49151"
        alert(title, headerText, contentText)

      }
    }else{
      val title = "Предупреждение! Пустое поле\\поля"
      val headerText = "Предупреждение! Одно или несколько полей ввода не заполнены!!"
      val contentText = "Ни одно из полей ввода не должно быть пустым!"
      alert(title, headerText, contentText)
    }
  }

  @FXML
  private def create: Unit = {
    val myHost = myHostForConnect.getText()
    val myPort = myPortForConnect.getText()

    if(!myHost.isEmpty && !myPort.isEmpty){
      if(isCurrentDigit(myHost) && isCurrentDigit(myPort)){
        mainChat.setMyHost(myHostForConnect.getText())
        mainChat.setMyPort(myPortForConnect.getText().toInt)

        mainChat.getInputDataToConnectToClusterStage.close()
      }else{
        val title = "Предупреждение! Некорректные данные"
        val headerText = "Предупреждение! Введены некорректные данные!"
        val contentText = f"IP-адрес должен иметь вид 0-255.0-255.0-255.0-255., например 127.0.0.1 или 255.255.136.26. Порт должен быть целым положительным числом в пределе 1024—49151"
        alert(title, headerText, contentText)
      }
    }else{
      val title = "Предупреждение! Пустое поле\\поля"
      val headerText = "Предупреждение! Одно или несколько полей ввода не заполнены!!"
      val contentText = "Ни одно из полей ввода не должно быть пустым!"
      alert(title, headerText, contentText)
    }






    println(mainChat.getMyPort)
    println(mainChat.getPortToConnect)
  }



}
