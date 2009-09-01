/*================================================================================
Copyright (c) 2009 VMware, Inc. All Rights Reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

 * Neither the name of VMware, Inc. nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior
written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
================================================================================*/

package com.vmware.vim25.ws;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * The XML serialization/de-serialization engine.
 * @author Steve Jin (sjin@vmware.com)
 */

public final class XmlGen
{
   private String PACKAGE_NAME = VIM_PACKAGE_NAME;
   private static final String VIM_PACKAGE_NAME = "com.vmware.vim25";
   private static Namespace XSI = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
   private static QName XSI_TYPE = new QName("type", XSI);
   private static String[] BASIC_TYPES = new String[] {"String", "int", "short", "long", "byte", "boolean", "Boolean", "Calendar", "Integer", "Short", "Long", "Byte"};

   public SoapFaultException parseSoapFault(Element root) throws Exception
   {
      SoapFaultException sfe = new SoapFaultException();

      sfe.setFaultCode(root.elementText("faultcode"));
      sfe.setFaultString(root.elementText("faultstring"));
      sfe.setFaultActor(root.elementText("faultactor"));

      Element detailE = root.element("detail");
      if(detailE != null)
      {
         List<?> subElems = detailE.elements();
         if(subElems.size()!=0)
         {
            Element faultE = (Element) subElems.get(0);
            String faultTypeName = faultE.attributeValue(XSI_TYPE);
            if(faultTypeName!=null)
            {
               sfe.detail = (Throwable) fromXml(getClass(faultTypeName), faultE);
            }
         }
      }
      return sfe;
   }

   public Object fromXML(String type, Element root) throws Exception
   {
      List<Element> subNodes = root.elements();

      if(subNodes.size()==0)
      {
         return null;
      }

      if(type.startsWith("ManagedObjectReference"))
      {
         if(! type.endsWith("[]"))
         {
            Element e = subNodes.get(0);
            return createMOR(e.attributeValue("type"), e.getText());
         }
         else
         {
            Object mos = Array.newInstance(
                  Class.forName(VIM_PACKAGE_NAME + "." + type.substring(0,type.indexOf('['))),
                  subNodes.size());
            for(int i=0; i<subNodes.size(); i++)
            {
               Element elem = subNodes.get(i);
               Array.set(mos, i, createMOR(elem.attributeValue("type"), elem.getText()));
            }
            return mos;
         }
      }
      else if(isBasicType(type))
      {
         String[] vals = new String[subNodes.size()];
         for(int i=0; i<vals.length; i++)
         {
            vals[i] = subNodes.get(i).getText();
         }
         return parseValue(type, vals);
      }
      else if(type.endsWith("[]"))
      { // array type
         String arrayItemTypeName = type.substring(0, type.length()-2);
         Class clazz = getClass(arrayItemTypeName);
         Object ao = Array.newInstance(clazz, subNodes.size());

         for(int i=0; i<subNodes.size(); i++)
         {
            Element e = subNodes.get(i);
            String xsiType = e.attributeValue(XSI_TYPE);
            Object o = fromXml(getClass(xsiType==null? arrayItemTypeName : xsiType), subNodes.get(i));
            Array.set(ao, i, o);
         }
         return ao;
      }
      else
      {
         return fromXml(getClass(type), subNodes.get(0));
      }
   }

   private static boolean isBasicType(String type)
   {
      for(int i=0; i<BASIC_TYPES.length; i++)
      {
         if(type.startsWith(BASIC_TYPES[i]))
            return true;
      }
      return false;
   }

   private final static Set<String> PRIMITIVE_DATA_TYPES = new HashSet<String>();
   static
   {
      PRIMITIVE_DATA_TYPES.add("int");
      PRIMITIVE_DATA_TYPES.add("boolean");
      PRIMITIVE_DATA_TYPES.add("short");
      PRIMITIVE_DATA_TYPES.add("byte");
      PRIMITIVE_DATA_TYPES.add("long");
   }

   private final Map<String, Class> PkgClasses = new HashMap<String, Class>();

