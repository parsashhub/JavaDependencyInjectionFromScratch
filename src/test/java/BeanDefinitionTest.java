import com.example.DI.BeanDefinition;
import com.example.enums.Scope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BeanDefinitionTest {
    @Test
    void testBeanDefinitionCreation() {
        var definition = new BeanDefinition(String.class, Scope.SINGLETON, null);
        assertEquals(String.class, definition.getBeanClass());
        assertEquals(Scope.SINGLETON, definition.getScope());
        assertNull(definition.getQualifier());
    }
}
