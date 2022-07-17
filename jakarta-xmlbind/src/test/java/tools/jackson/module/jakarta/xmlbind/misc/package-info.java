/**
 * Package info can be used to add "package annotations", so here we are...
 */
@jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
  @jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter(
    type = javax.xml.namespace.QName.class,
    value = tools.jackson.module.jakarta.xmlbind.introspect.TestJaxbAnnotationIntrospector.QNameAdapter.class
  )
})
package tools.jackson.module.jakarta.xmlbind.misc;

