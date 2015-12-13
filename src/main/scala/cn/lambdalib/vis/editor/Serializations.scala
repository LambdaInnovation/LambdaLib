package cn.lambdalib.vis.editor

import java.lang.reflect.Modifier
import org.w3c.dom.{Document, Node}

import scala.reflect.ClassTag

private object ReflectionHelper {

  def getExposedFields(c: Class[_]) = c.getFields.filter(f => {
    val anno = f.getAnnotation(classOf[VisProperty])
    val modifiers = f.getModifiers
    (anno == null || !anno.exclude()) &&
    (modifiers & Modifier.FINAL) == 0 && (modifiers & Modifier.PUBLIC) != 0 && (modifiers & Modifier.STATIC) == 0
  })

}
import ReflectionHelper._

/**
  * Dom conversion used to read and write XML documents.
  */
class DOMConversion {
  type DOMElement = org.w3c.dom.Element

  type Forwarder = (AnyRef, Node) => Any
  type Backwarder[T] = (Class[T], Node) => T

  private var forwarders = List[(AnyRef => Boolean, Forwarder)]()
  private var backwarders = Map[Class[_], Backwarder[_]]()

  def addForward(cond: AnyRef => Boolean, forwarder: Forwarder) = forwarders = (cond, forwarder) :: forwarders
  def addForwardType(forwarder:Forwarder, classes: Class[_]*) =
    addForward(obj => classes.exists(_.isInstance(obj)), forwarder)
  def addBackward[T](backwarder: Backwarder[T])(implicit evidence: ClassTag[T]) =
    backwarders = backwarders updated (evidence.runtimeClass, backwarder)

  /**
    * ? + Document -> Node
    */
  def convertTo(obj: AnyRef, name: String)(implicit doc: Document): Node = {
    forwarders.find{ case (cond, _) => cond(obj) } match {
      case Some(f) =>
        val ret = doc.createElement(name)
        f._2(obj, ret)
        ret
      case _ =>
        forwardDefault(obj, name)
    }
  }

  /**
    * Node -> ?
    */
  def convertFrom[T](klass: Class[T], node: Node) = {
    backwarders.get(klass) match {
      case Some(bw) => bw.asInstanceOf[Backwarder[T]](klass, node)
      case None => backwardDefault(klass, node)
    }
  }

  def forwardDefault(obj: AnyRef, name: String)(implicit document: Document): Node = {
    val ret: DOMElement = document.createElement(name)
    val fields = getExposedFields(obj.getClass).toList
    for (f <- fields) {
      Option(f.get(obj)) match {
        case Some(x) =>
          ret.appendChild(convertTo(x, f.getName))
        case _ =>
          val nullNode = document.createElement(f.getName)
          nullNode.setAttribute("isNull", "true")
          ret.appendChild(nullNode)
      }
    }
    ret
  }

  def backwardDefault[T, U<:T](klass: Class[U], src: Node):T = {
    if (klass.isEnum) {
      val content = src.getTextContent
      klass.getEnumConstants.find(_.toString == content) match {
        case Some(e) => e.asInstanceOf[T]
      }
    } else {
      val ret = klass.newInstance
      val fields = getExposedFields(klass).toList
      val childs = src.getChildNodes
      // Loop through all the fields and fetch corresponding elements from document
      (0 until childs.getLength).map(childs.item)
        .foreach({ case elem: DOMElement =>
          fields.find(f => elem.getNodeName == f.getName) match {
            case Some(field) =>
              val attr = elem.getAttribute("isNull")
              field.set(ret, if(!attr.isEmpty) null else convertFrom(field.getType, elem))
            case _ =>
          }
      })
      ret
    }
  }

  private def init() {
    def addText(node: Node, content: String) =
      node.appendChild(node.getOwnerDocument.createTextNode(content))

    // Primitive types
    addForwardType((obj, node) => addText(node, obj.toString),
      classOf[Int], classOf[Integer],
      classOf[Float], classOf[java.lang.Float],
      classOf[Double], classOf[java.lang.Double],
      classOf[Boolean], classOf[java.lang.Boolean],
      classOf[String])
    addForward(_.getClass.isEnum, (obj, node) => addText(node, obj.toString))

    def bw[T](parseMethod: String => T)(implicit evidence: ClassTag[T]) =
      addBackward[T]((_, n) => parseMethod(n.getTextContent))

    // Literal value parsings
    bw(Integer.parseInt)
    bw(Integer.valueOf)
    bw(java.lang.Float.parseFloat)
    bw(java.lang.Float.valueOf)
    bw(java.lang.Double.parseDouble)
    bw(java.lang.Double.valueOf)
    bw(java.lang.Boolean.parseBoolean)
    bw(java.lang.Boolean.valueOf)
    bw(String.valueOf)
  }

  init()
}

