module.exports = function(express, con){
    var route = express.Router();
    
    route.post('', (req, res, next)=>{
        var post_data = req.body;

        var id = post_data.id;
        var x = Math.round(post_data.x);
        var y = Math.round(post_data.y);
        
        var no;
        var insertId;
        
        let sql, params;
        
        console.log("로그1, 받아온 값: "+id, x, y);
        //세션검색으로 유저넘버 들고옴
        let result = con.query('SELECT * FROM user where userID=?', [id]);
        no = result[0].userNo;
        
        console.log("로그2, 유저넘버: "+no);
        //유저넘버로 방에 참여중인지 여부검색
        result = con.query('SELECT * FROM room_has_user where userNo=?', [no]);
        //방생성과 참가를 막음
        if(result && result.length){
            console.log("로그3, 참여중인 방 존재여부: true");
            res.json('방이 이미 존재합니다.');
        }
        //조건과 일치하는 방이있다면 참가 아니면 생성
        else{
            console.log("로그3, 참여중인 방 존재여부: false");
            sql = 'SELECT * FROM room where locationX=? && locationY=?';
            params = [x, y];
            result = con.query(sql, params);
            
            //조건과 일치하는 방이있고 그 방들의 인원을 체크함
            if(result && result.length){
                console.log("로그4, 방이 존재하여 인원을 체크함 방 갯수: "+result.length);
                for(var i = 0; i < result.length; i++){
                    console.log("로그5, 방 인원체크: "+i+"번째 방");
                    console.log("로그6, 방 번호: "+result[i].roomNo);
                    sql = 'SELECT * FROM room_has_user where roomNo=?';
                    params = [result[i].roomNo];
                    rows = con.query(sql, params);
                    //4명이하인 방이 있다면 참가함
                    console.log("로그7, "+result[i].roomNo+"번 방 인원: "+rows.length);
                    if(rows.length < 4){
                        sql = 'INSERT INTO room_has_user (roomNo, userNo) VALUES (?, ?)';
                        params = [result[i].roomNo, no];
                        con.query(sql, params);
                        console.log("로그8, "+i+"번째 방에 참가함");
                        res.json('방에 참가하였습니다.');
                        break;
                    }
                }
                result = con.query('SELECT * FROM room_has_user where userNo=?', [no]);
                if(result.length == 0){
                    console.log("로그8, 인원이 모두 차 방을 생성함");
                    sql = 'INSERT INTO room (userNo, locationX, locationY) VALUES (?, ?, ?)';
                    params = [no, x, y];
                    result = con.query(sql, params);
                    res.json('방이 생성되었습니다.');

                    insertId = result.insertId;
                    console.log(insertId+'번 방 생성됨');

                    sql = 'INSERT INTO room_has_user (roomNo, userNo) VALUES (?, ?)';
                    params = [insertId, no];
                    con.query(sql, params);
                }
            }else{
                console.log("로그5, 방이 존재하지 않아 방을 생성함");
                sql = 'INSERT INTO room (userNo, locationX, locationY) VALUES (?, ?, ?)';
                params = [no, x, y];
                result = con.query(sql, params);
                res.json('방이 생성되었습니다.');

                insertId = result.insertId;
                console.log(insertId+'번 방 생성됨');

                sql = 'INSERT INTO room_has_user (roomNo, userNo) VALUES (?, ?)';
                params = [insertId, no];
                con.query(sql, params);
            }
        }
    })
    return route;
}