package com.example.healthflow.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error details from request
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        StringBuilder errorMessage = new StringBuilder("An error occurred");

        if (status != null) {
            errorMessage.append(" (Status code: ").append(status).append(")");
        }

        if (message != null && !message.toString().isEmpty()) {
            errorMessage.append(": ").append(message);
        }

        if (exception != null) {
            errorMessage.append("\nException: ").append(exception.toString());
        }

        model.addAttribute("error", errorMessage.toString());
        return "error";
    }
}