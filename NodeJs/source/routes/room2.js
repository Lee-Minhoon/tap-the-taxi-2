module.exports = function(express, con){
    var route = express.Router();
    
    route.post('', (req, res, next)=>{
        var post_data = req.body;

        var id = post_data.id;
        var x = post_data.x;
        var y = post_data.y;
        
        var no;
        var insertId;

        console.log("로그1, 받아온 값: "+id, x, y);
        //세션검색으로 유저넘버 들고옴
        con.query('SELECT * FROM user where userID=?', [id], function(err, result, fields){
            con.on('error', function(err){
                console.log('[MySQL ERROR]', err); 
            });
            if(result && result.length){
                no = result[0].userNo;
                console.log("로그2, 유저넘버: "+no);
                //유저넘버로 방에 참여중인지 여부검색
                con.query('SELECT * FROM room_has_user where userNo=?', [no], function(err, result, fields){
                    con.on('error', function(err){
                        console.log('[MySQL ERROR]', err); 
                    });
                    //방생성과 참가를 막음
                    if(result && result.length){
                        console.log("로그3, 참여중인 방 존재여부: true");
                        res.json('방이 이미 존재함');
                    }
                    //조건과 일치하는 방이있다면 참가 아니면 생성
                    else{
                        console.log("로그3, 참여중인 방 존재여부: false");
                        var sql = 'SELECT * FROM room where locationX=? && locationY=?';
                        var params = [x, y];
                        con.query(sql, params, function(err, result, fields){
                            con.on('error', function(err){
                                console.log('[MySQL ERROR]', err);
                            });
                            
                            //조건과 일치하는 방이있고 그 방들의 인원을 체크함
                            if(result && result.length){
                                console.log("로그4, 방이 존재하여 인원을 체크함 방 갯수: "+result.length);
                                for(var i = 0; i < result.length; i++){
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
                                                console.log("로그7, "+i+"번째 방에 참가함");
                                                res.json('방에 참가함');
                                            });
                                        }
                                    });
                                }
                            }
                            else{
                                console.log("로그5, 방이 존재하지 않아 방을 생성함");
                                var sql = 'INSERT INTO room (userNo, locationX, locationY) VALUES (?, ?, ?)';
                                var params = [no, x, y];
                                con.query(sql, params, function(err, result, fields){
                                    con.on('error', function(err){
                                        console.log('[MySQL ERROR]', err);
                                        res.json('방생성 실패: ', err);
                                    });
                                    res.json('방이 생성됨');

                                    insertId = result.insertId;
                                    console.log(insertId+'번 방 생성됨');

                                    var sql = 'INSERT INTO room_has_user (roomNo, userNo) VALUES (?, ?)';
                                    var params = [insertId, no];
                                    con.query(sql, params, function(err, result, fields){
                                        con.on('error', function(err){
                                            console.log('[MySQL ERROR]', err);
                                        });
                                    });
                                });
                            }
                        });
                    } 
                });
            }
            else
                res.json('조회할 수 없음');
        });
    })
    return route;
}