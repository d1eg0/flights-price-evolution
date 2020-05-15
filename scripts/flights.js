use admin
db.createUser(
   {
     user: "appuser",
     pwd: "apppass",
     roles:
       [
         { role: "readWrite", db: "flights" }
       ]
   }
);
use flights;
db.createCollection("prices");
db.prices.createIndex({"ts":1, "origin": 1, "destination": 1, "departureTime": 1}, {"name": "updateIndex", "unique": true});
