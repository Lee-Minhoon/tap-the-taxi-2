module.exports = function(express, con, crypto){
    var route = express.Router();

    // 암호화
    var sha512 = function(password, salt){
        var hash = crypto.createHmac('sha512', salt);
        hash.update(password);
        var value = hash.digest('hex');
        return {
            salt:salt,
            passwordHash:value
        };
    };

    function checkHashPassword(userPassword, salt){
        var passwordData = sha512(userPassword, salt);
        return passwordData;
    }
    
    route.post('', (req, res, next)=>{
        // 데이터 받아옴
        var post_data = req.body;

        // id, password
        var id = post_data.id;
        var user_password = post_data.password;

        // DB 쿼리 userID = req.id 받아옴
        con.query('SELECT * FROM user where userID=?', [id], function(err, result, fields){
            // DB 접속 에러
            con.on('error', function(err){
                console.log('[MySQL ERROR]', err); 
            });

            // DB 접속 성공
            console.log(id+"유저 로그인시도");
            // 쿼리 결과가 있음
            if(result && result.length){
                // 비밀번호 암호화 해제 후
                var salt = result[0].userSalt;
                var encrypted_password = result[0].userPassword;
                var hashed_password = checkHashPassword(user_password, salt).passwordHash;
                // 비밀번호 동일하면 로그인 성공
                if(encrypted_password == hashed_password){
                    console.log(result[0].userName+"유저 로그인성공");
                    res.send(JSON.stringify(result[0]));
                }
                // 비밀번호 오류
                else
                    res.send(JSON.stringify('비밀번호 오류'));
            }
            // 아이디 오류
            else{
                res.json('아이디 오류');
            }
        });
    })
    
    return route;
}