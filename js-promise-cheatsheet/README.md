# PromiseCheatSheet
Notes for Kriskowal's Q promise

* what is a promise
  ```javascript 
      promiseForResult.then(onFulfilled, onRejected);
  ```       

    * Only one of onFulfilled or onRejected will be called.
    * onFulfilled will be called with a single fulfillment value (⇔     return value).
    * onRejected will be called with a single rejection reason (⇔ thrown exception).
    * If the promise is already settled, the handlers will still be called once you attach them.
    * The handlers will always be called asynchronously.

### characteristics 
* a better control flow
* chainable

	  ```javascript
    	one().
        	then(two).
            then(three).
            then(four)
    ```
* better error handling
	* this of this like **try/catch**
    ```javascript
    	//previous
        one(function(err, result1) {
        	if (err) {
            	//blah
            }
        	two(result, function(err, result2) {
            	if (err) {
                	//blah
                }
            }
        }
        //now
        one().
            then(two, errorHandler1).
            catch(errorHandler2)
        //or 
        one().
            then(two).
            catch(combinedErrorHandler).
            finally().
            done()
    ```

* How to create a promise
    ```javascript
    loadQLibrary();
    function one() {
        var deferred = Q.defer();
        doSomething(err, result) {
            if (err) {
                deferred.reject(err);
            } else {
                deferred.resolve(result);
            }
        }
        return deferred.promise;
    }

    one().
        then(successHandler).
        catch(errorHandler).
        done() //end the chain, uncaught error will explode here
    ```
    * specific for node
    	* [API reference in Q](https://github.com/kriskowal/q/wiki/API-Reference#qnbindnodemethod-thisarg-args)
 
* composable
	```javascript
    	//parallel
    	Q.all().spread()
        //serial
        one().
        	then().
            then().
            catch().
            finally()
	```
* pitfall
```javascript
	one().
	   then(successHandler, errorHandler).
	   then(function(){
	   	//If you return a value in a handler(neither success or error), the returned promise will get fulfilled.
	   	//if errorHanndler return a value, the value is available in next success handler
	   })
```
```javascript
	 function doB() {
	   var startTime = new Date();
	   var endTime = new Date();
	   console.log('t2: consumed' , endTime - startTime, 'ms');
	 }
	 var startTime = new Date();
	   var promise = doA().then(doB).catch().done();
	   promise.then(function() {
	   	var endTime = new Date();
	   	console.log('t1: consumed' , endTime - startTime, 'ms');
	   })
```

the value of t2 can be greater than t1
The Golden Rule of done vs. then usage is: either **return your promise to someone else**, or if the chain ends with you, **call done** to terminate it. Terminating with catch is not sufficient because the catch handler may itself throw an error.

#### Resources
- [promises-promises](http://www.slideshare.net/domenicdenicola/promises-promises)
- [callbacks-promises-and-coroutines](http://www.slideshare.net/domenicdenicola/callbacks-promises-and-coroutines-oh-my-the-evolution-of-asynchronicity-in-javascript)
