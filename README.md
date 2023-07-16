# debezium-demo

内嵌部分 debezium 2.3.0源码(jdk11转jdk8编译)
offset存redis, history存内存, 数据dml同步上报

history存redis因服务器redis版本不支持stream(版本小于5)不可用