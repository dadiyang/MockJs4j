package com.github.dadiyang.mockjs;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MockTest {

    @Test
    void mock() {
        Map<String, Object> obj = Mock.mock("{'name': '@city','code|1-1000': 1}");
        System.out.println(JSON.toJSONString(obj, true));
        String city = (String) obj.get("name");
        Number code = (Number) obj.get("code");
        System.out.println(city);
        System.out.println(code);
        assertNotNull(city);
        assertNotNull(code);
    }

    @Test
    void mockForClass() {
        City city = Mock.mock("{'name': '@city','code|1-1000': 1}", City.class);
        System.out.println(city);
        assertNotNull(city);
        assertNotNull(city.getCode());
        assertNotNull(city.getName());
    }

    @Test
    void mockAsList() {
        List<Map<String, Object>> map = Mock.mockAsList("[{'name': '@city','code|1-1000': 1}, {'name': '@city','code|1-1000': 1}]");
        System.out.println(JSON.toJSONString(map));
        assertEquals(2, map.size());
        for (Map<String, Object> obj : map) {
            assertNotNull(obj.get("code"));
            assertNotNull(obj.get("name"));
        }
    }

    @Test
    void mockAsListForClass() {
        List<City> city = Mock.mockAsList("[{'name': '@city','code|1-1000': 1}, {'name': '@city','code|1-1000': 1}]", City.class);
        System.out.println(city);
        assertEquals(2, city.size());
        for (City city1 : city) {
            assertNotNull(city1.getCode());
            assertNotNull(city1.getName());
        }
    }

    @Test
    void mockListWithObj() {
        int size = ThreadLocalRandom.current().nextInt(1, 50);
        List<Map<String, Object>> obj = Mock.mockList("{'city': '@city'}", size);
        String city = (String) obj.get(0).get("city");
        System.out.println(city);
        assertNotNull(city);
    }

    @Test
    void mockListForClass() {
        int size = ThreadLocalRandom.current().nextInt(1, 50);
        List<City> list = Mock.mockList("{'name': '@city','code|1-1000': 1}", City.class, size);
        System.out.println(list);
        assertEquals(size, list.size());
        for (City city : list) {
            assertNotNull(city.getCode());
            assertNotNull(city.getName());
        }
    }
}