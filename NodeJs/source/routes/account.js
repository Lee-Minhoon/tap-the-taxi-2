module.exports = function(express, con){
    var route = express.Router();

    route.post('', (req, res, next)=>{
        var post_data = req.body;

        var id = post_data.id;
        
        con.query('SELECT * FROM user where userID=?', [id], function(err, result, fields){
            con.on('error', function(err){
                console.log('[MySQL ERROR]', err); 
            });
            if(result && result.length){
                con.query('SELECT * FROM account where userNo=?', [result[0].userNo], function(err, result, fields){
                    con.on('error', function(err){
                        console.log('[MySQL ERROR]', err); 
                    });

                    if(result && result.length){
                        res.end(JSON.stringify(result[0]));
                    }
                    else{
                        res.json('조회할 수 없음');
                    }
                });
            }
            else{
                res.json('조회할 수 없음');
            }
        });


    })
    
    return route;
}