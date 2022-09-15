### 使用说明
#### 在项目中配置资源地址
```
maven { url = uri("https://gitee.com/liu-huiliang/jarlibs/raw/master") }
```
#### 在模块中引用
```
implementation 'com.lhl.imagecheck:imagecheck:1.0.0'
```

#### 初始化    
        不需要初始化，会自动初始化
#### 作用  
        检测项目中图片大小比控件大并且打印对应日志，可以用ImageCheck过滤日志
        