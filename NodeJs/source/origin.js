var genRandomString = function(length){
    return crypto.randomBytes(Math.ceil(length/2))
    .toString('hex')
    .slice(0, length);
};

var sha512 = function(password, salt){
    var hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    var value = hash.digest('hex');
    return {
        salt:salt,
        passwordHash:value
    };
};

function saltHashPassword(userPassword){
    var salt = genRandomString(16);
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function checkHashPassword(userPassword, salt){
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

app.post('/login/', (req, res, next)=>{
    var post_data = req.body;
    
    var id = post_data.id;
    var user_password = post_data.password;
    
    con.query('SELECT * FROM user where userID=?', [id], function(err, result, fields){
        con.on('error', function(err){
            console.log('[MySQL ERROR]', err); 
        });
        
        console.log(result[0]);
        if(result && result.length){
            console.log("true");
            var salt = result[0].userSalt;
            var encrypted_password = result[0].userPassword;
            var hashed_password = checkHashPassword(user_password, salt).passwordHash;
            if(encrypted_password == hashed_password){
                res.end(JSON.stringify(result[0]));
            }
            else
                res.end(JSON.stringify('Wrong password'));
        }
        else{
            res.json('User not exists!!!');
        }
    });
})

app.post('/register/', (req, res, next)=>{
    var post_data = req.body;
    
    var uid = uuid.v4();
    var plaint_password = post_data.password;
    var hash_data = saltHashPassword(plaint_password);
    var password = hash_data.passwordHash;
    var salt = hash_data.salt;
    
    var name = post_data.name;
    var id = post_data.id;
    var tel = post_data.tel;
    var account = post_data.account;
    
    con.query('SELECT * FROM user where userID=?', [id], function(err, result, fields){
        con.on('error', function(err){
            console.log('[MySQL ERROR]', err); 
        });
        
        if(result && result.length)
            res.json('User already exists!!!');
        else{
            var sql = 'INSERT INTO user (userUID, userName, userID, userPassword, userSalt, userTel, userAccount, userCreate, userUpdate) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())';
            var params = [uid, name, id, password, salt, tel, account];
            con.query(sql, params, function(err, result, fields){
                con.on('error', function(err){
                    console.log('[MySQL ERROR]', err);
                    res.json('Register Error: ', err);
                });
                res.json('Register succesful');
            });
        }
    });
})

console.log("로그5, 방 인원체크: "+i+"번째 방");
                                        var sql = 'SELECT * FROM room_has_user where roomNo=?';
                                        console.log("디버그1: "+i);
                                        var params = [result[i].roomNo];
                                        console.log("디버그2: "+i);
                                        con.query(sql, params, function(err, result, fields){
                                            con.on('error', function(err){
                                                console.log('[MySQL ERROR]', err);
                                            });
                                            //4명이하인 방이 있다면 참가함
                                            console.log("디버그3: "+i);
                                            console.log("로그6, "+i+"번째 방 인원: "+result.length);
                                            if(result.length < 4){
                                                var sql = 'INSERT INTO room_has_user (roomNo, userNo) VALUES (?, ?)';
                                                var params = [result[i].roomNo, no];
                                                con.query(sql, params, function(err, result, fields){
                                                    con.on('error', function(err){
                                                        console.log('[MySQL ERROR]', err);
                                                    });
                                                    console.log("로그7, "+value+"번째 방에 참가함");
                                                    res.json('방에 참가함');
                                                });
                                            }else{
                                                res.json('방에 참가못해');
                                            }
                                        });