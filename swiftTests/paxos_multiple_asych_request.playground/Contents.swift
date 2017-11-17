import Foundation
import PlaygroundSupport

PlaygroundPage.current.needsIndefiniteExecution = true

//Wykonujemy
//Test pokazujacy livelocka - podatnosc podstawowego alg paxosa - basic paxosa

    let shouldSendRequestsOnlyToOneServer = true
    let numberOfRounds = 1
    let numberOfRequestsInEachRound = 2

for p in 0...numberOfRounds-1 {
    let headers = [
        "cache-control": "no-cache",
        "postman-token": "e5fb03cc-c5e6-27ed-7361-2deae241cf76"
    ]
    
    let testString = UUID.init().uuidString
    
    for i in 0...numberOfRequestsInEachRound-1 {
        let port = 8090
        
        var portString = ""
        if shouldSendRequestsOnlyToOneServer {
            portString = "\(port)"
        } else {
            portString = "\(port+i%2)"
        }
        
        let request = NSMutableURLRequest(url: NSURL(string: "http://localhost:\(portString)/write/\(i)")! as URL,
                                          cachePolicy: .useProtocolCachePolicy,
                                          timeoutInterval: 10.0)
        request.httpMethod = "POST"
        request.allHTTPHeaderFields = headers
        
        let session = URLSession.shared
        let dataTask = session.dataTask(with: request as URLRequest, completionHandler: { (data, response, error) -> Void in
            if (error != nil) {
                print(error)
            } else {
                let httpResponse = response as? HTTPURLResponse
                print(httpResponse)
            }
            
        })
        
        dataTask.resume()
    }
    
    let getRequest = NSMutableURLRequest(url: NSURL(string: "http://localhost:8080/read")! as URL,
                                         cachePolicy: .useProtocolCachePolicy,
                                         timeoutInterval: 10.0)
    getRequest.httpMethod = "GET"
    getRequest.allHTTPHeaderFields = headers
    
    sleep(6)
    let session = URLSession.shared
    let getDataTask = session.dataTask(with: getRequest as URLRequest, completionHandler: { (data, response, error) -> Void in
        if (error != nil) {
            print(error)
        } else {
            let httpResponse = response as? HTTPURLResponse
            let respStr = String(data: data!, encoding: .utf8)!
            print(httpResponse)
            print("==== VALUE = \(respStr)")
        }
    })
    getDataTask.resume()
    sleep(6)
}










