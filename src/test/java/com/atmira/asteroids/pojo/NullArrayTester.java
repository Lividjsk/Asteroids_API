package com.atmira.asteroids.pojo;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.openpojo.log.LoggerFactory;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoField;
import com.openpojo.reflection.PojoMethod;
import com.openpojo.reflection.exception.ReflectionException;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.test.Tester;
import com.openpojo.validation.utils.IdentityHandlerStub;
import com.openpojo.validation.utils.ValidationHelper;

public class NullArrayTester implements Tester {

    /**
     * Utility method to add at leas one implementation on every abstarcta class generated by JAXB
     * 
     * @param pojoClasses
     *            The list of pojoClasses created by OpenPojjo factory
     */
    public static void propareAbstractPojoClasses(List<PojoClass> pojoClasses) {

        for (PojoClass p : pojoClasses) {
            LoggerFactory.getLogger(pojoClasses.getClass()).info(" pojo inicio {0} {1}", p.isAbstract(), p.getName());
        }

        List<PojoClass> pojoClassesImpl = new ArrayList<PojoClass>();
        for (PojoClass p : pojoClasses) {
            if (p.isAbstract() && p.getAnnotation(XmlSeeAlso.class) != null) {
                XmlSeeAlso xmlSeeAlso = p.getAnnotation(XmlSeeAlso.class);
                if (xmlSeeAlso.value().length > 0) {
                    pojoClassesImpl.add(PojoClassFactory.getPojoClass(xmlSeeAlso.value()[0]));
                    LoggerFactory.getLogger(pojoClasses.getClass()).info(" Added implementation {0} ",
                            PojoClassFactory.getPojoClass(xmlSeeAlso.value()[0]).getName());
                }
            }
        }
        pojoClasses.addAll(pojoClassesImpl);

        for (PojoClass p : pojoClasses) {
            LoggerFactory.getLogger(pojoClasses.getClass()).info(" pojo {0} {1}", p.isAbstract(), p.getName());
        }
    }

