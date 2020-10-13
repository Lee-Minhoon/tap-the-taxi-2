module.exports = function(express, con, crypto, uuid, mysql){
    var route = express.Router();
    
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
    
    route.post('', (req, res, next)=>{
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
        
        var insertId;

        con.query('SELECT * FROM user where userID=?', [id], function(err, result, fields){
            con.on('error', function(err){
                console.log('[MySQL ERROR]', err); 
            });

            if(result && result.length)
                res.json('존재하는 ID입니다.');
            else{
                var sql = 'INSERT INTO user (userUID, userName, userID, userPassword, userSalt, userTel, userCreate, userUpdate) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())';
                var params = [uid, name, id, password, salt, tel];
                con.query(sql, params, function(err, result, fields){
                    con.on('error', function(err){
                        console.log('[MySQL ERROR]', err);
                        res.json('회원가입 실패: ', err);
                    });
                    res.json('회원가입 성공!!');
                    
                    insertId = result.insertId;
                    console.log(insertId+'번 유저 생성됨');
                    
                    var sql = 'INSERT INTO account (userNo, accountNum, accountBalance) VALUES (?, ?, ?)';
                    var params = [insertId, account, 100000000];
                    con.query(sql, params, function(err, result, fields){
                        con.on('error', function(err){
                            console.log('[MySQL ERROR]', err);
                        });
                    })
                });
            }   
        });
    })
    
    return route;
}