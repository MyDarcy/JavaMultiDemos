
## 报错
1. Method com/mchange/v2/c3p0/impl/NewProxyResultSet.isClosed()Z is abstract
> c3p0 版本太低造成,重新下载Maven依赖包; 但是maven.sk.com中的最高版本就是 0.9.1.2

## 配置
2. 配置druid连接池
- https://www.cnblogs.com/wuyun-blog/p/5679073.html
- https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE

```xml
<?xml version="1.0" encoding="UTF-8"?> 
<beans xmlns=" http://www.springframework.org/schema/beans" 
 xmlns:xsi=" http://www.w3.org/2001/XMLSchema-instance" xmlns:batch=" http://www.springframework.org/schema/batch" 
 xsi:schemaLocation=" http://www.springframework.org/schema/beans 
           http://www.springframework.org/schema/beans/spring-beans-4.0.xsd">

 
 <!--获取jdbc配置-->
 <bean id="propertyConfigure" 
  class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer"> 
  <property name="locations"> 
   <list> 
    <value>./conf/application.properties</value> 
   </list> 
  </property> 
 </bean>

 

 <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" 
  init-method="init" destroy-method="close"> 
  <property name="driverClassName" value="${jdbc.driverClassName}" /> 
  <property name="url" value="${jdbc.url}" /> 
  <property name="username" value="${jdbc.username}" /> 
  <property name="password" value="${jdbc.password}" /> 
  <!-- 配置初始化大小、最小、最大 --> 
  <property name="initialSize" value="1" /> 
  <property name="minIdle" value="1" /> 
  <property name="maxActive" value="10" />

  <!-- 配置获取连接等待超时的时间 --> 
  <property name="maxWait" value="10000" />

  <!-- 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 --> 
  <property name="timeBetweenEvictionRunsMillis" value="60000" />

  <!-- 配置一个连接在池中最小生存的时间，单位是毫秒 --> 
  <property name="minEvictableIdleTimeMillis" value="300000" />

  <property name="testWhileIdle" value="true" />

  <!-- 这里建议配置为TRUE，防止取到的连接不可用 --> 
  <property name="testOnBorrow" value="true" /> 
  <property name="testOnReturn" value="false" />

  <!-- 打开PSCache，并且指定每个连接上PSCache的大小 --> 
  <property name="poolPreparedStatements" value="true" /> 
  <property name="maxPoolPreparedStatementPerConnectionSize" 
   value="20" />

  <!-- 这里配置提交方式，默认就是TRUE，可以不用配置 -->

  <property name="defaultAutoCommit" value="true" />

  <!-- 验证连接有效与否的SQL，不同的数据配置不同 --> 
  <property name="validationQuery" value="select 1 " /> 
  <property name="filters" value="stat" /> 
  <property name="proxyFilters"> 
   <list> 
    <ref bean="logFilter" /> 
   </list> 
  </property> 
 </bean>

 

 <bean id="logFilter" class="com.alibaba.druid.filter.logging.Slf4jLogFilter"> 
  <property name="statementExecutableSqlLogEnable" value="false" /> 
 </bean>

</beans>
```
http://note.youdao.com/noteshare?id=540cc9fe34819b5475aa7849df0afccf

3. druid 配置参考
- 监控
```xml
<property name="proxyFilters">
    <list>
        <ref bean="log-filter" />
        <ref bean="stat-filter" />
    </list>
</property>
```

- Slow Query监控
```xml
<bean id="stat-filter" class="com.alibaba.druid.filter.stat.StatFilter">
    <property name="slowSqlMillis" value="10000" />
    <property name="logSlowSql" value="true" />
</bean>
```

- Sql合并
```xml
<bean id="stat-filter" class="com.alibaba.druid.filter.stat.StatFilter">
    <property name="mergeSql" value="true" />
</bean>
```

- 打印Sql
 ```xml
<bean id="log-filter" class="com.alibaba.druid.filter.logging.Slf4jLogFilter">
    <property name="statementExecutableSqlLogEnable" value="true" />
</bean>
```

- 周期性输出统计信息到日志中
```xml
<bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close"> 
    <property name="timeBetweenLogStatsMillis" value="300000" />
</bean>
```

