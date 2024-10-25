package com.inhatc.SafeCommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;
import org.web3j.crypto.Sign;

@Service
public class MetaMaskService {

    private static final Logger logger = LoggerFactory.getLogger(MetaMaskService.class);

    public boolean verifySignature(String account, String signature) {
        String message = "로그인을 위한 서명"; // 동일한 메시지 사용

        logger.info("Verifying signature for account: {}", account);
        logger.info("Signature received: {}", signature);

        try {
            String r = signature.substring(0, 66);
            String s = "0x" + signature.substring(66, 130);
            int v = Integer.parseInt(signature.substring(130, 132), 16);

            // v 값을 27로 보정
            if (v < 27) {
                v += 27;
            }

            Sign.SignatureData signatureData = new Sign.SignatureData((byte) v, Numeric.hexStringToByteArray(r), Numeric.hexStringToByteArray(s));
            byte[] messageHash = Sign.getEthereumMessageHash(message.getBytes());
            String recoveredAddress = "0x" + Keys.getAddress(Sign.signedMessageToKey(messageHash, signatureData).toString(16));

            logger.info("Recovered address: {}", recoveredAddress);  // 복구된 주소 로그
            boolean isValid = account.equalsIgnoreCase(recoveredAddress);
            logger.info("Signature verification result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error during signature verification", e);
            return false;
        }
    }
}