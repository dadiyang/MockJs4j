# Java 版 MockJs

底层采用 ScriptEngine 执行封装的 [mockjs](http://mockjs.com/) 脚本

模板语法请查看官方示例：http://mockjs.com/examples.html

# 使用

## 使用模板生成 map 对象

```java
    String tpl = "{'name': '@city','code|1-1000': 1}";
    Map<String, Object> obj = Mock.mock(tpl);

    // 指定类型
    City city = Mock.mock(tpl, City.class);
```

效果：
```json
{
    "name":"莱芜市",
    "code":289.0
}
```

## 根据模板生成列表

```java
    String tpl = "[{'name': '@city','code|1-1000': 1}, {'name': '@city','code|1-1000': 1}]";
    List<Map<String, Object>> map = Mock.mockAsList(tpl);
    // 指定类型
    List<City> city = Mock.mockAsList(tpl, City.class);
```

## 根据模板生成指定元素数量的对象列表

```java
    String tpl = ""{'name': '@city','code|1-1000': 1}"";
    List<Map<String, Object>> obj = Mock.mockList(tpl, 10);

    // 指定类型
    List<City> list = Mock.mockList(tpl, City.class, 10);
    
```