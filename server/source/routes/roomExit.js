module.exports = function(express, con){
    var route = express.Router();
    
    route.post('', (req, res, next)=>{
        var post_data = req.body;
        var id = post_data.id;
        let result = con.query('SELECT * FROM user where userID=?', [id]);
        no = result[0].userNo;
        con.query('DELETE FROM room_has_user where userNo=?', [no]);
        console.log("방 퇴장");
        result = con.query('SELECT * FROM room where userNo=?', [no]);
        if(result && result.length){
            console.log("방 삭제");
            con.query('DELETE FROM room where userNo=?', [no]);
        }
        res.json('방을 나갔습니다.');
    })
    
    return route;
}