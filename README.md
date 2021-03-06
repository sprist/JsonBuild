# JsonBuild
轻量级http解析

Download
--------

Download grab via Maven:
```xml
<dependency>
  <groupId>com.apcan.sprist</groupId>
  <artifactId>jsonBuilder</artifactId>
  <version>1.0.2</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.apcan.sprist:jsonBuilder:1.0.2'
```

For the SNAPSHOT version:
```xml
<dependency>
  <groupId>com.apcan.sprist</groupId>
  <artifactId>jsonBuilder</artifactId>
  <version>1.0.2</version>
</dependency>
```
or Gradle:
```groovy
allprojects {
  repositories {
           maven { url "http://www.apcan.cn:8081/nexus/content/groups/AndroidSnapshot/" }
  }
}
```
```groovy
dependencies {
  compile 'com.apcan.sprist:jsonBuilder:1.0.2'
}
```
 

开启调试log(默认不打印)
--------

```java
JsonInject.setDebug(debug)
```

使用
--------

1.从前
--------

```java
public class ListItemRegisterDepartModel {
    public String dept_code;
    public String dept_name;
    public int i;

    public ListItemRegisterDepartModel(JSONObject obj ){
        this.dept_name = obj.optString("dept_name");
        this.dept_code = obj.optString("dept_code");
        this.i = obj.optInt("i");
    }
}
```

2.现在
--------

```java
public class ListItemRegisterDepartModel {
    @JsonBuilder public String dept_code;//默认使用成员变量名
    @JsonBuilder("dept_name") public String dept_name;
    @JsonBuilder public int i = 0;//默认值 直接设置

    public ListItemRegisterDepartModel(JSONObject obj ){
        JsonInject.inject(this, obj);
    }
}
```

3.自动生成部分
--------

```java
public class ListItemRegisterDepartModel$$JsonBuilder {
  public static void inject(Finder finder, final com.ucmed.qingdao.model.ListItemRegisterDepartModel target, JSONObject source) {
    Object object;
    object = finder.opt(source, "i");
    if (object != null) {
    target.i = (java.lang.Integer) object;
    }
    object = finder.opt(source, "dept_code");
    if (object != null) {
    target.dept_code = (java.lang.String) object;
    }
    object = finder.opt(source, "dept_name");
    if (object != null) {
    target.dept_name = (java.lang.String) object;
    }
  }
}
```

4.混淆
-----------

```xml
-dontwarn com.yaming.json.internal.**
-keep class **$$JsonBuilder { *; }
-keepnames class * { @com.yaming.json.JsonBuilder *;}
```