   private Class getClass(String type)
   {
      if(PkgClasses.containsKey(type))
      {
         return PkgClasses.get(type);
      }
      else
      {
         try
         {
            Class clazz = null;
            if(! type.endsWith("[]"))
            {
               /*
                * Check to see if this name is fully qualified. If it is not
                * attempt to locate the type in the VIM package. Otherwise,
                * attempt to locate the type in the set package name.
                */
               try {
                  clazz = Class.forName(type);
               } catch (ClassNotFoundException e) {
                  /*
                   * Check to see if this class exists in the VIM package.
                   */
                  try {
                     clazz = Class.forName(VIM_PACKAGE_NAME + "." + type);
                  } catch (ClassNotFoundException cnfe) {
                     /*
                      * Object was not in the VIM package. Use configured
                      * package instead.
                      */
                     if (PACKAGE_NAME.equals(VIM_PACKAGE_NAME)) {
                        throw cnfe;
                     }
                     clazz = Class.forName(PACKAGE_NAME + "." + type);
                  }
               }
            }
            else
            {
               String compType = type.substring(0, type.length()-2);
               clazz = Array.newInstance(getClass(compType), 0).getClass();
            }
            PkgClasses.put(type, clazz);
            return clazz;
         } catch (ClassNotFoundException cnfe)
         {
            cnfe.printStackTrace();
            return null;
         }
      }
   }

   /** Handle single VIM Data Object except MOR */
   private Object fromXml(Class clazz, Element node) throws Exception
   {
      Object obj = clazz.newInstance();

      List<Element> subNodes = node.elements();
      int sizeOfSubNodes = subNodes.size();

      for (int i=0; i<sizeOfSubNodes; i++)
      {
         Element e = subNodes.get(i);
         String tagName = e.getName();

         Field field = null;
         if(PRIMITIVE_DATA_TYPES.contains(tagName))
         {
            field = clazz.getField("_" + tagName);
         }
         else
         {
            field = clazz.getField(tagName);
         }

         Class fType = field.getType();
         boolean isFieldArray = fType.isArray();
         //if field is an array, adjust it to the component type
         if(isFieldArray)
         {
            fType = fType.getComponentType();
         }

         Class fRealType = fType;
         String xsiType = e.attributeValue(XSI_TYPE);
         if(xsiType!=null && (!xsiType.startsWith("xsd:")))
         {
            fRealType = getClass(xsiType);
         }

         if(fRealType == getClass("ManagedObjectReference"))
         { // MOR
            if(isFieldArray)
            {
               int sizeOfFieldArray = getNumberOfSameTags(subNodes, sizeOfSubNodes, i, tagName);
               Object mos = Array.newInstance(fRealType, sizeOfFieldArray);
               for(int j=0; j<sizeOfFieldArray; j++)
               {
                  Element elem = subNodes.get(j+i);
                  Array.set(mos, j, fRealType.cast(createMOR(elem.attributeValue("type"), elem.getText())));
               }
               field.set(obj, mos);
               i = i + sizeOfFieldArray -1;
            }
            else
            {
               field.set(obj, createMOR(e.attributeValue("type"), e.getText()));
            }
         }
         else if(fRealType.isEnum())
         { // Enum type
            if(!isFieldArray)
            {
               Object fo = Enum.valueOf(fRealType, e.getText());
               field.set(obj, fo);
            }
            else
            {
               int sizeOfFieldArray = getNumberOfSameTags(subNodes, sizeOfSubNodes, i, tagName);
               Object ao = Array.newInstance(fRealType, sizeOfFieldArray);
               for(int j=0; j<sizeOfFieldArray; j++)
               {
                  String enumStr = (subNodes.get(j+i)).getText();
                  Array.set(ao, j, Enum.valueOf(fRealType, enumStr));
               }
               field.set(obj, ao);
               i = i + sizeOfFieldArray -1;
            }
         }
         else if (fRealType.getPackage() == Package.getPackage(PACKAGE_NAME)
               || fRealType.getPackage() == Package.getPackage(VIM_PACKAGE_NAME))
         { //VIM type
            if(isFieldArray)
            {
               int sizeOfFieldArray = getNumberOfSameTags(subNodes, sizeOfSubNodes, i, tagName);
               Object ao = Array.newInstance(fType, sizeOfFieldArray);
               String fGenericType = fType.getSimpleName();
               for(int j=0; j<sizeOfFieldArray; j++)
               {
                  Element elem = subNodes.get(j+i);
                  String elemXsiType = elem.attributeValue(XSI_TYPE);
                  String elemType = elemXsiType!=null? elemXsiType : fGenericType;
                  Object o = fromXml(getClass(elemType), elem);
                  Array.set(ao, j, o);
               }
               field.set(obj, ao);
               i = i + sizeOfFieldArray -1;
            }
            else
            { // single VIM
               Object o = fromXml(fRealType, e);
               field.set(obj, o);
            }
         }
         else
         { //basic data type
            if(isFieldArray)
            {
               int sizeOfFieldArray = getNumberOfSameTags(subNodes, sizeOfSubNodes, i, tagName);

               String[] values = new String[sizeOfFieldArray];
               for(int j=0; j < values.length; j++)
               {
                  values[j] = (subNodes.get(j+i)).getText();
               }

               String fTrueType = null;
               if(xsiType!=null)
               {
                  fTrueType = xsiType.substring("xsd:".length()) + "[]";
               }
               else
               {
                  fTrueType = fRealType.getSimpleName();
                  if(!fTrueType.endsWith("[]"))
                  {
                     fTrueType = fTrueType + "[]";
                  }
               }
               setArrayFieldValue(field, obj, fTrueType, values);
               i = i + sizeOfFieldArray -1;
            }
            else
            {
               if(xsiType!=null)
               {
                  xsiType = xsiType.substring("xsd:".length());
                  setFieldValue(field, obj, xsiType, e.getText());
               }
               else
               {
                  setFieldValue(field, obj, fRealType.getSimpleName(), e.getText());
               }
            }
         }
      }
      return obj;
   }

