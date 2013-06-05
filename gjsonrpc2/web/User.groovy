package jsonrpc.dynamic
import static jsonrpc.dynamic.Util.*

public static dosleep(int s){
    sleep(s)
    s
}

public static create(){
    def sql = getSql()
    sql.execute(M_User.SQL_USER_CREATE)
    true
}

public static adduser(String name, String phone){
    def sql = getSql()
    sql.execute(M_User.SQL_USER_INSERT, name, phone)
    true
}

public static listuser(){
    def sql = getSql()
    sql.rows(M_User.SQL_USER_LIST).collect {
        new M_User.User(id:it.id, name:it.name, phone:it.phone)
    }
}

class M_User{
    public static class User{int id; String name; String phone;}
    public static SQL_USER_LIST='select * from tb_user'
    public static SQL_USER_CREATE='create table tb_user(ID INT IDENTITY, NAME VARCHAR(255), phone VARCHAR(255))'
    public static SQL_USER_INSERT='insert into tb_user(name, phone)values(?, ?)'
}