- Druid 推荐配置
```xml
<bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource"
        init-method="init" destroy-method="close">
        <!-- 基本属性 url、user、password -->
        <property name="url" value="${jdbc_url}" />
        <property name="username" value="${jdbc_user}" />
        <property name="password" value="${jdbc_password}" />

        <!-- 配置初始化大小、最小、最大 -->
        <property name="initialSize" value="5" />
        <property name="minIdle" value="5" />
        <property name="maxActive" value="10" />
        <!-- 配置从连接池获取连接等待超时的时间 -->
        <property name="maxWait" value="10000" />

        <!-- 配置间隔多久启动一次DestroyThread，对连接池内的连接才进行一次检测，单位是毫秒。
            检测时:1.如果连接空闲并且超过minIdle以外的连接，如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。2.在minIdle以内的不处理。
        -->
        <property name="timeBetweenEvictionRunsMillis" value="600000" />
        <!-- 配置一个连接在池中最大空闲时间，单位是毫秒 -->
        <property name="minEvictableIdleTimeMillis" value="300000" />
        <!-- 设置从连接池获取连接时是否检查连接有效性，true时，每次都检查;false时，不检查 -->
        <property name="testOnBorrow" value="false" />
        <!-- 设置往连接池归还连接时是否检查连接有效性，true时，每次都检查;false时，不检查 -->
        <property name="testOnReturn" value="false" />
        <!-- 设置从连接池获取连接时是否检查连接有效性，true时，如果连接空闲时间超过minEvictableIdleTimeMillis进行检查，否则不检查;false时，不检查 -->
        <property name="testWhileIdle" value="true" />
        <!-- 检验连接是否有效的查询语句。如果数据库Driver支持ping()方法，则优先使用ping()方法进行检查，否则使用validationQuery查询进行检查。(Oracle jdbc Driver目前不支持ping方法) -->
        <property name="validationQuery" value="select 1 from dual" />
        <!-- 单位：秒，检测连接是否有效的超时时间。底层调用jdbc Statement对象的void setQueryTimeout(int seconds)方法 -->
        <!-- <property name="validationQueryTimeout" value="1" />  -->

        <!-- 打开后，增强timeBetweenEvictionRunsMillis的周期性连接检查，minIdle内的空闲连接，每次检查强制验证连接有效性. 参考：https://github.com/alibaba/druid/wiki/KeepAlive_cn -->
        <property name="keepAlive" value="true" />  

        <!-- 连接泄露检查，打开removeAbandoned功能 , 连接从连接池借出后，长时间不归还，将触发强制回连接。回收周期随timeBetweenEvictionRunsMillis进行，如果连接为从连接池借出状态，并且未执行任何sql，并且从借出时间起已超过removeAbandonedTimeout时间，则强制归还连接到连接池中。 -->
        <property name="removeAbandoned" value="true" /> 
        <!-- 超时时间，秒 -->
        <property name="removeAbandonedTimeout" value="80"/>
        <!-- 关闭abanded连接时输出错误日志，这样出现连接泄露时可以通过错误日志定位忘记关闭连接的位置 -->
        <property name="logAbandoned" value="true" />

        <!-- 根据自身业务及事务大小来设置 -->
        <property name="connectionProperties"
            value="oracle.net.CONNECT_TIMEOUT=2000;oracle.jdbc.ReadTimeout=10000"></property>

        <!-- 打开PSCache，并且指定每个连接上PSCache的大小，Oracle等支持游标的数据库，打开此开关，会以数量级提升性能，具体查阅PSCache相关资料 -->
        <property name="poolPreparedStatements" value="true" />
        <property name="maxPoolPreparedStatementPerConnectionSize"
            value="20" />   

        <!-- 配置监控统计拦截的filters -->
        <!-- <property name="filters" value="stat,slf4j" /> -->

        <property name="proxyFilters">
            <list>
                <ref bean="log-filter" />
                <ref bean="stat-filter" />
            </list>
        </property>
        <!-- 配置监控统计日志的输出间隔，单位毫秒，每次输出所有统计数据会重置，酌情开启 -->
        <property name="timeBetweenLogStatsMillis" value="120000" />
    </bean>
```

