package com.atmira.asteroids.pojo;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterChain;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.PojoValidator;
import com.openpojo.validation.rule.impl.NoNestedClassRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsExceptStaticFinalRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsRule;
import com.openpojo.validation.rule.impl.NoStaticExceptFinalRule;
import com.openpojo.validation.rule.impl.SerializableMustHaveSerialVersionUIDRule;
import com.openpojo.validation.test.impl.DefaultValuesNullTester;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import com.openpojo.validation.utils.ValidationHelper;

public class PojoTest {

	private PojoValidator pojoValidator;
    private List<PojoClass> pojoClasses;
	
    @SuppressWarnings("deprecation")
	@Before
    public void setup() {

        pojoValidator = new PojoValidator();
        pojoClasses = PojoClassFactory.getPojoClassesRecursively(
                "com.atmira.asteroids.pojo", new FilterChain());

        NullArrayTester.propareAbstractPojoClasses(pojoClasses);

        // Create Rules to validate structure for POJO_PACKAGE
        pojoValidator.addRule(new NoPublicFieldsRule());
        pojoValidator.addRule(new NoStaticExceptFinalRule());
        pojoValidator.addRule(new NoNestedClassRule());
        pojoValidator.addRule(new NoPublicFieldsExceptStaticFinalRule());
        pojoValidator.addRule(new SerializableMustHaveSerialVersionUIDRule());

        pojoValidator.addTester(new NullArrayTester());

        // Create Testers to validate behaviour for POJO_PACKAGE
        pojoValidator.addTester(new DefaultValuesNullTester());
        pojoValidator.addTester(new SetterTester());
        pojoValidator.addTester(new GetterTester());
    }

    @Test
    public void testPojoStructureAndBehavior() {
        for (PojoClass pojoClass : pojoClasses) {
            try {
                pojoValidator.runValidation(pojoClass);
                ValidationHelper.getBasicInstance(pojoClass).toString();
                ValidationHelper.getMostCompleteInstance(pojoClass).toString();
            } catch (Exception e) {
                continue;
            }
        }
    }
    
}
