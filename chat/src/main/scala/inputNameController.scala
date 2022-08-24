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

  private var mainChat: mainChat = null

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
      System.exit(0)

    }
  }

  def sendUserNameAfterEnterPressed: EventHandler[KeyEvent] = new EventHandler[KeyEvent]:
    override def handle(event: KeyEvent): Unit = {
      if(event.getCode.toString.equals("ENTER")) setUserName
    }
  
  @FXML
  protected def setUserName: Unit = {
    val textUserName = userName.getText
    //println("setUserNameActivated") // отладочный вывод

    if(!textUserName.isEmpty){
      mainChat.userName = textUserName
      mainChat.getInputNameStage.close()

    }
  }

}
