package jsonrpc.dynamic
import groovy.sql.Sql
import com.alibaba.druid.pool.DruidDataSource

M_Util._initdb()

public static getSql(){
    return new Sql(M_Util.ds)
}

class M_Util{
    public static final JDBCURL='dbc:h2:mem:test_mem'
    public static final DruidDataSource ds = new DruidDataSource()

    public static void _initdb(){
        ds.with{
            setUrl 'jdbc:h2:mem:test_mem'
            setUsername 'sa'
            setPassword 'sa'
            setInitialSize 1
            setMinIdle 1
            setMaxActive 5
            init()
        }
        println 'db inited'
    }
}