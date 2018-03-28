* Long queries can result from ineffective use of indexes; non-optimal schema design; poor query structure; system architecture issues; or insufficient RAM resulting in page faults and disk reads.

```javascript
//get server status
db.runCommand( { serverStatus: 1 } )
/*
set up profiling for performance optimization
*/
db.getProfilingLevel()
//verbose level as 0
db.setProfilingLevel(0)
//set the threshold to 20ms
db.setProfilingLevel(0,20)
db.system.profile.find().limit(10).sort( { ts : -1 } ).pretty()
//explain one query
db.products.explain().remove( { category: "apparel" }, { justOne: true } )
//append a comment in mongoose and mongodb
testModel.find().comment('hahahahha')
```

### get log
```javascript
db.adminCommand({getLog: "*" })
db.adminCommand({getLog: "global" })
```

### stub
A method stub or simply stub in software development is a piece of code used to stand in for some other programming functionality. The same as mock.

### What is a Storage Engine?
The storage engine is the component of the database that is responsible for managing how data is stored, both in memory and on disk.
