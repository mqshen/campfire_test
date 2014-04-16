package org.goldratio.xmpp.start

import scala.xml.{Elem, Node}
import scala.collection.mutable
import java.net.{UnknownHostException, InetAddress, MalformedURLException, URL}
import java.lang.reflect.InvocationTargetException
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.HashSet
import java.security.cert.{X509Certificate, Certificate}

/**
 * Created by GoldRatio on 4/10/14.
 */
class JettyXmlConfiguration(configuration: XmlConfiguration) {
  val __supportedCollections = Array[Class[_]]()

  def configure(root: Node) {
    val oClass = nodeClass(root)
    val id = (root \ "@id").text

    val obj = if(!configuration.idMap.contains(id)) {
      val args = root \ "Arg"
      if(args.size > 0) {
        //val namedArgMap = new mutable.HashMap[String, AnyRef]()
        val arguments = new Array[AnyRef](args.size)
        var index = 0
        (0 until args.size).foreach { i =>
          arguments(0)
          arguments(i) = getValue(Nil , args(i))
        }
        TypeUtil.construct(oClass, arguments)
        /*
        val argTypes = arguments.map(_.getClass)
        val constructor = oClass.getConstructor(argTypes:_*)
        constructor.newInstance(arguments:_*).asInstanceOf[AnyRef]
        */
      }
      else {
        val constructor = oClass.getConstructor()
        constructor.newInstance().asInstanceOf[AnyRef]
      }
    }
    else {
      configuration.idMap.get(id)
    }
    configure(obj, root, 0)
  }

  def configure(obj: AnyRef, node: Node, i: Int) {
    val id = (node \ "@id").text
    configuration.idMap.put(id, obj)
    val children = node.child
    children.foreach { child =>
      itemValue(obj, child)
    }
  }


  def getValue(obj: AnyRef, node: Node ): AnyRef =  {

    val nodeType = (node \ "@type").text

    val refNode = (node \ "Ref")
    val value:AnyRef =  if (refNode.size > 0) {
      val ref = (refNode \ "@refid").text
      val result = configuration.idMap.get(ref)
      if(result.isDefined)
        result.get
      else
        Nil
    }
    else {
      // handle trivial case
      if (node.size == 0) {
        if ("String".equals(nodeType))
          ""
        else
          Nil
      }
      else {
        val children = node.child
        if(children.size > 0) {
          def parseOpt(node: Node): Option[Node] = {
            if(node.isInstanceOf[Elem])
              Some(node)
            else
              None
          }
          val found = children.view.flatMap(parseOpt).headOption
          if(found.isDefined)
            itemValue(obj, found.get)
          else
            children(0).text
        }
        else if (nodeType == null || !"String".equals(nodeType)) {
          Nil
        }
        else {
          Nil
        }
      }
    }
    value
  }

  def call(obj: AnyRef, node: Node):AnyRef = {
    val id = (node \ "@id").text

    val children = node.child
    val args = new Array[AnyRef](children.size)

    (0 until children.size).foreach { i =>
      val o = children(i)
      args(i) = getValue(obj, o)
    }

    val methodName = (node \ "@name").text

//    val argTypes = args.map(_.getClass)
//    val method = obj.getClass.getMethod(methodName, argTypes:_*)
//    method.invoke(obj, args:_*)
    TypeUtil.call(methodName, obj, args)

    configuration.idMap.put(id, obj)

    obj
  }

  def newObject(obj: AnyRef, node: Node): AnyRef = {
    val id = (node \ "@id").text
    val oClass = nodeClass(node)
    val argsNodes = node \ "Arg"

    val n =  if(argsNodes.size > 0) {
      val args = new Array[AnyRef](argsNodes.size)
      (0 until argsNodes.size).foreach { i =>
        val o = argsNodes(i)
        args(i) = getValue(obj, o)
      }

      TypeUtil.construct(oClass, args)
    }
    else {
      val constructor = oClass.getConstructor()
      constructor.newInstance()
    }
    val value = n.asInstanceOf[AnyRef]
    if(!id.isEmpty)
      configuration.idMap.put(id, value)
    val callsNode = node \ "Call"
    callsNode.foreach { call =>
      itemValue(value, call)
    }
    value

  }

  def itemValue(obj: AnyRef, node: Node) = {
    // String value
    val tag = node.label
    if ("Call".equals(tag))
      call(obj,node)
    else if ("New".equals(tag))
      newObject(obj,node)
    else if ("SystemProperty".equals(tag)) {
      val name = (node \ "@name").text
      val defaultValue = (node \ "@default").text
      System.getProperty(name,defaultValue)
    }
    else if ("Env".equals(tag)) {
      val name = (node \ "@name").text
      val defaultValue = (node \ "@default").text
      val value = System.getenv(name)
      if(value==null)
        defaultValue
      else
        value
    }
    else
      Nil
    //    if ("Get".equals(tag))
    //      return get(obj,node)
    //    if ("New".equals(tag))
    //      return newObj(obj,node)
    //    if ("Ref".equals(tag))
    //      return refObj(obj,node)
    //    if ("Array".equals(tag))
    //      return newArray(obj,node)
    //    if ("Map".equals(tag))
    //      return newMap(obj,node)
    //    if ("Property".equals(tag))
    //      return propertyObj(node)
  }

  def nodeClass(node: Node ): Class[_] = {
    val className = (node \ "@class").text
    Loader.loadClass(classOf[XmlConfiguration],className)
  }

}

object Loader {
  def loadClass(loadClass: Class[_],name: String ): Class[_] = {
      var ex:Exception = null
      var c:Class[_] = null
      val context_loader=Thread.currentThread().getContextClassLoader()
      if (context_loader!=null ) {
        try {
          c = context_loader.loadClass(name)
        }
        catch {
          case e: ClassNotFoundException =>
            ex = e
        }
      }

      if (c==null && loadClass!=null) {
        val load_loader=loadClass.getClassLoader()
        if (load_loader!=null && load_loader!=context_loader) {
          try {
            c = load_loader.loadClass(name);
          }
          catch  {
            case e: ClassNotFoundException =>
              if(ex==null)
                ex=e
          }
        }
      }

      if (c==null) {
        try {
          c = Class.forName(name)
        }
        catch  {
          case e: ClassNotFoundException =>
          if(ex!=null)
            throw ex
          throw e
        }
      }
      c
    }
}