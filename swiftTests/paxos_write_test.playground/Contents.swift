import Foundation
import PlaygroundSupport

PlaygroundPage.current.needsIndefiniteExecution = true

let testString = UUID.init().uuidString

let headers = [
    "cache-control": "no-cache",
    "postman-token": "e5fb03cc-c5e6-27ed-7361-2deae241cf76"
]

let request = NSMutableURLRequest(url: NSURL(string: "http://localhost:8080/write/\(testString)")! as URL,
                                  cachePolicy: .useProtocolCachePolicy,
                                  timeoutInterval: 10.0)
request.httpMethod = "POST"
request.allHTTPHeaderFields = headers

let session = URLSession.shared
let dataTask = session.dataTask(with: request as URLRequest, completionHandler: { (data, response, error) -> Void in
    if (error != nil) {
        print(error)
    } else {
        let httpResponse = response as! HTTPURLResponse
        print(httpResponse)
        
        let statusCode = httpResponse.statusCode
        switch statusCode {
        case 200:
            print("POST sucessful")
        case 500:
            print("Internal server error on posting value")
            return
        default:
            print("error posting request")
        }
        
        let getRequest = NSMutableURLRequest(url: NSURL(string: "http://localhost:8080/read")! as URL,
                                             cachePolicy: .useProtocolCachePolicy,
                                             timeoutInterval: 10.0)
        getRequest.httpMethod = "GET"
        getRequest.allHTTPHeaderFields = headers
        
        sleep(5)
        let getDataTask = session.dataTask(with: getRequest as URLRequest, completionHandler: { (data, response, error) -> Void in
            if (error != nil) {
                print(error)
            } else {
                let httpResponse = response as! HTTPURLResponse
                let respStr = String(data: data!, encoding: .utf8)!
                let statusCode = httpResponse.statusCode
                switch statusCode {
                case 200:
                    if respStr == testString {
                        print("sucess, values are equal: \(respStr) & \(testString)")
                    } else {
                        print("error, some other value is set: \(respStr) & \(testString)")
                    }
                case 500:
                    print("internal server error on getting value")
                default:
                    print("error getting request")
                }
            }
        })
        getDataTask.resume()
    }
})

dataTask.resume()














