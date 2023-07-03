import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

object dialogs {
  /**
   * Функция для создания предупреждения
   *
   * @param title       заголовок предупреждения
   * @param headerText  тема
   * @param contentText подробности
   */
  def alert(title: String, headerText: String, contentText: String): Unit = {
    val alert: Alert = new Alert(AlertType.WARNING)
    alert.setTitle(title)
    alert.setHeaderText(headerText)
    alert.setContentText(contentText)

    alert.showAndWait()
  }
}
