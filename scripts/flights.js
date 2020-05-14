use flights;
db.createCollection("prices");
db.prices.createIndex({"ts":1, "origin": 1, "destination": 1, "departureTime": 1}, {"name": "updateIndex", "unique": true});
