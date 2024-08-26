import com.example.DI.BeanDefinition;
import com.example.DI.BeanFactory;
import com.example.annotations.*;
import com.example.enums.Scope;
import exceptions.CircularDependencyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BeanFactoryTest {
    @Test
    void testSingletonBeanCreation() throws Exception {
        var factory = new BeanFactory();
        var definition = new BeanDefinition(MyComponent.class, Scope.SINGLETON, null);
        var className = MyComponent.class.getName();

        factory.createBean(className, definition, MyComponent.class);

        var bean1 = factory.getComponent(className);
        var bean2 = factory.getComponent(className);

        Assertions.assertNotNull(bean1);
        Assertions.assertSame(bean1, bean2); // Singleton beans should be the same instance
    }

    @Component
    public static class MyComponent {
    }

    @Test
    void testPrototypeBeanCreation() throws Exception {
        var factory = new BeanFactory();
        var definition = new BeanDefinition(MyPrototypeComponent.class, Scope.PROTOTYPE, null);
        var className = MyPrototypeComponent.class.getName();

        factory.createBean(className, definition, MyPrototypeComponent.class);

        var bean1 = factory.getComponent(className);
        var bean2 = factory.getComponent(className);

        Assertions.assertNotNull(bean1);
        Assertions.assertNotSame(bean1, bean2); // Prototype beans should be different instances
    }

    @Component(scope = Scope.PROTOTYPE)
    public static class MyPrototypeComponent {
    }

    @Test
    void testDependencyInjection() throws Exception {
        var factory = new BeanFactory();

        // Register dependency first
        var depDefinition = new BeanDefinition(DependencyComponent.class, Scope.SINGLETON, null);
        factory.createBean(DependencyComponent.class.getName(), depDefinition, DependencyComponent.class);

        // Register the main component
        var mainDefinition = new BeanDefinition(MainComponent.class, Scope.SINGLETON, null);
        factory.createBean(MainComponent.class.getName(), mainDefinition, MainComponent.class);
        factory.injectDependencies();

        MainComponent mainComponent = factory.getComponent(MainComponent.class.getName());

        Assertions.assertNotNull(mainComponent);
        Assertions.assertNotNull(mainComponent.dependency); // Dependency should be injected
    }

    @Component
    public static class MainComponent {
        @Autowired
        public DependencyComponent dependency;
    }

    @Component
    public static class DependencyComponent {
    }

    @Test
    void testCircularDependencyDetection() {
        var factory = new BeanFactory();

        // Register Component A and B which depend on each other to simulate circular dependency
        var aDefinition = new BeanDefinition(ComponentA.class, Scope.SINGLETON, null);
        var bDefinition = new BeanDefinition(ComponentB.class, Scope.SINGLETON, null);

        factory.createBean(ComponentA.class.getName(), aDefinition, ComponentA.class);
        factory.createBean(ComponentB.class.getName(), bDefinition, ComponentB.class);

        Assertions.assertThrows(CircularDependencyException.class, factory::injectDependencies);
    }

    @Component
    public static class ComponentA {
        @Autowired
        private ComponentB componentB;
    }

    @Component
    public static class ComponentB {
        @Autowired
        private ComponentA componentA;
    }
}
