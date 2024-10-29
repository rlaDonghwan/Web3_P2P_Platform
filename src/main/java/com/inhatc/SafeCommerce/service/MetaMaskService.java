//package com.inhatc.SafeCommerce.service;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.web3j.crypto.Keys;
//import org.web3j.crypto.Sign;
//import org.web3j.utils.Numeric;
//
//@Service
//public class MetaMaskService {
//
//    private static final Logger logger = LoggerFactory.getLogger(MetaMaskService.class);
//
//    public boolean verifySignature(String account, String signature, Integer nonce) {
//        // Nonce 포함된 메시지 생성
//        String message = String.format("Signing a message to login: %s", nonce);
//        logger.info("Message to verify: {}", message);
//
//        logger.info("Verifying signature for account: {}", account);
//        logger.info("Signature received: {}", signature);
//
//        try {
//            // 서명에서 r, s, v 추출
//            if (signature.length() != 132) {
//                logger.error("Invalid signature length: {}", signature.length());
//                return false; // 서명 길이가 유효하지 않음
//            }
//
//            String r = signature.substring(0, 66);
//            String s = "0x" + signature.substring(66, 130);
//            int v = Integer.parseInt(signature.substring(130, 132), 16);
//
//            if (v < 27) {
//                v += 27;
//            }
//
//            // 서명 데이터 객체 생성
//            Sign.SignatureData signatureData = new Sign.SignatureData((byte) v, Numeric.hexStringToByteArray(r), Numeric.hexStringToByteArray(s));
//
//            // 메시지 해시 계산 (Ethereum 표준에 맞게 접두사 추가)
//            String prefix = "\u0019Ethereum Signed Message:\n" + message.length();
//            byte[] messageHash = Sign.getEthereumMessageHash((prefix + message).getBytes());
//
//            // 서명된 메시지에서 주소 복구
//            String recoveredAddress = "0x" + Keys.getAddress(Sign.signedMessageHashToKey(messageHash, signatureData).toString(16));
//
//            logger.info("Recovered address: {}", recoveredAddress);
//            logger.info("Original account: {}", account);
//
//            // 주소 비교
//            boolean isValid = account.equalsIgnoreCase(recoveredAddress);
//            logger.info("Signature verification result: {}", isValid);
//
//            // 검증 결과 출력
//            if (!isValid) {
//                logger.warn("Signature verification failed for account: {}", account);
//                logger.warn("Mismatch between recovered address {} and original account {}.", recoveredAddress, account);
//            }
//
//            return isValid;
//        } catch (Exception e) {
//            logger.error("Error during signature verification", e);
//            return false;
//        }
//    }
//}