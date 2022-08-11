
//import java.io.IOException
//import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
//import akka.cluster
//import akka.cluster.ClusterEvent.*
//import akka.event.LoggingReceive
//import com.typesafe.config.ConfigFactory
//import akka.cluster.pubsub.*
//
//import scala.collection.mutable.ArrayBuffer
//import scala.io.StdIn.readLine
////import rootGUIController.addLabel
//
//case class send(message: (String, ActorRef))
//
//
//class actor extends Actor with ActorLogging {
//
//
//  val connectionList = new ArrayBuffer[user]() // список подключений
//
//  def getConnectionList: ArrayBuffer[user] = connectionList
//
//  val actorCluster = cluster.Cluster(context.system)
//
//  val mediator = DistributedPubSub(context.system).mediator
//
//  mediator ! DistributedPubSubMediator.Subscribe("publicChat", self)
//  println(self)
//
//  //println(context.system)
//  //actorCluster.join(Address("tcp", "ActorSystem", "127.0.0.1", 2554))
//
//  override def preStart() = {
//    actorCluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember], classOf[MemberUp])
//  }
//
//  override def postStop() = {
//    actorCluster.unsubscribe(self)
//  }
//
//  def receive = LoggingReceive {
//    case s: (String, ActorRef) =>
//      if(!(s._2 == self)){
//        //s._3.addLabel(s._1, s._3.getMessageStory)
//
//        log.info("Got {}", s)
//        println(s._1)
//      }else{print(s)}
//    case "test" => println("It is a test")
//    case send(message: (String, ActorRef))=>
//      mediator ! DistributedPubSubMediator.Publish("publicChat", message)
//    case MemberUp(member) =>
//      log.info(s"[Listener] node is up: $member")
//      connectionList.append(new user("login", member.address.getHost().toString, member.address.getPort().get())) // добавление нового пользователя в список подключений
//      println(member.toString)
//    case UnreachableMember(member) =>
//      log.info(s"[Listener] node is unreachable: $member")
//      println(member.toString)
//    case MemberRemoved(member, prevStatus) =>
//      log.info(s"[Listener] node is removed: $member")
//      println(member.toString)
//    case ev: MemberEvent =>
//      log.info(s"[Listener] event: $ev")
//      println(ev.toString)
//    case d: String => println(d)
//    case _ => println("Unknown message")
//  }
//}
//
//class mainActors{
//  val conf = ConfigFactory.load()
//  //ConfigFactory.systemProperties()
//
//  val system = ActorSystem("ActorSystem", conf)
//
//  val actor1 = system.actorOf(Props[actor](), "Actor1")
//  
//  
//}

