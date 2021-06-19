module.exports = function(express, con, crypto, uuid, mysql){
    var route = express.Router();
    
    // 암호화
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
    
    // 회원가입
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

        // 아이디 존재하는지 검색
        con.query('SELECT * FROM user where userID=?', [id], function(err, result, fields){
            // throw 구문
            con.on('error', function(err){
                console.log('[MySQL ERROR]', err); 
            });

            // 아이디 존재함
            if(result && result.length)
                res.json('존재하는 ID입니다.');
            // 아이디 존재하지 않음
            else{
                // INSERT 구문 실행
                var sql = 'INSERT INTO user (userUID, userName, userID, userPassword, userSalt, userTel, userCreate, userUpdate) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())';
                var params = [uid, name, id, password, salt, tel];
                con.query(sql, params, function(err, result, fields){
                    // DB 접속 실패
                    con.on('error', function(err){
                        console.log('[MySQL ERROR]', err);
                        res.json('회원가입 실패: ', err);
                    });
                    res.json('회원가입 성공!!');
                    
                    insertId = result.insertId;
                    console.log(insertId+'번 유저 생성됨');
                    
                    // 계좌 INSERT
                    var sql = 'INSERT INTO account (userNo, accountNum, accountBalance) VALUES (?, ?, ?)';
                    var params = [insertId, account, 100000000];
                    
                    // DB 접속 실패
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