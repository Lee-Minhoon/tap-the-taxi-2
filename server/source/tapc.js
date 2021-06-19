/*jslint devel: true */
/* eslint-disable no-console */
/*eslint no-undef: "error"*/
/*eslint-env node*/

//moduleSet
var express = require('express'),
    app = require('express')(),
    mysql = require('mysql'),
    mysql2 = require('sync-mysql'),
    bodyParser = require('body-parser'),
    cookieParser = require('cookie-parser'),
    expressSession = require('express-session'),
    crypto = require('crypto'),
    uuid = require('uuid'),
    http = require('http').createServer(app),
    io = require('socket.io')(http);

//connectDB
var con = mysql.createConnection({
    host : 'localhost',
    user : 'root',
    password : '1234',
    database : 'tapc'
});

var con2 = new mysql2({
    host : 'localhost',
    user : 'root',
    password : '1234',
    database : 'tapc'
})

//appSet
app.set('port', process.env.PORT || 3000);
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(cookieParser());
app.use(expressSession({
    secret:'my key',
    resave:true,
    saveUninitialized:true
}));

app.get('/', function(req, res){
    res.sendFile(__dirname+'/index.html');
})

let session;
let result;
let roomNum;
let roomExist;

io.sockets.on("connection", function(socket){
    socket.on("joinRoom", function(session){
        result = con2.query('SELECT * FROM user where userID=?', [session]);
        session = result[0].userNo;
        console.log(session+'번 유저 소켓통신시도');
        while(1){
            if(roomExist){
                socket.join(roomNum);
                console.log('소켓통신시작/ 방에 입장: '+roomNum);
                //io.to(roomNum).emit('message_from_server', '?');
                roomExist = null;
                break;
            }else{
                roomExist = con2.query('SELECT * FROM room_has_user where userNo=?', [session]);
                roomNum = roomExist[0].roomNo;
                console.log(roomNum+'방 조회!!');
            }
        }
    });
    socket.on("message_from_client", function(msg, get){
        console.log(get+': '+msg);
        var chat = {
            'name': get,
            'chat': msg
        };
        io.sockets.to(roomNum).emit("message_from_server", chat);
    });
    socket.on("money", function(money){
        console.log('송금요청: ', money);
        var v = {
            'request_money': money / 4
        };
        socket.broadcast.to(roomNum).emit("request_money", v);
    })
    socket.on("moneyOK", function(money, session){
        result = con2.query('SELECT * FROM user where userID=?', [session]);
        userNo = result[0].userNo;
        console.log(userNo);
        result = con2.query('SELECT * FROM account where userNo=?', [userNo]);
        var balance = result[0].accountBalance - money;
        console.log(balance);
        con2.query('UPDATE account SET accountBalance=? where userNo=?', [balance, result[0].userNo]);
        console.log('송금OK');
    })
});

//postLogin
var login = require('./routes/login')(express, con, crypto);
app.use('/login', login);

//postRegister
var register = require('./routes/register')(express, con, crypto, uuid, mysql);
app.use('/register', register);

//postProfile
var profile = require('./routes/profile')(express, con);
app.use('/profile', profile);

//postAccount
var account = require('./routes/account')(express, con);
app.use('/account', account);

//postRomm
var room = require('./routes/room')(express, con2);
app.use('/room', room);

var roomExit = require('./routes/roomExit')(express, con2);
app.use('/roomExit', roomExit);

//serverStart
http.listen(app.get('port'), function(){
    console.log('익스프레스 서버를 시작했습니다 : ' + app.get('port'));
});