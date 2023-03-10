# apidoc-core
该插件用于自动扫描Java接口注释并生成接口信息对象，利用这些接口对象可以构建接口文档。

参考：https://github.com/arsframework/apidoc-maven-plugin

## 1 环境依赖
- Java JDK1.8+

## 2 部署配置
在Maven配置中添加如下依赖：
```
<dependency>
    <groupId>com.arsframework</groupId>
    <artifactId>apidoc-core</artifactId>
    <version>1.3.6</version>
</dependency>
```

## 3 功能描述


## 4 版本更新日志
### v1.3.0
1. 将接口信息处理核心部分拆分成单独的JAR包；

### v1.3.1
1. 增加对@PathVariable注解的解析；

### v1.3.2
1. 更新请求头的默认值设置；

### v1.3.3
1. 修复获取默认参数值时出现异常问题；

### v1.3.4
1. 修复通过lombok获取对象实例异常问题；

### v1.3.5
1. 修复同时使用@PathVariable和@RequestBody注解参数的解析异常问题；

### v1.3.6
1. 修复获取默认值异常问题；

### v1.3.7
1. 优化参数解析逻辑；

### v1.3.8
1. 参数对象新增是否是输入参数属性；
2. 新增参数初始化完成后处理方法；

