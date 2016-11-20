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