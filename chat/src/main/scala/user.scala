import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

class user {
  private var login: StringProperty = null
  private var ip: StringProperty = null
  private var port: IntegerProperty = null

  def this(login: String, ip: String, port: Integer) = {
    this()
    this.login = new SimpleStringProperty(login)
    this.ip = new SimpleStringProperty(ip)
    this.port = new SimpleIntegerProperty(port)
  }

  def loginProperty: StringProperty = login

  def getLogin: String = login.get()

  def login_ (newValue: String): Unit = {
    this.login.set(newValue)
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
}