    @Override
    public void run(PojoClass pojoClass) {
        Object classInstance = ValidationHelper.getBasicInstance(pojoClass);

        LoggerFactory.getLogger(this.getClass()).info("============================================");
        LoggerFactory.getLogger(this.getClass())
                .info("============ Processing Class {0}  {1} {2}",
                        pojoClass.getName().substring(pojoClass.getName().lastIndexOf('.') + 1),
                        pojoClass.getSuperClass().getName()
                                .substring(pojoClass.getSuperClass().getName().lastIndexOf('.') + 1),
                        pojoClass.getName());
        if (pojoClass.isAbstract()) {
            LoggerFactory.getLogger(this.getClass()).debug("---------- Actual class is Abstract");
            return;
        } else {
            LoggerFactory.getLogger(this.getClass()).debug("======== Actual class is NOT Abstract");
        }

        List<PojoField> pojoFields = pojoClass.getPojoFields();
        if (pojoClass.getName().endsWith("_Impl")) {
            pojoFields = pojoClass.getSuperClass().getPojoFields();
            LoggerFactory.getLogger(this.getClass()).info("============  _Impl Abstract parent class");
            for (PojoField p : pojoFields)
                LoggerFactory.getLogger(this.getClass()).info("  Field {0}", p.getName());
        } else if (pojoClass.getSuperClass().isAbstract()) {
            pojoFields = pojoClass.getSuperClass().getPojoFields();
            LoggerFactory.getLogger(this.getClass()).info("============  Abstract parent class");
            for (PojoField p : pojoFields)
                LoggerFactory.getLogger(this.getClass()).info("  Field {0}", p.getName());
        } else {
            LoggerFactory.getLogger(this.getClass()).debug("============  No Abstract parent class");
        }

        List<PojoMethod> pojoMethods = pojoClass.getPojoMethods();
        if (pojoClass.getName().endsWith("_Impl")) {
            pojoMethods = pojoClass.getSuperClass().getPojoMethods();
            for (PojoMethod p : pojoMethods)
                LoggerFactory.getLogger(this.getClass()).info("  _IMPL Method {0}", p.getName());
        } else if (pojoClass.getSuperClass().isAbstract()) {
            pojoMethods = pojoClass.getSuperClass().getPojoMethods();
            for (PojoMethod p : pojoMethods)
                LoggerFactory.getLogger(this.getClass()).info("  Method {0}", p.getName());
        }

        try {
            for (PojoField fieldEntry : pojoFields) {
                if (!fieldEntry.isArray()) {
                    LoggerFactory.getLogger(this.getClass()).debug("Field [{0}] is not an array. Skipping...",
                            fieldEntry);
                    continue;
                }
                LoggerFactory.getLogger(this.getClass()).info("Processing Field {0}", fieldEntry.getName());

                PojoMethod pojoGetter = null;
                PojoMethod pojoSetter = null;
                PojoMethod pojoGetterIndex = null;
                PojoMethod pojoSetterIndex = null;
                PojoMethod pojoGetterLength = null;
                Object result = null;

                // Obtenemos el nombre de la variable de instancia capitalizada
                String fieldNameUpper = fieldEntry.getAnnotation(XmlElement.class).name();
                String fieldNameCapitalized = fieldEntry.getName().substring(0, 1).toUpperCase()
                        + fieldEntry.getName().substring(1);

                if (fieldNameUpper.endsWith("y")) {
                    fieldNameUpper = fieldNameUpper.substring(0, fieldNameUpper.length() - 1) + "ies";
                } else if (fieldNameUpper.endsWith("s")) {
                    fieldNameUpper = fieldNameUpper.substring(0, fieldNameUpper.length()) + "es";
                } else {
                    fieldNameUpper = fieldNameUpper.substring(0) + "s";
                }

                LoggerFactory.getLogger(this.getClass()).debug("Processing Field {0} [{1}] [{2}] [{3}]",
                        fieldEntry.getName(), fieldNameUpper, fieldNameCapitalized, fieldEntry);

                try {

                    // Crear una instancia de la clase
                    // probar o.getProperty() != null
                    // probar o.getProperty(0) thwors IndexOutOfBound
                    classInstance = ValidationHelper.getBasicInstance(pojoClass);

                    for (PojoMethod pm : pojoMethods) {
                        LoggerFactory.getLogger(this.getClass()).debug("  Method {0} {1}",
                                (pm.isConstructor() ? "Constructor" : pm.getName()), pm.getPojoParameters());

                        if (pm.getName().equals("get" + fieldNameUpper) && pm.getParameterTypes().length == 0) {
                            pojoGetter = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método getter()");
                        } else if (pm.getName().equals("get" + fieldNameCapitalized)
                                && pm.getParameterTypes().length == 0) {
                            pojoGetter = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método getter()");
                        }
                        if (pm.getName().equals("get" + fieldNameUpper) && pm.getParameterTypes().length == 1) {
                            pojoGetterIndex = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método getter(index)");
                        } else if (pm.getName().equals("get" + fieldNameCapitalized)
                                && pm.getParameterTypes().length == 1) {
                            pojoGetterIndex = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método getter(index)");
                        }
                        if (pm.getName().equals("set" + fieldNameUpper) && pm.getParameterTypes().length == 1) {
                            pojoSetter = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método setter(array[])");
                        } else if (pm.getName().equals("set" + fieldNameCapitalized)
                                && pm.getParameterTypes().length == 1) {
                            pojoSetter = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método setter(array[])");
                        }
                        if (pm.getName().equals("set" + fieldNameUpper) && pm.getParameterTypes().length == 2) {
                            pojoSetterIndex = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método setter(index, value)");
                        } else if (pm.getName().equals("set" + fieldNameCapitalized)
                                && pm.getParameterTypes().length == 2) {
                            pojoSetterIndex = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método setter(index, value)");
                        }
                        if (pm.getName().equals("get" + fieldNameUpper + "Length")
                                && pm.getParameterTypes().length == 0) {
                            pojoGetterLength = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método getterLengh()");
                        } else if (pm.getName().equals("get" + fieldNameCapitalized + "Length")
                                && pm.getParameterTypes().length == 0) {
                            pojoGetterLength = pm;
                            LoggerFactory.getLogger(this.getClass()).debug("     Asignado método getterLengh()");
                        }
                    }
                    Affirm.affirmTrue("Error 1: debe tener getter()", pojoGetter != null);
                    LoggerFactory.getLogger(this.getClass()).debug("  Invocar métido getter()");
                    result = pojoGetter.invoke(classInstance);
                    LoggerFactory.getLogger(this.getClass()).debug("  Metodo invocado getter()");
                    Affirm.affirmTrue("Error 1: debe ser no nulo", result != null);
                    result = pojoGetterLength.invoke(classInstance);
                    Affirm.affirmTrue("Error 1: debe ser leght cero", new Integer(0).equals(result));

                    Affirm.affirmTrue("Error 1: debe tener getter(0)", pojoGetterIndex != null);
                    LoggerFactory.getLogger(this.getClass()).debug("  Invocar métido getter(0)");
                    try {
                        result = pojoGetterIndex.invoke(classInstance, 0);
                    } catch (ReflectionException e) {
                        Throwable cause = e.getCause();
                        Affirm.affirmTrue(
                                "Thrown exception must be IndexOutOfBoundsException with cause IndexOutOfBoundsException",
                                cause.getCause() instanceof IndexOutOfBoundsException);
                    }
                    LoggerFactory.getLogger(this.getClass()).debug("  Metodo invocado getter(0)");

                    // Crear una instancia de la clase
                    // probar o.setProperty(new value[0])
                    // probar o.property() != null
                    // probar o.propertylengh() == 0
                    classInstance = ValidationHelper.getBasicInstance(pojoClass);
                    Object arrayInstance = Array.newInstance(classInstance.getClass(), 0);
                    LoggerFactory.getLogger(this.getClass()).debug("  Invocando método setter(instance[0])");
                    Class<?> arrayClass = pojoSetter.getParameterTypes()[0];
                    arrayInstance = Array.newInstance(arrayClass.getComponentType(), 0);
                    result = pojoSetter.invoke(classInstance, arrayInstance);

                    result = pojoGetter.invoke(classInstance);
                    LoggerFactory.getLogger(this.getClass()).debug("  Invocado método getter()");

                    Affirm.affirmTrue("Error 3: debe ser no nulo", pojoGetterLength != null);
                    result = pojoGetterLength.invoke(classInstance);
                    LoggerFactory.getLogger(this.getClass()).debug("  Invocado método getterLength()");
                    Affirm.affirmTrue("Error 3: debe ser cero", new Integer(0).equals(result));

                    // Crear una instancia de la clase
                    // probar o.setProperty(new value[1])
                    // probar o.property() != null
                    // probar o.propertylengh() == 1
                    // probar o.setProperty(1,null)
                    // probar o.getProerty(1) == null
                    LoggerFactory.getLogger(this.getClass()).debug("  Caso setProperty(instancia[1]))");
                    classInstance = ValidationHelper.getBasicInstance(pojoClass);
                    arrayClass = pojoSetter.getParameterTypes()[0];
                    arrayInstance = Array.newInstance(arrayClass.getComponentType(), 1);
                    result = pojoSetter.invoke(classInstance, arrayInstance);
                    result = pojoGetter.invoke(classInstance);
                    Affirm.affirmNotNull("getter() must be not null ", result);
                    result = pojoGetterLength.invoke(classInstance);
                    Affirm.affirmTrue("getterLenght() must be 1", new Integer(1).equals(result));
                    pojoSetterIndex.invoke(classInstance, 0, null);
                    result = pojoGetterIndex.invoke(classInstance, 0);
                    Affirm.affirmTrue("getter(0) must be null", result == null);
                    LoggerFactory.getLogger(this.getClass()).info("Finished property {0} ", fieldEntry.getName());

                } catch (IllegalArgumentException | SecurityException | AssertionError e) {
                    LoggerFactory.getLogger(this.getClass()).debug("Finished property with ERROR1 {0} ",
                            fieldEntry.getName());

                } catch (NullPointerException e) {
                    LoggerFactory.getLogger(this.getClass()).debug("Finished property with ERROR2 {0} Error: {1}",
                            fieldEntry.getName(), e);
                }

            }

            LoggerFactory.getLogger(this.getClass()).debug("==== Testing fields");
            for (PojoField fieldEntry : pojoFields) {
                try {
                    if (fieldEntry.hasGetter() && !fieldEntry.isArray()) {
                        Object value = fieldEntry.get(classInstance);

                        IdentityHandlerStub.registerIdentityHandlerStubForValue(value);

                        LoggerFactory.getLogger(this.getClass()).debug("Testing Field [{0}] with value [{1}]",
                                fieldEntry, value);

                        Affirm.affirmEquals("Getter returned non equal value for field=[" + fieldEntry + "]", value,
                                fieldEntry.invokeGetter(classInstance));
                        IdentityHandlerStub.unregisterIdentityHandlerStubForValue(value);
                    } else {
                        LoggerFactory.getLogger(this.getClass())
                                .debug("Field [{0}] has no getter skipping", fieldEntry);
                    }
                } catch (NullPointerException | AssertionError e) {
                    LoggerFactory.getLogger(this.getClass()).error("======== Field [{0}] has getter problems",
                            fieldEntry);
                }
            }

            for (PojoMethod methodEntry : pojoMethods) {
                try {
                    if (methodEntry.getName().startsWith("get") && methodEntry.getParameterTypes().length == 0) {
                        methodEntry.invoke(classInstance);
                    } else if (methodEntry.getName().startsWith("set") && methodEntry.getParameterTypes().length == 1
                            && methodEntry.getParameterTypes()[0].getCanonicalName().startsWith("com.telefonica")
                            && !methodEntry.getParameterTypes()[0].isArray()) {
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter ", methodEntry);

                        Object newInstance = null;
                        if (Modifier.isAbstract(methodEntry.getParameterTypes()[0].getModifiers())) {
                            if (methodEntry.getParameterTypes()[0].getAnnotation(XmlSeeAlso.class) != null) {
                                newInstance = methodEntry.getParameterTypes()[0].getAnnotation(XmlSeeAlso.class)
                                        .value()[0].newInstance();
                                LoggerFactory.getLogger(this.getClass()).error(
                                        "==== Field [{0}] sseter  Abstract parameter", methodEntry);

                            } else {
                                newInstance = Class.forName(methodEntry.getParameterTypes()[0].getName() + "_Impl")
                                        .newInstance();
                            }
                        } else {
                            newInstance = methodEntry.getParameterTypes()[0].newInstance();
                        }

                        methodEntry.invoke(classInstance, newInstance);
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter OK", methodEntry);

                    } else if (methodEntry.getName().startsWith("set") && methodEntry.getParameterTypes().length == 1
                            && methodEntry.getParameterTypes()[0].equals(java.lang.String.class)
                            && !methodEntry.getParameterTypes()[0].isArray()) {
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter ", methodEntry);
                        methodEntry.invoke(classInstance, methodEntry.getParameterTypes()[0].newInstance());
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter OK", methodEntry);

                    } else if (methodEntry.getName().startsWith("set") && methodEntry.getParameterTypes().length == 1
                            && methodEntry.getParameterTypes()[0].equals(java.lang.Long.class)
                            && !methodEntry.getParameterTypes()[0].isArray()) {
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter ", methodEntry);
                        methodEntry.invoke(classInstance, new Long(0));
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter OK", methodEntry);
                    } else if (methodEntry.getName().startsWith("set") && methodEntry.getParameterTypes().length == 1
                            && methodEntry.getParameterTypes()[0].equals(java.math.BigDecimal.class)
                            && !methodEntry.getParameterTypes()[0].isArray()) {
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter ", methodEntry);
                        methodEntry.invoke(classInstance, new java.math.BigDecimal(0));
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter OK", methodEntry);
                    } else if (methodEntry.getName().startsWith("set") && methodEntry.getParameterTypes().length == 1
                            && methodEntry.getParameterTypes()[0].equals(java.util.Date.class)
                            && !methodEntry.getParameterTypes()[0].isArray()) {
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter ", methodEntry);
                        methodEntry.invoke(classInstance, new java.util.Date());
                        LoggerFactory.getLogger(this.getClass()).error("==== Field [{0}] sseter OK", methodEntry);
                    }

                } catch (NullPointerException | IllegalAccessException | InstantiationException
                        | ClassNotFoundException | AssertionError e) {
                    LoggerFactory.getLogger(this.getClass()).error(
                            "======== Field [{0}] has getter/sseter problems {1}", methodEntry, e.getMessage());
                }
            }

        } catch (NullPointerException e) {
            // Gracefull text exit
            LoggerFactory.getLogger(this.getClass()).error("======== Test exception {0}", e.getMessage());
        }
        LoggerFactory.getLogger(this.getClass()).debug("================...................============");
        LoggerFactory.getLogger(this.getClass()).info("============ Processing Field {0}",
                pojoClass.getName().substring(pojoClass.getName().lastIndexOf('.')));
    }

}
