
package com.keerthana.bank.controller;

import com.keerthana.bank.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @GetMapping("/otp")
    public String showOtpPage(HttpSession session, Model model) {

        // Only logged-in users can access the OTP page
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", session.getAttribute("user"));
        return "otp";
    }

    @PostMapping("/otp/generate")
    public String generateOtp(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        String otp = otpService.generateOtp();

        // Store OTP in the current session
        session.setAttribute("bankOtp", otp);

        // Reset previous verification status
        session.setAttribute("bankOtpVerified", false);

        // Demo only: OTP is displayed on the page
        redirectAttributes.addFlashAttribute("demoOtp", otp);
        redirectAttributes.addFlashAttribute(
                "message",
                "Demo OTP generated successfully."
        );

        return "redirect:/otp";
    }

    @PostMapping("/otp/verify")
    public String verifyOtp(
            @RequestParam String enteredOtp,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        String generatedOtp =
                (String) session.getAttribute("bankOtp");

        if (otpService.verifyOtp(enteredOtp, generatedOtp)) {

            // Keep OTP available for the transfer controller.
            // Mark it verified instead of removing it here.
            session.setAttribute("bankOtpVerified", true);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Demo OTP verified. Return to the transfer page."
            );

        } else {

            session.setAttribute("bankOtpVerified", false);

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Invalid OTP. Generate a new OTP and try again."
            );
        }

        return "redirect:/otp";
    }
}
