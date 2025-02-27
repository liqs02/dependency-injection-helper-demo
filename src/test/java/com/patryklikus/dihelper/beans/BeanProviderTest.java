/* Copyright patryklikus.com All Rights Reserved. */
package com.patryklikus.dihelper.beans;

import com.google.common.reflect.TypeToken;
import com.patryklikus.dihelper.beans.exampleProject.Main;
import com.patryklikus.dihelper.beans.exampleProject.models.Apple;
import com.patryklikus.dihelper.beans.exampleProject.models.Color;
import com.patryklikus.dihelper.beans.exampleProject.models.Fruit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class BeanProviderTest {
    private BeanProvider beanProvider;

    @BeforeEach
    void setUp() {
        beanProvider = new BeanProviderImpl(Main.class);
        beanProvider.init();
    }

    @Test
    @DisplayName("Should contain correct number of beans in storage")
    void getBeansTest() throws NoSuchFieldException, IllegalAccessException {
        Field beansField = BeanProviderImpl.class.getDeclaredField("beans");
        beansField.setAccessible(true);

        var storage = (Map<?, ?>) beansField.get(beanProvider);

        assertEquals(7, storage.size());
    }

    @Test
    @DisplayName("Should return each bean with correct value and type")
    void getBeanTest() {
        Fruit apple = beanProvider.getBean("apple", Fruit.class).value();
        String red = beanProvider.getBean("red", String.class).value();
        Color redColor = beanProvider.getBean("redColor", Color.class).value();
        List<String> textList = beanProvider.getBean("textList", new TypeToken<List<String>>() {
        }).value();
        String text = beanProvider.getBean("text", String.class).value();
        int[] numbers = beanProvider.getBean("numbers", int[].class).value();
        int sum = beanProvider.getBean("sum", int.class).value();

        assertNotNull(apple);
        assertEquals("red", red);
        assertEquals("red", redColor.getAsText());
        assertEquals("Hello World !", text);
        assertEquals(List.of("Hello", "World", "!"), textList);
        assertArrayEquals(new int[]{1, 2, 3}, numbers);
        assertEquals(6, sum);
    }

    @Test
    @DisplayName("Shouldn't find bean if invalid type is provided")
    void getBeanInvalidGenericTest() {
        var apple = beanProvider.getBean("apple", Apple.class);
        var result = beanProvider.getBean("textList", new TypeToken<List<Integer>>() {
        });

        assertNull(apple);
        assertNull(result);
    }

    @Test
    @DisplayName("Should return beans with proper configuration")
    void getBeanLifePriorityTest() {
        Bean<String> defaultBean = beanProvider.getBean("red", String.class);
        Bean<Color> customBean = beanProvider.getBean("redColor", Color.class);

        // default config
        assertTrue(defaultBean.initConfig().isEnabled());
        assertEquals(0, defaultBean.initConfig().getOrder());

        assertTrue(defaultBean.runConfig().isEnabled());
        assertEquals(0, defaultBean.runConfig().getDelay());
        assertEquals(0, defaultBean.runConfig().getRepetitionPeriod());
        assertEquals(SECONDS, defaultBean.runConfig().getTimeUnit());

        assertTrue(defaultBean.closeConfig().isEnabled());
        assertEquals(0, defaultBean.closeConfig().getOrder());
        // with annotations
        assertFalse(customBean.initConfig().isEnabled());
        assertEquals(1, customBean.initConfig().getOrder());

        assertFalse(customBean.runConfig().isEnabled());
        assertEquals(0, customBean.runConfig().getDelay());
        assertEquals(0, customBean.runConfig().getRepetitionPeriod());
        assertEquals(SECONDS, customBean.runConfig().getTimeUnit());

        assertFalse(customBean.closeConfig().isEnabled());
        assertEquals(3, customBean.closeConfig().getOrder());
    }
}
