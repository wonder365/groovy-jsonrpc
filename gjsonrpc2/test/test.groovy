package jsonrpc.dynamic

static getDate(){
    return new Date()
}

static fun1arg(int a){
    return a+1
}


static int subtract(int a, int b){
    a-b
}

static int add(int a, int b){
    a+b
}

static int adds(List args){
    checkArgument(args.size() > 2, 'args count must > 2')
    args.sum()
}

static notify_sum(List args){
    args.sum()
}

static sum(List args){
    args.sum()
}

static donotify(){
    println 'notify invoked'
    return 'this is from notify'
}

static echo(args){
    args
}

static nullf(){
    [a:1, b:2, c:null]
}