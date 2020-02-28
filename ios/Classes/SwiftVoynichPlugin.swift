import Flutter
import UIKit
import CryptoSwift

public class SwiftVoynichPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "voynich", binaryMessenger: registrar.messenger())
        let instance = SwiftVoynichPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "encryptSymmetric":
            guard let args = call.arguments as? Dictionary<String, Any> else {
                result("iOS could not recognize flutter arguments in method: (sendParams)")
                return
            }
            let inputPath = args["inputPath"] as! String
            let password  = args["password"]as! String
            let outputPath  = args["outputPath"]as! String
            print(inputPath)
            print(password)
            print(outputPath)
            do {
                let plainData = try Data(contentsOf: URL(fileURLWithPath: inputPath))
                
                let keyUInt8Array: [UInt8] = stringToBytes(password)!
                let iv16BytesArray = stringToBytes(generateRandomIV()!)!
                let saltByteArray = stringToBytes(generateSalt()!)!
                
                let aes = try AES(key: keyUInt8Array, blockMode: CBC(iv: iv16BytesArray) , padding: .pkcs7)
                
                var encrypted = try aes.encrypt(plainData.bytes)
                
                var encryptedData: Data? = Data(capacity: iv16BytesArray.count + saltByteArray.count + encrypted.count)
                encryptedData?.append(contentsOf: iv16BytesArray)
                encryptedData?.append(contentsOf: saltByteArray)
                encryptedData?.append(UnsafeBufferPointer(start: &encrypted, count: encrypted.count))
                
                try encryptedData!.write(to: URL(fileURLWithPath: outputPath))
            } catch let error {
                result(FlutterError(
                    code: error.localizedDescription, message: "Error encrypting", details: nil
                ))
            }
            
            result(nil)
        case "decryptSymmetric":
            guard let args = call.arguments as? Dictionary<String, Any> else {
                result("iOS could not recognize flutter arguments in method: (sendParams)")
                return
            }
            let inputPath = args["inputPath"] as! String
            let password  = args["password"]as! String
            let outputPath  = args["outputPath"]as! String
            print(inputPath)
            print(password)
            print(outputPath)
            do {
                let encryptedData = try Data(contentsOf: URL(fileURLWithPath: inputPath))
                
                let keyUInt8Array: [UInt8] = self.stringToBytes(password)!
                let iv16BytesArray = Array(encryptedData[0..<16])
                
                let encryptedDataWithoutIV = encryptedData.dropFirst(48) // 16Bytes of IV and 32Bytes of salt
                
                let aes = try AES(key: keyUInt8Array, blockMode: CBC(iv: iv16BytesArray) , padding: .pkcs7)
                var decryptedData: Data? = nil
                var decrypted = try aes.decrypt(encryptedDataWithoutIV.bytes)
                decryptedData = Data(buffer: UnsafeBufferPointer(start: &decrypted, count: decrypted.count))
                
                try decryptedData!.write(to: URL(fileURLWithPath: outputPath))
            } catch let error {
                result(FlutterError(
                    code: error.localizedDescription, message: "Error decrypting", details: nil
                ))
            }
            result(nil)
        default:
            result("iOS " + UIDevice.current.systemVersion)
        }
    }
    
    func generateRandomKey() -> String? {
        return generateRandomBytes(32)
    }
    
    func generateRandomIV() -> String? {
        return generateRandomBytes(16)
    }
    
    func generateSalt() -> String? {
        return generateRandomBytes(32)
    }
    
    func generateRandomBytes(_ count: Int) -> String? {
        var data = Data(count: count)
        let result = data.withUnsafeMutableBytes {
            SecRandomCopyBytes(kSecRandomDefault, count, $0.baseAddress!)
        }
        if result == errSecSuccess {
            return data.toHexString()
        } else {
            print("Error generating random bytes")
            return nil
        }
    }
    
    func stringToBytes(_ string: String) -> [UInt8]? {
        let length = string.count
        if length & 1 != 0 {
            return nil
        }
        var bytes = [UInt8]()
        bytes.reserveCapacity(length/2)
        var index = string.startIndex
        for _ in 0..<length/2 {
            let nextIndex = string.index(index, offsetBy: 2)
            if let b = UInt8(string[index..<nextIndex], radix: 16) {
                bytes.append(b)
            } else {
                return nil
            }
            index = nextIndex
        }
        return bytes
    }
}
