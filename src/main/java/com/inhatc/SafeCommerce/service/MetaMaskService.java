package com.inhatc.SafeCommerce.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;
import org.web3j.crypto.Sign;

@Service
public class MetaMaskService {

    public boolean verifySignature(String account, String signature) {
        String message = "로그인을 위한 서명";

        try {
            String r = signature.substring(0, 66);
            String s = "0x" + signature.substring(66, 130);
            int v = Integer.parseInt(signature.substring(130, 132), 16);

            if (v < 27) {
                v += 27;
            }

            Sign.SignatureData signatureData = new Sign.SignatureData((byte) v, Numeric.hexStringToByteArray(r), Numeric.hexStringToByteArray(s));
            byte[] messageHash = Sign.getEthereumMessageHash(message.getBytes());
            String recoveredAddress = "0x" + Keys.getAddress(Sign.signedMessageToKey(messageHash, signatureData).toString(16));

            return account.equalsIgnoreCase(recoveredAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}