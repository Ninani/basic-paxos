
//: Playground - noun: a place where people can play

import UIKit


let dir = try? FileManager.default.url(for: .documentDirectory,
                                       in: .userDomainMask, appropriateFor: nil, create: true)

let outFileURL = dir!.appendingPathComponent("output").appendingPathExtension("html")


let outPath = Bundle.main.path(forResource: "output", ofType: "txt")!
var outputHTML = ""

outputHTML.append("<!DOCTYPE html>\n")
outputHTML.append("<html>\n")
outputHTML.append("<body>\n")

if let path = Bundle.main.path(forResource: "paxos", ofType: "txt") {
    do {
        let data = try String(contentsOfFile: path, encoding: .utf8)
        let myStrings = data.components(separatedBy: .newlines)
        
        for str in myStrings {
            
            let pat = "[*]*  \\d{4}[*]*"
            let regex = try! NSRegularExpression(pattern: pat, options: [])
            var port = ""
            
            do {
                let regex = try NSRegularExpression(pattern: "[*]*  \\d{4}[*]*", options: NSRegularExpression.Options.caseInsensitive)
                let matches = regex.matches(in: str, options: [], range: NSRange(location: 0, length: str.utf16.count))
                
                if let match = matches.first {
                    let range = match.range(at:0)
                    if let swiftRange = Range(range, in: str) {
                        let name = str[swiftRange]
                        port = "\(name)"
                    }
                }
            } catch {
                // regex was bad!
            }
            
            do {
                var color = "black"
                switch port {
                case "  8090":
                    color = "blue"
                case "  8091":
                    color = "green"
                case "  8092":
                    color = "red"
                default:
                    break
                }
                let outString = "<p><font color=\"\(color)\">\(str)</font></p>"
                outputHTML.append(outString + "\n")
            }
        }
        
    } catch {
        print(error)
    }
    outputHTML.append("</body>\n")
    outputHTML.append("</html>\n")
    try outputHTML.write(to: outFileURL, atomically: false, encoding: .utf8)
    print(outputHTML)
    
}

