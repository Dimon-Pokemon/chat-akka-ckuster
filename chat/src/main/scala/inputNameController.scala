import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}



class inputNameController {

  @FXML
  var userName: TextField = null

  @FXML
  var button: Button = null

  private var mainChat: mainChat = null

  def setMainChat(mainChat: mainChat): Unit = {
    this.mainChat = mainChat
  }

  @FXML
  def setUserName: Unit = {
    val textUserName = userName.getText
    //println("setUserNameActivated") // отладочный вывод

    if(!textUserName.isEmpty){
      println("ok")
      mainChat.userName = textUserName
      mainChat.getSetNameUserStage.close()

    }
  }

}