   private final static int getNumberOfSameTags(List<Element> subNodes, int sizeOfSubNodes, int from, String tagName)
   {
      int numOfTags = 1;
      for(int j=from+1; j<sizeOfSubNodes; j++)
      {
         if(subNodes.get(j).getName().equals(tagName))
         {
            numOfTags ++;
         }
         else
         {
            break;
         }
      }
      return numOfTags;
   }

   private Object createMOR(String type, String value)
   {
      try {
         Class clazz = Class.forName(VIM_PACKAGE_NAME + ".ManagedObjectReference");
         Object mor = clazz.newInstance();
         clazz.getMethod("set_value", Class.forName("java.lang.String")).invoke(mor, value);
         clazz.getMethod("setType", Class.forName("java.lang.String")).invoke(mor, type);
         return mor;
      } catch(Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   private static byte[] parseByteArray(String[] values)
   {
      byte[] bs = new byte[values.length];
      for(int i=0; i< bs.length; i++)
      {
         bs[i] = Byte.parseByte(values[i]);
      }
      return bs;
   }

   private static long[] parseLongArray(String[] values)
   {
      long[] ls = new long[values.length];
      for(int i=0; i< ls.length; i++)
      {
         ls[i] = Long.parseLong(values[i]);
      }
      return ls;
   }

   private static short[] parseShortArray(String[] values)
   {
      short[] ss = new short[values.length];
      for(int i=0; i< ss.length; i++)
      {
         ss[i] = Short.parseShort(values[i]);
      }
      return ss;
   }

   private static int[] parseIntArray(String[] values)
   {
      int[] is = new int[values.length];
      for(int i=0; i<is.length; i++)
      {
         is[i] = Integer.parseInt(values[i]);
      }
      return is;
   }

   private static boolean[] parseBooleanArray(String[] values)
   {
      boolean[] bs = new boolean[values.length];
      for(int i=0; i< bs.length; i++)
      {
         bs[i] = Boolean.parseBoolean(values[i]);
      }
      return bs;
   }

   private static Object parseValue(String type, String[] values)
   {
      if("String".equals(type) || "string".equals(type))
      {
         return values[0];
      }
      else if("String[]".equals(type))
      {
         return values;
      }
      else if("int".equals(type))
      {
         return new Integer(values[0]);
      }
      else if("int[]".equals(type))
      {
         return parseIntArray(values);
      }
      else if("short".equals(type))
      {
         return new Short(values[0]);
      }
      else if("short[]".equals(type))
      {
         return parseShortArray(values);
      }
      else if("byte".equals(type))
      {
         return new Byte(values[0]);
      }
      else if("byte[]".equals(type))
      {
         return parseByteArray(values);
      }
      else if("long".equals(type))
      {
         return new Long(values[0]);
      }
      else if("long[]".equals(type))
      {
         return parseLongArray(values);
      }
      else if("boolean".equals(type))
      {
         return new Boolean(values[0]);
      }
      else if("boolean[]".equals(type))
      {
         return parseBooleanArray(values);
      }
      else if("Calendar".equals(type)  || "dateTime".equals(type))
      {
         Calendar cal = DatatypeConverter.parseTime(values[0]);
         return cal;
      }
      else
      {
         throw new RuntimeException("Unexpected Type@setField: " + type + values[0]);
      }
   }

   private final static void setFieldValue(Field f, Object obj, String type, String value) throws IllegalArgumentException, IllegalAccessException
   {
      if("String".equals(type) || "string".equals(type))
      {
         f.set(obj, value);
      }
      else if("int".equals(type))
      {
         f.set(obj, Integer.parseInt(value));
      }
      else if("Integer".equals(type))
      {
         f.set(obj, new Integer(value));
      }
      else if("short".equals(type))
      {
         f.set(obj, Short.parseShort(value));
      }
      else if("Short".equals(type))
      {
         f.set(obj, new Short(value));
      }
      else if("byte".equals(type))
      {
         f.set(obj, Byte.parseByte(value));
      }
      else if("Byte".equals(type))
      {
         f.set(obj, new Byte(value));
      }
      else if("long".equals(type))
      {
         f.set(obj, Long.parseLong(value));
      }
      else if("Long".equals(type))
      {
         f.set(obj, new Long(value));
      }
      else if("boolean".equals(type))
      {
         f.set(obj, Boolean.parseBoolean(value));
      }
      else if("Boolean".equals(type))
      {
         f.set(obj, new Boolean(value));
      }
      else if("Calendar".equals(type) || "dateTime".equals(type))
      {
         Calendar cal = DatatypeConverter.parseTime(value);
         f.set(obj, cal);
      }
      else
      {
         throw new RuntimeException("Unexpected Type@setField: " + f.getType().getCanonicalName() + f.getName());
      }
   }

   private static void setArrayFieldValue(Field f, Object obj, String type, String[] values) throws IllegalArgumentException, IllegalAccessException
   {
      if("String[]".equals(type) || "string[]".equals(type))
      {
         f.set(obj, values);
      }
      else if("int[]".equals(type))
      {
         f.set(obj, parseIntArray(values));
      }
      else if("short[]".equals(type))
      {
         f.set(obj, parseShortArray(values));
      }
      else if("byte[]".equals(type))
      {
         f.set(obj, parseByteArray(values));
      }
      else if("long[]".equals(type))
      {
         f.set(obj, parseLongArray(values));
      }
      else if("boolean[]".equals(type))
      {
         f.set(obj, parseBooleanArray(values));
      }
      else
      {
         throw new RuntimeException("Unexpected Type@setField: " + f.getType().getCanonicalName() + f.getName());
      }
   }

   public String toXML(String tag, String type, Object obj)
   {
      if(obj==null)
      {
         return "";
      }
      StringBuffer sb = new StringBuffer();
      if(isBasicType(type))
      {
         toXML(sb, tag, obj.getClass(), obj);
      }
      else
      {
         Class clazz = getClass(type);
         toXML(sb, tag, clazz, obj);
      }
      return sb.toString();
   }

   private void toXML(StringBuffer sb, String tagName, Class type, Object obj)
   {
      Class<?> clazz = obj.getClass();
      try{
         if(clazz.isArray())
         {
            if(obj.getClass() == (new int[]{}.getClass()))
            {
               int[] objs = (int[]) obj;
               for(int i=0; i<objs.length; i++)
               {
                  sb.append("<" + tagName +">");
                  sb.append(objs[i]);
                  sb.append("</" + tagName + ">");
               }
            }
            else
            {
               Object[] objs = (Object[]) obj;
               for(int i=0; i<objs.length; i++)
               {
                  toXML(sb, tagName, type.getComponentType(), objs[i]);
               }
            }
         }

         // from now on, no array type
         else if(clazz == Class.forName(VIM_PACKAGE_NAME + ".ManagedObjectReference"))
         { //MOR]
            try {
               sb.append("<" + tagName + " type=\""
                     + clazz.getMethod("getType", null).invoke(obj, null)
                     + "\">");
               sb.append(clazz.getMethod("get_value", null).invoke(obj, null));
            } catch (IllegalAccessException iae) {
               iae.printStackTrace();
            }
            catch (InvocationTargetException ite) {
               ite.printStackTrace();
            }
            catch (NoSuchMethodException nsme) {
               nsme.printStackTrace();
            }
            sb.append("</" + tagName + ">");
         }
         else if(clazz.getCanonicalName().startsWith("java.lang")) //basic data type
         {
            if(clazz!=type)
            {
               sb.append("<" + tagName + " xsi:type=\"" + getXSIType(obj) + "\">");
            }
            else
            {
               sb.append("<" + tagName +">");
            }
            sb.append(obj);
            sb.append("</" + tagName + ">");
         }
         else if(clazz.isEnum()) //enum data type
         {
            sb.append("<" + tagName +">" + obj + "</" + tagName + ">");
         }
         else if (obj instanceof Calendar)
         {
            sb.append("<" + tagName + " xsi:type=\"xsd:dateTime\">" + DatatypeConverter.printDateTime((Calendar)obj) + "</" + tagName + ">");
         }
         else
         { // VIM type
            if(clazz==type)
            {
               sb.append("<" + tagName + ">");
            }
            else
            {
               String nameSpaceType = clazz.getSimpleName();
               sb.append("<" + tagName + " xsi:type=\"" + nameSpaceType + "\">");
            }

            Field[] fields = getAllFields(clazz);

            for(int i=0; i<fields.length; i++)
            {
               Field f = fields[i];
               String fName = f.getName();

               Object value  = null;
               try
               {
                  value = f.get(obj);
               } catch (IllegalAccessException iae)
               {
                  iae.printStackTrace();
               }
               if(value==null)
               {
                  continue;
               }

               Class<?> fType = f.getType();
               toXML(sb, fName, fType, value);
            }
            sb.append("</" + tagName + ">");
         }
      } catch (ClassNotFoundException cnfe) {
         cnfe.printStackTrace();
      }
   }

   private String getXSIType(Object obj)
   {
      Class type = obj.getClass();
      if(java.lang.Integer.class == type)
      {
         return "xsd:int";
      }
      else if(java.lang.Long.class == type)
      {
         return "xsd:long";
      }
      else if(java.lang.Boolean.class == type)
      {
         return "xsd:boolean";
      }
      else if(java.lang.Short.class == type)
      {
         return "xsd:short";
      }
      else if(java.lang.Float.class == type)
      {
         return "xsd:float";
      }
      else if(java.lang.String.class == type)
      {
         return "xsd:string";
      }
      else if(java.lang.Byte.class == type)
      {
         return "xsd:byte";
      }
      else if(obj instanceof java.util.Calendar)
      {
         return "xsd:dateTime";
      }
      else
      {
         throw new RuntimeException("Unknow data type during serialization:" + type);
      }
   }

   private static Field[] getAllFields(Class<?> c)
   {
      Field[] fields = null;
      ArrayList<Field> al = new ArrayList<Field>();
      getAllFields(c, al);
      fields = new Field[al.size()];
      al.toArray(fields);
      return fields;
   }

   private static void getAllFields(Class<?> clazz, ArrayList<Field> al)
   {
      Class<?> supClazz = clazz.getSuperclass();
      if(supClazz!=null)
      {
         getAllFields(supClazz, al);
      }
      Field[] fs = clazz.getDeclaredFields();
      for(int i=0; i<fs.length; i++)
      {
         al.add(fs[i]);
      }
   }

   public void setPkgName(String pkgName) {
      this.PACKAGE_NAME = pkgName;
   }
}