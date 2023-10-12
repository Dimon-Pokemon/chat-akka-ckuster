import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

/**
 * Класс, представляющий пользователя (user).
 * Содерижт:
 *     - имя
 *     - ip адрес
 *     - порт
 *     - ссылку на актора, с которым связан данный пользователь (на одного пользователя один актор)
 */
class user {
  private var name: StringProperty = null
  private var ip: StringProperty = null
  private var port: IntegerProperty = null
  private var actorReference: StringProperty = null
  

  def this(name: String, ip: String, port: Integer, actorReference: String) = {
    this()
    this.name = new SimpleStringProperty(name)
    this.ip = new SimpleStringProperty(ip)
    this.port = new SimpleIntegerProperty(port)
    this.actorReference = new SimpleStringProperty(actorReference)
  }

  def nameProperty: StringProperty = name

  def getName: String = name.get()

  def name_ (newValue: String): Unit = {
    this.name.set(newValue)
  }


  def ipProperty: StringProperty = ip

  def getIp: String = ip.get()

  def ip_ (newValue: String): Unit = {
    this.ip.set(newValue)
  }


  def portProperty: IntegerProperty = port

  def getPort: Integer = port.get()

  def port_ (newValue: Integer): Unit = {
    this.port.set(newValue)
  }
  
  def actorReferenceProperty: StringProperty = actorReference
  
  def getActorReference: String = actorReference.get
  
  def actorReference_ (newValue: String): Unit = {
    this.actorReference.set(newValue)
  }
}
