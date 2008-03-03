/**
 * 
 */
package edu.internet2.middleware.grouper.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.mchange.util.AssertException;

/**
 * utility methods for grouper
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GrouperUtil {
  
  /**
   * split a string based on a separator into an array, and trim each entry (see
   * the Commons Util StringUtils.trim() for more details)
   * 
   * @param input
   *          is the delimited input to split and trim
   * @param separator
   *          is what to split on
   * 
   * @return the array of items after split and trimmed, or null if input is null.  will be trimmed to empty
   */
  public static String[] splitTrim(String input, String separator) {
    if (input == null) {
      return null;
    }

    //first split
    String[] items = StringUtils.split(input, separator);

    //then trim
    for (int i = 0; (items != null) && (i < items.length); i++) {
      items[i] = StringUtils.trim(items[i]);
    }

    //return the array
    return items;
  }

  /**
   * escape url chars (e.g. a # is %23)
   * @param string input
   * @return the encoded string
   */
  public static String escapeUrlEncode(String string) {
    String result = null;
    try {
      result = URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("UTF-8 not supported", ex);
    }
    return result;
  }
  
  /**
   * unescape url chars (e.g. a space is %20)
   * @param string input
   * @return the encoded string
   */
  public static String escapeUrlDecode(String string) {
    String result = null;
    try {
      result = URLDecoder.decode(string, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("UTF-8 not supported", ex);
    }
    return result;
  }

  /**
   * make sure a list is non null.  If null, then return an empty list
   * @param <T>
   * @param list
   * @return the list or empty list if null
   */
  public static <T> List<T> nonNull(List<T> list) {
    return list == null ? new ArrayList<T>() : list;
  }
  
  /**
   * return a list of objects from varargs.
   * 
   * @param <T>
   *            template type of the objects
   * @param objects
   * @return the list
   */
  public static <T> List<T> toList(T... objects) {

    List<T> result = new ArrayList<T>();
    for (T object : objects) {
      result.add(object);
    }
    return result;
  }

  /**
   * cache separator
   */
  private static final String CACHE_SEPARATOR = "__";

  /**
   * If false, throw an assertException, and give a reason
   * 
   * @param isTrue
   * @param reason
   * @throws AssertException
   */
  public static void assertion(boolean isTrue, String reason) {
    if (!isTrue) {
      throw new RuntimeException(reason);
    }

  }

  /**
   * use the field cache, expire every day (just to be sure no leaks)
   */
  private static ExpirableCache<String, Set<Field>> fieldSetCache = new ExpirableCache<String, Set<Field>>(
      60 * 24);

  /**
   * make a cache with max size to cache declared methods
   */
  private static Map<Class, Method[]> declaredMethodsCache = new HashMap<Class, Method[]>() {
    /**
     * @see java.util.HashMap#put(java.lang.Object,java.lang.Object)
     */
    @Override
    public Method[] put(Class key, Method[] value) {
      if (this.size() > 2000) {
        return null;
      }
      return super.put(key, value);
    }
  };

  /**
   * assign data to a field
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @param dataToAssign
   *            data
   * @param callOnSupers
   *            if static and method not exists, try on supers
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @param typeCast
   *            true if we should typecast
   * @param annotationWithValueOverride
   *            annotation with value of override
   */
  public static void assignField(Class theClass, Object invokeOn,
      String fieldName, Object dataToAssign, boolean callOnSupers,
      boolean overrideSecurity, boolean typeCast,
      Class<? extends Annotation> annotationWithValueOverride) {
    if (theClass == null && invokeOn != null) {
      theClass = invokeOn.getClass();
    }
    Field field = field(theClass, fieldName, callOnSupers, true);
    assignField(field, invokeOn, dataToAssign, overrideSecurity, typeCast,
        annotationWithValueOverride);
  }

  /**
   * assign data to a field. Will find the field in superclasses, will
   * typecast, and will override security (private, protected, etc)
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @param dataToAssign
   *            data
   * @param annotationWithValueOverride
   *            annotation with value of override
   */
  public static void assignField(Class theClass, Object invokeOn,
      String fieldName, Object dataToAssign,
      Class<? extends Annotation> annotationWithValueOverride) {
    assignField(theClass, invokeOn, fieldName, dataToAssign, true, true,
        true, annotationWithValueOverride);
  }

  /**
   * assign data to a field
   * 
   * @param field
   *            is the field to assign to
   * @param invokeOn
   *            to call on or null for static
   * @param dataToAssign
   *            data
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @param typeCast
   *            true if we should typecast
   */
  public static void assignField(Field field, Object invokeOn,
      Object dataToAssign, boolean overrideSecurity, boolean typeCast) {

    try {
     // Class fieldType = field.getType();
      // typecast
      if (typeCast) {
        //TODO add this in
        //dataToAssign = /*
        //         * ObjectUtils.typeCast(dataToAssign, fieldType,
        //         * true, true);
        //         */dataToAssign;
      }
      if (overrideSecurity) {
        field.setAccessible(true);
      }
      field.set(invokeOn, dataToAssign);
    } catch (Exception e) {
      throw new RuntimeException("Cant assign reflection field: "
          + (field == null ? null : field.getName()) + ", on: "
          + className(invokeOn) + ", with args: "
          + classNameCollection(dataToAssign), e);
    }
  }

  /**
   * null safe iterator getter if the type if collection
   * 
   * @param collection
   * @return the iterator
   */
  public static Iterator iterator(Object collection) {
    if (collection == null) {
      return null;
    }
    // array list doesnt need an iterator
    if (collection instanceof Collection
        && !(collection instanceof ArrayList)) {
      return ((Collection) collection).iterator();
    }
    return null;
  }

  /**
   * Null safe array length or map
   * 
   * @param arrayOrCollection
   * @return the length of the array (0 for null)
   */
  public static int length(Object arrayOrCollection) {
    if (arrayOrCollection == null) {
      return 0;
    }
    if (arrayOrCollection.getClass().isArray()) {
      return Array.getLength(arrayOrCollection);
    }
    if (arrayOrCollection instanceof Collection) {
      return ((Collection) arrayOrCollection).size();
    }
    if (arrayOrCollection instanceof Map) {
      return ((Map) arrayOrCollection).size();
    }
    // simple non array non collection object
    return 1;
  }

  /**
   * If array, get the element based on index, if Collection, get it based on
   * iterator.
   * 
   * @param arrayOrCollection
   * @param iterator
   * @param index
   * @return the object
   */
  public static Object next(Object arrayOrCollection, Iterator iterator,
      int index) {
    if (arrayOrCollection.getClass().isArray()) {
      return Array.get(arrayOrCollection, index);
    }
    if (arrayOrCollection instanceof ArrayList) {
      return ((ArrayList) arrayOrCollection).get(index);
    }
    if (arrayOrCollection instanceof Collection) {
      return iterator.next();
    }
    // simple object
    if (0 == index) {
      return arrayOrCollection;
    }
    throw new RuntimeException("Invalid class type: "
        + arrayOrCollection.getClass().getName());
  }

  /**
   * Remove the iterator or index
   * 
   * @param arrayOrCollection
   * @param index
   * @return the object list or new array
   */
  public static Object remove(Object arrayOrCollection, 
      int index) {
    return remove(arrayOrCollection, null, index);
  }
  
  /**
   * Remove the iterator or index
   * 
   * @param arrayOrCollection
   * @param iterator
   * @param index
   * @return the object list or new array
   */
  public static Object remove(Object arrayOrCollection, Iterator iterator,
      int index) {
    
    //if theres an iterator, just use that
    if (iterator != null) {
      iterator.remove();
      return arrayOrCollection;
    }
    if (arrayOrCollection.getClass().isArray()) {
      int newLength = Array.getLength(arrayOrCollection) - 1;
      Object newArray = Array.newInstance(arrayOrCollection.getClass().getComponentType(), newLength);
      if (newLength == 0) {
        return newArray;
      }
      if (index > 0) {
        System.arraycopy(arrayOrCollection, 0, newArray, 0, index);
      }
      if (index < newLength) {
        System.arraycopy(arrayOrCollection, index+1, newArray, index, newLength - index);
      }
      return newArray;
    }
    if (arrayOrCollection instanceof List) {
      ((List)arrayOrCollection).remove(index);
      return arrayOrCollection;
    } else if (arrayOrCollection instanceof Collection) {
      //this should work unless there are duplicates or something weird
      ((Collection)arrayOrCollection).remove(get(arrayOrCollection, index));
      return arrayOrCollection;
    }
    throw new RuntimeException("Invalid class type: "
        + arrayOrCollection.getClass().getName());
  }

  /**
   * null safe classname method
   * 
   * @param object
   * @return the classname
   */
  public static String classNameCollection(Object object) {
    if (object == null) {
      return null;
    }
    Iterator iterator = iterator(object);
    int length = length(object);
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < length; i++) {
      result.append(className(next(object, iterator, i)));
      if (i != length - 1) {
        result.append(", ");
      }
    }
    return result.toString();
  }

  /**
   * null safe classname method, gets the unenhanced name
   * 
   * @param object
   * @return the classname
   */
  public static String className(Object object) {
    return object == null ? null : unenhanceClass(object.getClass())
        .getName();
  }

  /**
   * if a class is enhanced, get the unenhanced version
   * 
   * @param theClass
   * @return the unenhanced version
   */
  public static Class unenhanceClass(Class theClass) {
    try {
      while (Enhancer.isEnhanced(theClass)) {
        theClass = theClass.getSuperclass();
      }
      return theClass;
    } catch (Exception e) {
      throw new RuntimeException("Problem unenhancing " + theClass, e);
    }
  }

  /**
   * assign data to a field
   * 
   * @param field
   *            is the field to assign to
   * @param invokeOn
   *            to call on or null for static
   * @param dataToAssign
   *            data
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @param typeCast
   *            true if we should typecast
   * @param annotationWithValueOverride
   *            annotation with value of override, or null if none
   */
  @SuppressWarnings("unchecked")
  public static void assignField(Field field, Object invokeOn,
      Object dataToAssign, boolean overrideSecurity, boolean typeCast,
      Class<? extends Annotation> annotationWithValueOverride) {

    if (annotationWithValueOverride != null) {
      // see if in annotation
      Annotation annotation = field
          .getAnnotation(annotationWithValueOverride);
      if (annotation != null) {
        /*
         * // type of the value, or String if not specific Class
         * typeOfAnnotationValue = typeCast ? field.getType() :
         * String.class; dataToAssign =
         * AnnotationUtils.retrieveAnnotationValue(
         * typeOfAnnotationValue, annotation, "value");
         */
        throw new RuntimeException("Not supported");
      }
    }
    assignField(field, invokeOn, dataToAssign, overrideSecurity, typeCast);
  }

  /**
   * assign data to a field. Will find the field in superclasses, will
   * typecast, and will override security (private, protected, etc)
   * 
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @param dataToAssign
   *            data
   */
  public static void assignField(Object invokeOn, String fieldName,
      Object dataToAssign) {
    assignField(null, invokeOn, fieldName, dataToAssign, true, true, true,
        null);
  }

  /**
   * get a field object for a class, potentially in superclasses
   * 
   * @param theClass
   * @param fieldName
   * @param callOnSupers
   *            true if superclasses should be looked in for the field
   * @param throwExceptionIfNotFound
   *            will throw runtime exception if not found
   * @return the field object or null if not found (or exception if param is
   *         set)
   */
  public static Field field(Class theClass, String fieldName,
      boolean callOnSupers, boolean throwExceptionIfNotFound) {
    try {
      Field field = theClass.getDeclaredField(fieldName);
      // found it
      return field;
    } catch (NoSuchFieldException e) {
      // if method not found
      // if traversing up, and not Object, and not instance method
      if (callOnSupers && !theClass.equals(Object.class)) {
        return field(theClass.getSuperclass(), fieldName, callOnSupers,
            throwExceptionIfNotFound);
      }
    }
    // maybe throw an exception
    if (throwExceptionIfNotFound) {
      throw new RuntimeException("Cant find field: " + fieldName
          + ", in: " + theClass + ", callOnSupers: " + callOnSupers);
    }
    return null;
  }

  /**
   * return a set of Strings for a class and type. This is not for any
   * supertypes, only for the type at hand. includes final fields
   * 
   * @param theClass
   * @param fieldType
   *            or null for all
   * @param includeStaticFields
   * @return the set of strings, or the empty Set if none
   */
  @SuppressWarnings("unchecked")
  public static Set<String> fieldNames(Class theClass, Class fieldType,
      boolean includeStaticFields) {
    return fieldNamesHelper(theClass, theClass, fieldType, true, true,
        includeStaticFields, null, true);
  }

  /**
   * get all field names from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @return the set of field names or empty set if none
   */
  public static Set<String> fieldNames(Class theClass,
      Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, boolean includeStaticFields,
      boolean includeFinalFields) {
    return fieldNamesHelper(theClass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, null, true);

  }

  /**
   * get all field names from a class, including superclasses (if specified).
   * ignore a certain marker annotation
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @param markerAnnotationToIngore
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list
   * @return the set of field names
   */
  public static Set<String> fieldNames(Class theClass,
      Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, boolean includeStaticFields,
      boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotationToIngore) {
    return fieldNamesHelper(theClass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotationToIngore, false);

  }

  /**
   * get all field names from a class, including superclasses (if specified)
   * (up to and including the specified superclass). ignore a certain marker
   * annotation. Dont get static or final field, and get fields of all types
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param markerAnnotationToIngore
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list
   * @return the set of field names or empty set if none
   */
  public static Set<String> fieldNames(Class theClass,
      Class superclassToStopAt,
      Class<? extends Annotation> markerAnnotationToIngore) {
    return fieldNamesHelper(theClass, superclassToStopAt, null, true,
        false, false, markerAnnotationToIngore, false);
  }

  /**
   * get all field names from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            true to include finals
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list (if includeAnnotation is false)
   * @param includeAnnotation
   *            true if the attribute should be included if annotation is
   *            present, false if exclude
   * @return the set of field names or empty set if none
   */
  @SuppressWarnings("unchecked")
  static Set<String> fieldNamesHelper(Class theClass,
      Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, boolean includeStaticFields,
      boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotation,
      boolean includeAnnotation) {
    Set<Field> fieldSet = fieldsHelper(theClass, superclassToStopAt,
        fieldType, includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotation, includeAnnotation);
    Set<String> fieldNameSet = new HashSet<String>();
    for (Field field : fieldSet) {
      fieldNameSet.add(field.getName());
    }
    return fieldNameSet;

  }

  /**
   * get all fields from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list (if includeAnnotation is false)
   * @param includeAnnotation
   *            true if the attribute should be included if annotation is
   *            present, false if exclude
   * @return the set of fields (wont return null)
   */
  @SuppressWarnings("unchecked")
  public static Set<Field> fields(Class theClass, Class superclassToStopAt,
      Class fieldType, boolean includeSuperclassToStopAt,
      boolean includeStaticFields, boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotation,
      boolean includeAnnotation) {
    return fieldsHelper(theClass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotation, includeAnnotation);
  }

  /**
   * get all fields from a class, including superclasses (if specified) (up to
   * and including the specified superclass). ignore a certain marker
   * annotation, or only include it. Dont get static or final field, and get
   * fields of all types
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list (if includeAnnotation is false)
   * @param includeAnnotation
   *            true if the attribute should be included if annotation is
   *            present, false if exclude
   * @return the set of field names or empty set if none
   */
  @SuppressWarnings("unchecked")
  public static Set<Field> fields(Class theClass, Class superclassToStopAt,
      Class<? extends Annotation> markerAnnotation,
      boolean includeAnnotation) {
    return fieldsHelper(theClass, superclassToStopAt, null, true, false,
        false, markerAnnotation, includeAnnotation);
  }

  /**
   * get all fields from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list (if includeAnnotation is false)
   * @param includeAnnotation
   *            true if the attribute should be included if annotation is
   *            present, false if exclude
   * @return the set of fields (wont return null)
   */
  @SuppressWarnings("unchecked")
  static Set<Field> fieldsHelper(Class theClass, Class superclassToStopAt,
      Class<?> fieldType, boolean includeSuperclassToStopAt,
      boolean includeStaticFields, boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotation,
      boolean includeAnnotation) {
    // MAKE SURE IF ANY MORE PARAMS ARE ADDED, THE CACHE KEY IS CHANGED!

    Set<Field> fieldNameSet = null;
    String cacheKey = theClass + CACHE_SEPARATOR + superclassToStopAt
        + CACHE_SEPARATOR + fieldType + CACHE_SEPARATOR
        + includeSuperclassToStopAt + CACHE_SEPARATOR
        + includeStaticFields + CACHE_SEPARATOR + includeFinalFields
        + CACHE_SEPARATOR + markerAnnotation + CACHE_SEPARATOR
        + includeAnnotation;
    fieldNameSet = fieldSetCache.get(cacheKey);
    if (fieldNameSet != null) {
      return fieldNameSet;
    }

    fieldNameSet = new HashSet<Field>();
    fieldsHelper(theClass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotation, fieldNameSet,
        includeAnnotation);

    // add to cache
    fieldSetCache.put(cacheKey, fieldNameSet);

    return fieldNameSet;

  }

  /**
   * get all field names from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list
   * @param fieldSet
   *            set to add fields to
   * @param includeAnnotation
   *            if include or exclude
   */
  @SuppressWarnings("unchecked")
  private static void fieldsHelper(Class theClass, Class superclassToStopAt,
      Class<?> fieldType, boolean includeSuperclassToStopAt,
      boolean includeStaticFields, boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotation, Set<Field> fieldSet,
      boolean includeAnnotation) {
    theClass = unenhanceClass(theClass);
    Field[] fields = theClass.getDeclaredFields();
    if (length(fields) != 0) {
      for (Field field : fields) {
        // if checking for type, and not right type, continue
        if (fieldType != null
            && !fieldType.isAssignableFrom(field.getType())) {
          continue;
        }
        // if not static, then continue
        if (!includeStaticFields
            && Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        // if not final constinue
        if (!includeFinalFields
            && Modifier.isFinal(field.getModifiers())) {
          continue;
        }
        // if checking for annotation
        if (markerAnnotation != null
            && (includeAnnotation != field
                .isAnnotationPresent(markerAnnotation))) {
          continue;
        }
        // go for it
        fieldSet.add(field);
      }
    }
    // see if done recursing (if superclassToStopAt is null, then stop at
    // Object
    if (theClass.equals(superclassToStopAt)
        || theClass.equals(Object.class)) {
      return;
    }
    Class superclass = theClass.getSuperclass();
    if (!includeSuperclassToStopAt && superclass.equals(superclassToStopAt)) {
      return;
    }
    // recurse
    fieldsHelper(superclass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotation, fieldSet,
        includeAnnotation);
  }

  /**
   * find out a field value
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @param callOnSupers
   *            if static and method not exists, try on supers
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @return the current value
   */
  public static Object fieldValue(Class theClass, Object invokeOn,
      String fieldName, boolean callOnSupers, boolean overrideSecurity) {
    Field field = null;

    // only if the method exists, try to execute
    try {
      // ok if null
      if (theClass == null) {
        theClass = invokeOn.getClass();
      }
      field = field(theClass, fieldName, callOnSupers, true);
      return fieldValue(field, invokeOn, overrideSecurity);
    } catch (Exception e) {
      throw new RuntimeException("Cant execute reflection field: "
          + fieldName + ", on: " + className(invokeOn), e);
    }
  }

  /**
   * get the value of a field, override security if needbe
   * 
   * @param field
   * @param invokeOn
   * @return the value of the field
   */
  public static Object fieldValue(Field field, Object invokeOn) {
    return fieldValue(field, invokeOn, true);
  }

  /**
   * get the value of a field
   * 
   * @param field
   * @param invokeOn
   * @param overrideSecurity
   * @return the value of the field
   */
  public static Object fieldValue(Field field, Object invokeOn,
      boolean overrideSecurity) {
    if (overrideSecurity) {
      field.setAccessible(true);
    }
    try {
      return field.get(invokeOn);
    } catch (Exception e) {
      throw new RuntimeException("Cant execute reflection field: "
          + field.getName() + ", on: " + className(invokeOn), e);

    }

  }

  /**
   * find out a field value (invoke on supers, override security)
   * 
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @return the current value
   */
  public static Object fieldValue(Object invokeOn, String fieldName) {
    return fieldValue(null, invokeOn, fieldName, true, true);
  }

  /**
   * get the decalred methods for a class, perhaps from cache
   * 
   * @param theClass
   * @return the declared methods
   */
  @SuppressWarnings("unused")
  private static Method[] retrieveDeclaredMethods(Class theClass) {
    Method[] methods = declaredMethodsCache.get(theClass);
    // get from cache if we can
    if (methods == null) {
      methods = theClass.getDeclaredMethods();
      declaredMethodsCache.put(theClass, methods);
    }
    return methods;
  }

  /**
   * helper method for calling a method with no params (could be in
   * superclass)
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param methodName
   *            method name to call
   * @return the data
   */
  public static Object callMethod(Class theClass, Object invokeOn,
      String methodName) {
    return callMethod(theClass, invokeOn, methodName, null, null);
  }

  /**
   * helper method for calling a method (could be in superclass)
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param methodName
   *            method name to call
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param paramsOrListOrArray
   *            data
   * @return the data
   */
  public static Object callMethod(Class theClass, Object invokeOn,
      String methodName, Object paramTypesOrArrayOrList,
      Object paramsOrListOrArray) {
    return callMethod(theClass, invokeOn, methodName,
        paramTypesOrArrayOrList, paramsOrListOrArray, true);
  }

  /**
   * helper method for calling a method
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param methodName
   *            method name to call
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param paramsOrListOrArray
   *            data
   * @param callOnSupers
   *            if static and method not exists, try on supers
   * @return the data
   */
  public static Object callMethod(Class theClass, Object invokeOn,
      String methodName, Object paramTypesOrArrayOrList,
      Object paramsOrListOrArray, boolean callOnSupers) {
    return callMethod(theClass, invokeOn, methodName,
        paramTypesOrArrayOrList, paramsOrListOrArray, callOnSupers,
        false);
  }

  /**
   * helper method for calling a method
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param methodName
   *            method name to call
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param paramsOrListOrArray
   *            data
   * @param callOnSupers
   *            if static and method not exists, try on supers
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @return the data
   */
  public static Object callMethod(Class theClass, Object invokeOn,
      String methodName, Object paramTypesOrArrayOrList,
      Object paramsOrListOrArray, boolean callOnSupers,
      boolean overrideSecurity) {
    Method method = null;

    Class[] paramTypesArray = (Class[]) toArray(paramTypesOrArrayOrList);

    try {
      method = theClass.getDeclaredMethod(methodName, paramTypesArray);
      if (overrideSecurity) {
        method.setAccessible(true);
      }
    } catch (Exception e) {
      // if method not found
      if (e instanceof NoSuchMethodException) {
        // if traversing up, and not Object, and not instance method
        // CH 070425 not sure why invokeOn needs to be null, removing
        // this
        if (callOnSupers /* && invokeOn == null */
            && !theClass.equals(Object.class)) {
          return callMethod(theClass.getSuperclass(), invokeOn,
              methodName, paramTypesOrArrayOrList,
              paramsOrListOrArray, callOnSupers, overrideSecurity);
        }
      }
      throw new RuntimeException("Problem calling method " + methodName
          + " on " + theClass.getName(), e);
    }

    return invokeMethod(method, invokeOn, paramsOrListOrArray);
  }

  /**
   * Safely invoke a reflection method
   * 
   * @param method
   *            to invoke
   * @param invokeOn
   * @param paramsOrListOrArray
   * @return the result
   */
  public static Object invokeMethod(Method method, Object invokeOn,
      Object paramsOrListOrArray) {

    Object[] args = (Object[]) toArray(paramsOrListOrArray);

    // we want to make sure things are accessible
    method.setAccessible(true);

    // only if the method exists, try to execute
    Object result = null;
    try {
      result = method.invoke(invokeOn, args);
    } catch (Exception e) {
      throw new RuntimeException("Cant execute reflection method: "
          + method.getName() + ", on: " + className(invokeOn)
          + ", with args: " + classNameCollection(args), e);
    }
    return result;
  }

  /**
   * Convert a list to an array with the type of the first element e.g. if it
   * is a list of Person objects, then the array is Person[]
   * 
   * @param objectOrArrayOrCollection
   *            is a list
   * @return the array of objects with type of the first element in the list
   */
  public static Object toArray(Object objectOrArrayOrCollection) {
    // do this before length since if array with null in it, we want ti get
    // it back
    if (objectOrArrayOrCollection != null
        && objectOrArrayOrCollection.getClass().isArray()) {
      return objectOrArrayOrCollection;
    }
    int length = length(objectOrArrayOrCollection);
    if (length == 0) {
      return null;
    }

    if (objectOrArrayOrCollection instanceof Collection) {
      Collection collection = (Collection) objectOrArrayOrCollection;
      Object first = collection.iterator().next();
      return toArray(collection, first == null ? Object.class : first
          .getClass());
    }
    // make an array of the type of object passed in, size one
    Object array = Array.newInstance(objectOrArrayOrCollection.getClass(),
        1);
    Array.set(array, 0, objectOrArrayOrCollection);
    return array;
  }

  /**
   * convert a list into an array of type of theClass
   * 
   * @param collection
   *            list to convert
   * @param theClass
   *            type of array to return
   * @return array of type theClass[] filled with the objects from list
   */
  public static Object toArray(Collection collection, Class theClass) {
    if (collection == null || collection.size() == 0) {
      return null;
    }

    return collection.toArray((Object[]) Array.newInstance(theClass,
        collection.size()));

  }

  /**
   * helper method for calling a static method up the stack. method takes no
   * args (could be in superclass)
   * 
   * @param theClass
   *            the class which has the method
   * @param methodName
   *            method name to call
   * @return the data
   */
  public static Object callMethod(Class theClass, String methodName) {
    return callMethod(theClass, null, methodName, null, null);
  }

  /**
   * helper method for calling a static method with no params
   * 
   * @param theClass
   *            the class which has the method
   * @param methodName
   *            method name to call
   * @param callOnSupers
   *            if we should try the super classes if not exists in this class
   * @return the data
   */
  public static Object callMethod(Class theClass, String methodName,
      boolean callOnSupers) {
    return callMethod(theClass, null, methodName, null, null, callOnSupers);
  }

  /**
   * helper method for calling a static method up the stack
   * 
   * @param theClass
   *            the class which has the method
   * @param methodName
   *            method name to call
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param paramsOrListOrArray
   *            data
   * @return the data
   */
  public static Object callMethod(Class theClass, String methodName,
      Object paramTypesOrArrayOrList, Object paramsOrListOrArray) {
    return callMethod(theClass, null, methodName, paramTypesOrArrayOrList,
        paramsOrListOrArray);
  }

  /**
   * helper method for calling a method with no params (could be in
   * superclass), will override security
   * 
   * @param invokeOn
   *            instance to invoke on
   * @param methodName
   *            method name to call not exists in this class
   * @return the data
   */
  public static Object callMethod(Object invokeOn, String methodName) {
    if (invokeOn == null) {
      throw new NullPointerException("invokeOn is null: " + methodName);
    }
    return callMethod(invokeOn.getClass(), invokeOn, methodName, null,
        null, true, true);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer. This does not recurse
   * 
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @return the string
   */
  public static String replace(String text, Object searchFor,
      Object replaceWith) {
    return replace(null, null, text, searchFor, replaceWith, false, 0,
        false);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer
   * 
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   * @return the string
   */
  public static String replace(String text, Object searchFor,
      Object replaceWith, boolean recurse) {
    return replace(null, null, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0, false);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer
   * 
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   * @param removeIfFound
   *            true if removing from searchFor and replaceWith if found
   * @return the string
   */
  public static String replace(String text, Object searchFor,
      Object replaceWith, boolean recurse, boolean removeIfFound) {
    return replace(null, null, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0, removeIfFound);
  }

  /**
   * <p>
   * Replaces all occurrences of a String within another String.
   * </p>
   * 
   * <p>
   * A <code>null</code> reference passed to this method is a no-op.
   * </p>
   * 
   * <pre>
   * StringUtils.replace(null, *, *)        = null
   * StringUtils.replace(&quot;&quot;, *, *)          = &quot;&quot;
   * StringUtils.replace(&quot;any&quot;, null, *)    = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, *, null)    = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, &quot;&quot;, *)      = &quot;any&quot;
   * StringUtils.replace(&quot;aba&quot;, &quot;a&quot;, null)  = &quot;aba&quot;
   * StringUtils.replace(&quot;aba&quot;, &quot;a&quot;, &quot;&quot;)    = &quot;b&quot;
   * StringUtils.replace(&quot;aba&quot;, &quot;a&quot;, &quot;z&quot;)   = &quot;zbz&quot;
   * </pre>
   * 
   * @see #replace(String text, String repl, String with, int max)
   * @param text
   *            text to search and replace in, may be null
   * @param repl
   *            the String to search for, may be null
   * @param with
   *            the String to replace with, may be null
   * @return the text with any replacements processed, <code>null</code> if
   *         null String input
   */
  public static String replace(String text, String repl, String with) {
    return replace(text, repl, with, -1);
  }

  /**
   * <p>
   * Replaces a String with another String inside a larger String, for the
   * first <code>max</code> values of the search String.
   * </p>
   * 
   * <p>
   * A <code>null</code> reference passed to this method is a no-op.
   * </p>
   * 
   * <pre>
   * StringUtils.replace(null, *, *, *)         = null
   * StringUtils.replace(&quot;&quot;, *, *, *)           = &quot;&quot;
   * StringUtils.replace(&quot;any&quot;, null, *, *)     = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, *, null, *)     = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, &quot;&quot;, *, *)       = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, *, *, 0)        = &quot;any&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, null, -1) = &quot;abaa&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;&quot;, -1)   = &quot;b&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 0)   = &quot;abaa&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 1)   = &quot;zbaa&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 2)   = &quot;zbza&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, -1)  = &quot;zbzz&quot;
   * </pre>
   * 
   * @param text
   *            text to search and replace in, may be null
   * @param repl
   *            the String to search for, may be null
   * @param with
   *            the String to replace with, may be null
   * @param max
   *            maximum number of values to replace, or <code>-1</code> if
   *            no maximum
   * @return the text with any replacements processed, <code>null</code> if
   *         null String input
   */
  public static String replace(String text, String repl, String with, int max) {
    if (text == null || isEmpty(repl) || with == null || max == 0) {
      return text;
    }

    StringBuffer buf = new StringBuffer(text.length());
    int start = 0, end = 0;
    while ((end = text.indexOf(repl, start)) != -1) {
      buf.append(text.substring(start, end)).append(with);
      start = end + repl.length();

      if (--max == 0) {
        break;
      }
    }
    buf.append(text.substring(start));
    return buf.toString();
  }

  /**
   * <p>
   * Checks if a String is empty ("") or null.
   * </p>
   * 
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty(&quot;&quot;)        = true
   * StringUtils.isEmpty(&quot; &quot;)       = false
   * StringUtils.isEmpty(&quot;bob&quot;)     = false
   * StringUtils.isEmpty(&quot;  bob  &quot;) = false
   * </pre>
   * 
   * <p>
   * NOTE: This method changed in Lang version 2.0. It no longer trims the
   * String. That functionality is available in isBlank().
   * </p>
   * 
   * @param str
   *            the String to check, may be null
   * @return <code>true</code> if the String is empty or null
   */
  public static boolean isEmpty(String str) {
    return str == null || str.length() == 0;
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer. This does not recurse
   * 
   * @param outBuffer
   *            stringbuffer to write to
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   */
  public static void replace(StringBuffer outBuffer, String text,
      Object searchFor, Object replaceWith) {
    replace(outBuffer, null, text, searchFor, replaceWith, false, 0, false);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer
   * 
   * @param outBuffer
   *            stringbuffer to write to
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   */
  public static void replace(StringBuffer outBuffer, String text,
      Object searchFor, Object replaceWith, boolean recurse) {
    replace(outBuffer, null, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0, false);
  }

  /**
   * replace a string with other strings, and either write to outWriter, or
   * StringBuffer, and if StringBuffer potentially return a string. If
   * outBuffer and outWriter are null, then return the String
   * 
   * @param outBuffer
   *            stringbuffer to write to, or null to not
   * @param outWriter
   *            Writer to write to, or null to not.
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for, or string, or list
   * @param replaceWith
   *            string array to replace with, or string, or list
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   * @param timeToLive
   *            if recursing, prevent endless loops
   * @param removeIfFound
   *            true if removing from searchFor and replaceWith if found
   * @return the String if outBuffer and outWriter are null
   * @throws IndexOutOfBoundsException
   *             if the lengths of the arrays are not the same (null is ok,
   *             and/or size 0)
   * @throws IllegalArgumentException
   *             if the search is recursive and there is an endless loop due
   *             to outputs of one being inputs to another
   */
  private static String replace(StringBuffer outBuffer, Writer outWriter,
      String text, Object searchFor, Object replaceWith, boolean recurse,
      int timeToLive, boolean removeIfFound) {

    // if recursing, we need to get the string, then print to buffer (since
    // we need multiple passes)
    if (!recurse) {
      return replaceHelper(outBuffer, outWriter, text, searchFor,
          replaceWith, recurse, timeToLive, removeIfFound);
    }
    // get the string
    String result = replaceHelper(null, null, text, searchFor, replaceWith,
        recurse, timeToLive, removeIfFound);
    if (outBuffer != null) {
      outBuffer.append(result);
      return null;
    }

    if (outWriter != null) {
      try {
        outWriter.write(result);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
      return null;
    }

    return result;

  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer. This does not recurse
   * 
   * @param outWriter
   *            writer to write to
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   */
  public static void replace(Writer outWriter, String text, Object searchFor,
      Object replaceWith) {
    replace(null, outWriter, text, searchFor, replaceWith, false, 0, false);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer
   * 
   * @param outWriter
   *            writer to write to
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   */
  public static void replace(Writer outWriter, String text, Object searchFor,
      Object replaceWith, boolean recurse) {
    replace(null, outWriter, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0, false);
  }

  /**
   * replace a string with other strings, and either write to outWriter, or
   * StringBuffer, and if StringBuffer potentially return a string. If
   * outBuffer and outWriter are null, then return the String
   * 
   * @param outBuffer
   *            stringbuffer to write to, or null to not
   * @param outWriter
   *            Writer to write to, or null to not.
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for, or string, or list
   * @param replaceWith
   *            string array to replace with, or string, or list
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   * @param timeToLive
   *            if recursing, prevent endless loops
   * @param removeIfFound
   *            true if removing from searchFor and replaceWith if found
   * @return the String if outBuffer and outWriter are null
   * @throws IllegalArgumentException
   *             if the search is recursive and there is an endless loop due
   *             to outputs of one being inputs to another
   * @throws IndexOutOfBoundsException
   *             if the lengths of the arrays are not the same (null is ok,
   *             and/or size 0)
   */
  private static String replaceHelper(StringBuffer outBuffer,
      Writer outWriter, String text, Object searchFor,
      Object replaceWith, boolean recurse, int timeToLive,
      boolean removeIfFound) {

    try {
      // if recursing, this shouldnt be less than 0
      if (timeToLive < 0) {
        throw new IllegalArgumentException("TimeToLive under 0: "
            + timeToLive + ", " + text);
      }

      int searchForLength = length(searchFor);
      boolean done = false;
      // no need to do anything
      if (isEmpty(text)) {
        return text;
      }
      // need to write the input to output, later
      if (searchForLength == 0) {
        done = true;
      }

      boolean[] noMoreMatchesForReplIndex = null;
      int inputIndex = -1;
      int replaceIndex = -1;
      long resultPacked = -1;

      if (!done) {
        // make sure lengths are ok, these need to be equal
        if (searchForLength != length(replaceWith)) {
          throw new IndexOutOfBoundsException("Lengths dont match: "
              + searchForLength + ", " + length(replaceWith));
        }

        // keep track of which still have matches
        noMoreMatchesForReplIndex = new boolean[searchForLength];

        // index of replace array that will replace the search string
        // found
        

        resultPacked = findNextIndexHelper(searchForLength, searchFor,
            replaceWith, 
            noMoreMatchesForReplIndex, text, 0);

        inputIndex = unpackInt(resultPacked, true);
        replaceIndex = unpackInt(resultPacked, false);
      }

      // get a good guess on the size of the result buffer so it doesnt
      // have to double if it
      // goes over a bit
      boolean writeToWriter = outWriter != null;

      // no search strings found, we are done
      if (done || inputIndex == -1) {
        if (writeToWriter) {
          outWriter.write(text, 0, text.length());
          return null;
        }
        if (outBuffer != null) {
          appendSubstring(outBuffer, text, 0, text.length());
          return null;
        }
        return text;
      }

      // no buffer if writing to writer
      StringBuffer bufferToWriteTo = outBuffer != null ? outBuffer
          : (writeToWriter ? null : new StringBuffer(text.length()
              + replaceStringsBufferIncrease(text, searchFor,
                  replaceWith)));

      String searchString = null;
      String replaceString = null;

      int start = 0;

      while (inputIndex != -1) {

        searchString = (String) get(searchFor, replaceIndex);
        replaceString = (String) get(replaceWith, replaceIndex);
        if (writeToWriter) {
          outWriter.write(text, start, inputIndex - start);
          outWriter.write(replaceString);
        } else {
          appendSubstring(bufferToWriteTo, text, start, inputIndex)
              .append(replaceString);
        }

        if (removeIfFound) {
          // better be an iterator based find replace
          searchFor = remove(searchFor, replaceIndex);
          replaceWith = remove(replaceWith, replaceIndex);
          noMoreMatchesForReplIndex = (boolean[])remove(noMoreMatchesForReplIndex, replaceIndex);
          // we now have a lesser size if we removed one
          searchForLength--;
        }

        start = inputIndex + searchString.length();

        resultPacked = findNextIndexHelper(searchForLength, searchFor,
            replaceWith, 
            noMoreMatchesForReplIndex, text, start);
        inputIndex = unpackInt(resultPacked, true);
        replaceIndex = unpackInt(resultPacked, false);
      }
      if (writeToWriter) {
        outWriter.write(text, start, text.length() - start);

      } else {
        appendSubstring(bufferToWriteTo, text, start, text.length());
      }

      // no need to convert to string if incoming buffer or writer
      if (writeToWriter || outBuffer != null) {
        if (recurse) {
          throw new IllegalArgumentException(
              "Cannot recurse and write to existing buffer or writer!");
        }
        return null;
      }
      String resultString = bufferToWriteTo.toString();

      if (recurse) {
        return replaceHelper(outBuffer, outWriter, resultString,
            searchFor, replaceWith, recurse, timeToLive - 1, false);
      }
      // this might be null for writer
      return resultString;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * give a best guess on buffer increase for String[] replace get a good
   * guess on the size of the result buffer so it doesnt have to double if it
   * goes over a bit
   * 
   * @param text
   * @param repl
   * @param with
   * @return the increase, with 20% cap
   */
  static int replaceStringsBufferIncrease(String text, Object repl,
      Object with) {
    // count the greaters
    int increase = 0;
    Iterator iteratorReplace = iterator(repl);
    Iterator iteratorWith = iterator(with);
    int replLength = length(repl);
    String currentRepl = null;
    String currentWith = null;
    for (int i = 0; i < replLength; i++) {
      currentRepl = (String) next(repl, iteratorReplace, i);
      currentWith = (String) next(with, iteratorWith, i);
      if (currentRepl == null || currentWith == null) {
        throw new NullPointerException("Replace string is null: "
            + text + ", " + currentRepl + ", " + currentWith);
      }
      int greater = currentWith.length() - currentRepl.length();
      increase += greater > 0 ? 3 * greater : 0; // assume 3 matches
    }
    // have upper-bound at 20% increase, then let Java take over
    increase = Math.min(increase, text.length() / 5);
    return increase;
  }

  /**
   * Helper method to find the next match in an array of strings replace
   * 
   * @param searchForLength
   * @param searchFor
   * @param replaceWith
   * @param noMoreMatchesForReplIndex
   * @param input
   * @param start
   *            is where to start looking
   * @return result packed into a long, inputIndex first, then replaceIndex
   */
  private static long findNextIndexHelper(int searchForLength,
      Object searchFor, Object replaceWith, boolean[] noMoreMatchesForReplIndex,
      String input, int start) {

    int inputIndex = -1;
    int replaceIndex = -1;

    Iterator iteratorSearchFor = iterator(searchFor);
    Iterator iteratorReplaceWith = iterator(replaceWith);

    String currentSearchFor = null;
    String currentReplaceWith = null;
    int tempIndex = -1;
    for (int i = 0; i < searchForLength; i++) {
      currentSearchFor = (String) next(searchFor, iteratorSearchFor, i);
      currentReplaceWith = (String) next(replaceWith,
          iteratorReplaceWith, i);
      if (noMoreMatchesForReplIndex[i] || isEmpty(currentSearchFor)
          || currentReplaceWith == null) {
        continue;
      }
      tempIndex = input.indexOf(currentSearchFor, start);

      // see if we need to keep searching for this
      noMoreMatchesForReplIndex[i] = tempIndex == -1;

      if (tempIndex != -1 && (inputIndex == -1 || tempIndex < inputIndex)) {
        inputIndex = tempIndex;
        replaceIndex = i;
      }

    }
    // dont create an array, no more objects
    long resultPacked = packInts(inputIndex, replaceIndex);
    return resultPacked;
  }

  /**
   * pack two ints into a long. Note: the first is held in the left bits, the
   * second is held in the right bits
   * 
   * @param first
   *            is first int
   * @param second
   *            is second int
   * @return the long which has two ints in there
   */
  public static long packInts(int first, int second) {
    long result = first;
    result <<= 32;
    result |= second;
    return result;
  }

  /**
   * take a long
   * 
   * @param theLong
   *            to unpack
   * @param isFirst
   *            true for first, false for second
   * @return one of the packed ints, first or second
   */
  public static int unpackInt(long theLong, boolean isFirst) {

    int result = 0;
    // put this in the position of the second one
    if (isFirst) {
      theLong >>= 32;
    }
    // only look at right part
    result = (int) (theLong & 0xffffffff);
    return result;
  }

  /**
   * append a substring to a stringbuffer. removes dependency on substring
   * which creates objects
   * 
   * @param buf
   *            stringbuffer
   * @param string
   *            source string
   * @param start
   *            start index of source string
   * @param end
   *            end index of source string
   * @return the string buffer for chaining
   */
  private static StringBuffer appendSubstring(StringBuffer buf,
      String string, int start, int end) {
    for (int i = start; i < end; i++) {
      buf.append(string.charAt(i));
    }
    return buf;
  }

  /**
   * Get a specific index of an array or collection (note for collections and
   * iterating, it is more efficient to get an iterator and iterate
   * 
   * @param arrayOrCollection
   * @param index
   * @return the object at that index
   */
  public static Object get(Object arrayOrCollection, int index) {

    if (arrayOrCollection == null) {
      if (index == 0) {
        return null;
      }
      throw new RuntimeException("Trying to access index " + index
          + " of null");
    }

    // no need to iterator on list (e.g. FastProxyList has no iterator
    if (arrayOrCollection instanceof List) {
      return ((List) arrayOrCollection).get(index);
    }
    if (arrayOrCollection instanceof Collection) {
      Iterator iterator = iterator(arrayOrCollection);
      for (int i = 0; i < index; i++) {
        next(arrayOrCollection, iterator, i);
      }
      return next(arrayOrCollection, iterator, index);
    }

    if (arrayOrCollection.getClass().isArray()) {
      return Array.get(arrayOrCollection, index);
    }

    if (index == 0) {
      return arrayOrCollection;
    }

    throw new RuntimeException("Trying to access index " + index
        + " of and object: " + arrayOrCollection);
  }


  
  /**
   * fail safe toString for Exception blocks, and include the stack
   * if there is a problem with toString()
   * @param object
   * @return the toStringSafe string
   */
  public static String toStringSafe(Object object) {
    if (object == null) {
      return null;
    }
    
    try {
      //give size and type if collection
      if (object instanceof Collection) {
        Collection<Object> collection = (Collection<Object>) object;
        int collectionSize = collection.size();
        if (collectionSize == 0) {
          return "Empty " + object.getClass().getSimpleName();
        }
        Object first = collection.iterator().next();
        return object.getClass().getSimpleName() + " of size " 
          + collectionSize + " with first type: " + 
          (first == null ? null : first.getClass());
      }
    
      return object.toString();
    } catch (Exception e) {
      return "<<exception>> " + object.getClass() + ":\n" + ExceptionUtils.getFullStackTrace(e) + "\n";
    }
  }
}
