package com.github.dadiyang.mockjs;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支持 Mockjs 的 Mock.mock 方法
 * <p>
 * 原理: 使用 ScriptEngine 执行封装的 mockjs 脚本
 *
 * @author huangxuyang
 * @since 2021/2/11
 */
public class Mock {
    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("javascript");
    private static final String MOCK_JS_PREFIX = "Mock.mock(";
    private static final String JSON_OBJECT_PREFIX = "{";
    private static final String JSON_ARRAY_PREFIX = "[";

    private Mock() {
        throw new UnsupportedOperationException("静态工具类不允许被实例化");
    }

    static {
        try (InputStream is = Mock.class.getClassLoader().getResourceAsStream("script/mock-min.js")) {
            if (is != null) {
                List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
                String tpl = String.join("\n", lines);
                SCRIPT_ENGINE.eval(tpl);
            }
        } catch (Exception e) {
            throw new IllegalStateException("加载 mockjs 脚本出错", e);
        }
    }

    /**
     * 执行 mockjs 脚本
     *
     * @param tpl mockjs模板脚本，必须是一个JSON对象或数组，或者以 Mock.mock( 直接调用 mockjs api 的形式提供
     * @return 如果模板是一个对象，则为 key->value，如果是数组则为 index -> key -> value
     * @throws IllegalArgumentException tpl 为空或不是 JSON 对象或数组且不以 Mock.mock( 时抛出非法参数异常
     * @throws IllegalStateException    脚本执行失败时抛出此异常
     */
    public static Map<String, Object> mock(String tpl) {
        checkMockTpl(tpl);
        tpl = tpl.trim();
        // 添加 Mock.mock() 调用
        if (tpl.startsWith(JSON_OBJECT_PREFIX) || tpl.startsWith(JSON_ARRAY_PREFIX)) {
            tpl = MOCK_JS_PREFIX + tpl + ")";
        }
        try {
            //noinspection unchecked
            return (Map<String, Object>) SCRIPT_ENGINE.eval(tpl);
        } catch (ScriptException e) {
            throw new IllegalStateException("脚本执行失败" + e.getMessage() + " 脚本: " + tpl, e);
        }
    }

    /**
     * 执行 mockjs 脚本
     * <p>
     * 注：此方法依赖 FastJson
     *
     * @param tpl mockjs模板脚本，必须是一个JSON对象或数组，或者以 Mock.mock( 直接调用 mockjs api 的形式提供
     * @throws IllegalArgumentException tpl 为空或不是 JSON 对象或数组且不以 Mock.mock( 时抛出非法参数异常
     * @throws IllegalStateException    脚本执行失败时抛出此异常
     */
    public static <T> T mock(String tpl, Class<T> clazz) {
        Object obj = mock(tpl);
        return JSON.parseObject(JSON.toJSONString(obj), clazz);
    }

    /**
     * 通过 mockjs 模板生成一个对象列表
     *
     * @param tpl mockjs模板，必须是一个 JSON 数组
     * @throws IllegalArgumentException tpl 为空或不是 JSON 对象时抛出
     * @throws IllegalStateException    脚本执行失败时抛出此异常
     */
    public static List<Map<String, Object>> mockAsList(String tpl) {
        if (StringUtils.isBlank(tpl)) {
            throw new IllegalArgumentException("模板不能为空");
        }
        tpl = tpl.trim();
        if (!tpl.startsWith(JSON_ARRAY_PREFIX)) {
            throw new IllegalArgumentException("模板有误，应是JSON数组");
        }
        Map<String, Object> map = mock(tpl);
        List<Map<String, Object>> list = new ArrayList<>(map.size());
        for (int i = 0; i < map.size(); i++) {
            //noinspection unchecked
            list.add((Map<String, Object>) map.get(String.valueOf(i)));
        }
        return list;
    }

    /**
     * 通过 mockjs 模板生成一个指定类型的对象列表
     *
     * @param tpl mockjs模板脚本，必须是一个 JSON 数组，或者以 Mock.mock( 直接调用 mockjs api 的形式提供
     * @throws IllegalArgumentException tpl 为空或不是 JSON 对象或数组且不以 Mock.mock( 时抛出非法参数异常
     * @throws IllegalStateException    脚本执行失败时抛出此异常
     */
    public static <T> List<T> mockAsList(String tpl, Class<T> clazz) {
        if (StringUtils.isBlank(tpl)) {
            throw new IllegalArgumentException("模板不能为空");
        }
        tpl = tpl.trim();
        if (!tpl.startsWith(JSON_ARRAY_PREFIX)) {
            throw new IllegalArgumentException("模板有误，应是JSON数组");
        }
        List<Map<String, Object>> list = mockAsList(tpl);
        return JSON.parseArray(JSON.toJSONString(list), clazz);
    }

    /**
     * 通过 mockjs 模板生成指定元素数量的对象列表
     *
     * @param tpl  mockjs 模板，可以是一个对象或者是列表
     * @param size 大小
     * @return 生成的对象列表
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> mockList(String tpl, int size) {
        if (StringUtils.isBlank(tpl)) {
            throw new IllegalArgumentException("模板不能为空");
        }
        tpl = tpl.trim();
        if (!tpl.startsWith(JSON_OBJECT_PREFIX) && !tpl.startsWith(JSON_ARRAY_PREFIX)) {
            throw new IllegalArgumentException("模板有误，Mockjs 模板应是JSON对象或JSON数组");
        }
        if (tpl.trim().startsWith(JSON_OBJECT_PREFIX)) {
            tpl = "[" + tpl + "]";
        }
        tpl = "{'list|" + size + "': " + tpl + "}";
        Map<String, Object> arr = Mock.mock(tpl);
        return ((Map<String, Object>) arr.get("list")).entrySet().stream()
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey())))
                .map(e -> (Map<String, Object>) e.getValue())
                .collect(Collectors.toList());
    }

    /**
     * 通过 mockjs 模板生成指定类型指定元素数量的对象列表
     *
     * @param tpl  mockjs 模板，可以是一个对象或者是列表
     * @param size 元素数量
     * @return 生成的对象列表
     */
    public static <T> List<T> mockList(String tpl, Class<T> clazz, int size) {
        List<Map<String, Object>> list = mockList(tpl, size);
        return JSON.parseArray(JSON.toJSONString(list), clazz);
    }

    private static void checkMockTpl(String tpl) {
        if (StringUtils.isBlank(tpl)) {
            throw new IllegalArgumentException("模板不能为空");
        }
        tpl = tpl.trim();
        if (!tpl.startsWith(MOCK_JS_PREFIX) && !tpl.startsWith(JSON_OBJECT_PREFIX) && !tpl.startsWith(JSON_ARRAY_PREFIX)) {
            throw new IllegalArgumentException("模板有误，Mockjs模板应是JSON对象或JSON数组");
        }
    }
}
