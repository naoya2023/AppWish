
package com.example.appwish.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "passwordForget";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
		return email;
        // パスワードリセットのロジックを実装
    }

    // その他の認証関連のメソッド（ログイン、ログアウトなど）
}
