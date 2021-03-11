package org.treblereel.j2cl.shim;

import com.google.j2cl.junit.apt.J2clTestInput;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 1/27/21
 */
@J2clTestInput(ES6ModuleTest.class)
public class ES6ModuleTest {

    private ES6Test tested = new ES6Test();
    private ES6Test2 testedWithJsName = new ES6Test2();
    private ES6TransitiveTest es6TransitiveTest = new ES6TransitiveTest();
    private ES6TestUserDefinedEs6ModuleAnnotation es6TestUserDefinedEs6ModuleAnnotation = new ES6TestUserDefinedEs6ModuleAnnotation();

    @Test
    public void testBooleanMethod() {
        assertEquals(true, tested.isTest());
        assertEquals(true, testedWithJsName.isTestZZ());
    }

    @Test
    public void testClassStringProperty() {
        assertEquals("#id", tested.id);
        assertEquals("#id2", testedWithJsName.id);
        assertEquals("#id", es6TransitiveTest.getEs6Testid());
        assertEquals("#id2", es6TransitiveTest.getEs6Test2id());

        assertEquals("#es6testuserdefinedes6moduleannotation", es6TestUserDefinedEs6ModuleAnnotation.id);
    }
}
