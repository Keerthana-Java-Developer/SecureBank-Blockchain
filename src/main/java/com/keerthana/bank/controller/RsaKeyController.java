
        package com.keerthana.bank.controller;

import com.keerthana.bank.security.RsaKeyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.KeyPair;

@Controller
@SessionAttributes("rsaKeyPair")
public class RsaKeyController {

    private final RsaKeyService rsaKeyService;

    public RsaKeyController(RsaKeyService rsaKeyService) {
        this.rsaKeyService = rsaKeyService;
    }

    @ModelAttribute("rsaKeyPair")
    public KeyPair createKeyPair() {
        return rsaKeyService.generateKeyPair();
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    @GetMapping("/rsa-keys")
    public String showKeys(
            @ModelAttribute("rsaKeyPair") KeyPair keyPair,
            HttpSession session,
            Model model) {

        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        // Display only the public key.
        model.addAttribute(
                "publicKey",
                rsaKeyService.getPublicKey(keyPair)
        );

        // Never send the private key to the webpage.
        return "rsa-keys";
    }

    @GetMapping("/encrypt")
    public String encryptionPage(HttpSession session) {

        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        return "encrypt";
    }

    @PostMapping("/encrypt")
    public String encryptMessage(
            @RequestParam String message,
            @ModelAttribute("rsaKeyPair") KeyPair keyPair,
            HttpSession session,
            Model model) {

        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("message", message);

        try {
            String encrypted =
                    rsaKeyService.encrypt(message, keyPair);

            model.addAttribute("encryptedMessage", encrypted);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "encrypt";
    }

    @PostMapping("/decrypt")
    public String decryptMessage(
            @RequestParam String encryptedMessage,
            @ModelAttribute("rsaKeyPair") KeyPair keyPair,
            HttpSession session,
            Model model) {

        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        model.addAttribute("encryptedMessage", encryptedMessage);

        try {
            String decrypted =
                    rsaKeyService.decrypt(encryptedMessage, keyPair);

            model.addAttribute("decryptedMessage", decrypted);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "encrypt";
    }
}
