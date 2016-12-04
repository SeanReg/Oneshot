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

Parse.Cloud.job("GameExpireCheck", function(request, response) {

  var games = new Parse.Query("Games");
  games.equalTo("completed", false);
  games.include("owner");
  games.find({
        success: function(results) {
            var nowDate = new Date();
            for (var i = 0; i < results.length; ++i) {
              var dateCreated = results[i].createdAt;
              var limit = Number(results[i].get("timelimit"));
              if (nowDate.getTime() >= dateCreated.getTime() + limit) {//results[i].createdAt.getTime() + results[i].get("timelimit") >= (new Date()).getTime()) {
                results[i].set("completed", true);
                results[i].save();

                var query = new Parse.Query(Parse.Installation);
                query.equalTo("user", results[i].get("owner"));

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

                console.log("Game completed: " + results[i].id);
              }
            }
            response.success("Games completed");
        },
        error: function() {
            response.error("Job Failed");
        }
    });
});