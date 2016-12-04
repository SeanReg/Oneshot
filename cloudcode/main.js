Parse.Cloud.define("PushUser", function(request, response) {
  Parse.Cloud.useMasterKey();
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", request.params.userId);

   query.first({
   success: function(user){
       var objectId = user.id;


        var query = new Parse.Query(Parse.Installation);
        query.equalTo("user", user);


        var payload = {
          alert: request.params.message
            // you can add other stuff here...
        };


        Parse.Push.send({
            data: payload,
            where: query
          }, {
            useMasterKey: true
          })
          .then(function() {
            response.success("Push Sent!");
          }, function(error) {
            response.error("Error while trying to send push " + error.message);
          });
       },

       error: function(error) {
           console.error(error);
           response.error("An error occured while lookup the users objectid");
       }

   });
  });

Parse.Cloud.define("CompleteGame", function(request, response) {
  var query = new Parse.Query("Games");
  query.equalTo("objectId", request.params.gameId);
  query.include("owner");

  query.first({
   success: function(game){
        completeGameIfDone(game)
        response.success("Game checked for experiation successfully");
       },

       error: function(error) {
           console.error(error);
           response.error("An error occured while lookup the users objectid");
       }

   });
});

Parse.Cloud.job("GameExpireCheck", function(request, response) {

  var games = new Parse.Query("Games");
  games.equalTo("completed", false);
  games.include("owner");
  games.find({
        success: function(results) {
            for (var i = 0; i < results.length; ++i) {
              completeGameIfDone(results[i])
            }
            response.success("Games completed");
        },
        error: function() {
            response.error("Job Failed");
        }
    });
});

function completeGameIfDone(game) {
  var nowDate = new Date();
  var dateCreated = game.createdAt;
  var limit = Number(game.get("timelimit"));
  if (Number(game.get("shotCount")) == Number(game.get("playerCount")) || nowDate.getTime() >= dateCreated.getTime() + limit) {//game.createdAt.getTime() + game.get("timelimit") >= (new Date()).getTime()) {
    game.set("completed", true);
    game.save();

    var query = new Parse.Query(Parse.Installation);
    query.equalTo("user", game.get("owner"));

    var payload = {
      alert: "Your game has ended! Choose a winner"
    };


    Parse.Push.send({
        data: payload,
        where: query
      }, {
        useMasterKey: true
      })
      .then(function() {
      }, function(error) {
    });

    console.log("Game completed: " + game.id);
  }
}