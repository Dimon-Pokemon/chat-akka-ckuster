import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.stage.WindowEvent
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

class  inputDataToConnectToClusterController {

  @FXML
  private var myHostForConnect: TextField = _ // поле ввода ip-адреса узла, который будет подключен к кластеру

  @FXML
  private var myPortForConnect: TextField = _ // поле ввода порта узла, который будет подключен к кластеру

  @FXML
  private var connectHost: TextField = _ // поле ввода ip-адреса узла, к которому будет происходить подключение к кластеру

  @FXML
  private var connectPort: TextField = _ // поле ввода порта узла, к которому будет происходить подключение к кластеру

  @FXML
  private var connectionButton: Button = _

  @FXML
  private var createHost: TextField = _ // поле ввода ip-адреса узла для создания кластера

  @FXML
  private var createPort: TextField = _ // поле ввода порта узла для создания кластера

  @FXML
  private var createButton: Button = _

  private var mainChat: mainChat = _

  
  // Регулярное выражение для определения того, является ли указанная строка ip-адресом
  val regValidNumbers: String = """([01]?\d\d?|2[0-4]\d|25[0-5])"""
  val reg: String =  f"^$regValidNumbers\\.$regValidNumbers\\.$regValidNumbers\\.$regValidNumbers$$"
  

  def setMainChat(mainChat: mainChat): Unit = {
    this.mainChat = mainChat
  }

  /**
   * Метод, определяющий операции, производимые при нажатии на крестик
   * @return EventHandler[WindowEvent]
   */
  def closeEventHandler: EventHandler[WindowEvent] = new EventHandler[WindowEvent](){
    override def handle(event: WindowEvent): Unit = {
      mainChat.getInputDataToConnectToClusterStage.close()
      System.exit(0)
    }
  }


  /**
   * Функция для проверки корректности указанных данных. 
   * Определяет, является ли указанный пользователем порт числом, лежащим в пределах от 1023 до 49152, или нет.
   * @param string возможное число в строковом представлении
   * @return true, если string - число в пределах от 1023 до 49152, иначе false
   */
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

  /**
   * Функционал кнопки для подключения к кластеру
   */
  @FXML
  private def connect: Unit = {
    // получение текста из полей ввода
    val myHost = myHostForConnect.getText()
    val myPort = myPortForConnect.getText()
    val hostToConnect = connectHost.getText()
    val portToConnect = connectPort.getText()
    

    if(!myHost.isEmpty && !myPort.isEmpty && !hostToConnect.isEmpty && !portToConnect.isEmpty) {
      if(myHost.matches(reg) && hostToConnect.matches(reg) && isCurrentDigit(myPort) && isCurrentDigit(portToConnect)) {
        if (!(myHost + myPort).equals(hostToConnect + portToConnect)) {
          // установка новых значений переменных в главном классе для подключения к кластеру
          mainChat.setMyHost(myHost)
          mainChat.setMyPort(myPort.toInt)
          mainChat.setHostToConnect(hostToConnect)
          mainChat.setPortToConnect(portToConnect.toInt)

          mainChat.getInputDataToConnectToClusterStage.close()
        } else {
          dialogs.alert(
            "Предупреждение! Некорректные данные",
            "Предупреждение! Введены некорректные данные!",
            f"Вы не можете подключиться к самому себе. Ваш адрес $myHost:$myPort. Адрес опорного узла, к которому вы пытались подключиться: $hostToConnect:$portToConnect.Если вы хотели создать чат, обратите внимание на поле ввода ниже.")

        }
      }else{
        dialogs.alert(
          "Предупреждение! Некорректные данные",
          "Предупреждение! Введены некорректные данные!",
          f"IP-адрес должен иметь вид 0-255.0-255.0-255.0-255., например 127.0.0.1 или 255.255.136.26. Порт должен быть целым положительным числом в пределе 1024—49151")

      }
    }else{
      dialogs.alert(
        "Предупреждение! Пустое поле\\поля",
        "Предупреждение! Одно или несколько полей ввода не заполнены!!",
        "Ни одно из полей ввода не должно быть пустым!"
      )
    }
  }
  
  /**
   * Функционал кнопки для создания кластера
   */
  @FXML
  private def create: Unit = {
    // получение текста из полей ввода
    val createClusterOnTheHost = createHost.getText()
    val createClusterOnThePort = createPort.getText()

    if(!createClusterOnTheHost.isEmpty && !createClusterOnThePort.isEmpty){
      if(createClusterOnTheHost.matches(reg) && isCurrentDigit(createClusterOnThePort)){
        // установка новых значений переменных в главном классе для подключения к кластеру
        mainChat.setMyHost(createClusterOnTheHost)
        mainChat.setMyPort(createClusterOnThePort.toInt)
        mainChat.setHostToConnect(createClusterOnTheHost)
        mainChat.setPortToConnect(createClusterOnThePort.toInt)

        mainChat.getInputDataToConnectToClusterStage.close()
      }else{
        dialogs.alert(
          "Предупреждение! Некорректные данные",
          "Предупреждение! Введены некорректные данные!",
          f"IP-адрес должен иметь вид 0-255.0-255.0-255.0-255., например 127.0.0.1 или 255.255.136.26. Порт должен быть целым положительным числом в пределе 1024—49151")
      }
    }else{
      dialogs.alert(
        "Предупреждение! Пустое поле\\поля",
        "Предупреждение! Одно или несколько полей ввода не заполнены!!",
        "Ни одно из полей ввода не должно быть пустым!")
    }
  }



}
