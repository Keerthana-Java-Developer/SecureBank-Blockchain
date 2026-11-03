
package com.keerthana.bank.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class OtpService {

    private final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        int number = random.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    public boolean verifyOtp(String enteredOtp, String generatedOtp) {
        return generatedOtp != null
                && enteredOtp != null
                && generatedOtp.equals(enteredOtp);
    }
}
