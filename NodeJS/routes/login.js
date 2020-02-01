module.exports = function(express, con, crypto){
    var route = express.Router();

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
        var post_data = req.body;

        var id = post_data.id;
        var user_password = post_data.password;

        con.query('SELECT * FROM user where userID=?', [id], function(err, result, fields){
            con.on('error', function(err){
                console.log('[MySQL ERROR]', err); 
            });

            console.log(id+"유저 로그인시도");
            if(result && result.length){
                var salt = result[0].userSalt;
                var encrypted_password = result[0].userPassword;
                var hashed_password = checkHashPassword(user_password, salt).passwordHash;
                if(encrypted_password == hashed_password){
                    console.log(result[0].userName+"유저 로그인성공");
                    res.end(JSON.stringify(result[0]));
                }
                else
                    res.end(JSON.stringify('비밀번호 오류'));
            }
            else{
                res.json('아이디 오류');
            }
        });
    })
    
    return route;
}