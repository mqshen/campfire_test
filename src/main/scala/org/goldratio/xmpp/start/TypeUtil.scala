package org.goldratio.xmpp.start

import scala.xml.{Elem, Node}
import scala.collection.JavaConversions._
import java.lang.reflect.{Constructor, Method}

/**
 * Created by GoldRatio on 4/11/14.
 */
object TypeUtil {

  def call(methodName: String, obj: AnyRef, args: Array[AnyRef]) = {
    val argTypes = args.map(_.getClass)

    val methods = obj.getClass.getMethods

    def parseOpt(method: Method): Option[Method] = {
      if(method.getName == methodName)
        Some(method)
      else
        None
    }
    val found = methods.toList.view.flatMap(parseOpt).headOption
    if(found.isDefined) {
      val method = found.get
      method.invoke(obj, args:_*)
    }
  }

  def construct(oClass: Class[_], args: Array[AnyRef]): AnyRef = {
    val constructors = oClass.getConstructors

    def parseOpt(constructor: Constructor[_]): Option[Constructor[_]] = {
      if(constructor.getParameterTypes.length == args.length)
        Some(constructor)
      else
        None
    }
    val found = constructors.toList.view.flatMap(parseOpt).headOption
    if(found.isDefined) {
      val method = found.get
      try {
        method.newInstance(args:_*).asInstanceOf[AnyRef]
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
        Nil
      }
    }
    else
      Nil
  }

}
