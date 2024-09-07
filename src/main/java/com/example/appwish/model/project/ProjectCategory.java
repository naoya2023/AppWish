package com.example.appwish.model.project;

/**
 * プロジェクトのカテゴリを表す列挙型。
 */
public enum ProjectCategory {
    /**
     * Webアプリケーション
     */
    WEB_APPLICATION("Webアプリケーション"),

    /**
     * モバイルアプリ
     */
    MOBILE_APP("モバイルアプリ"),

    /**
     * デスクトップアプリケーション
     */
    DESKTOP_APPLICATION("デスクトップアプリケーション"),

    /**
     * ゲーム
     */
    GAME("ゲーム"),

    /**
     * AI/機械学習
     */
    AI_ML("AI/機械学習"),

    /**
     * IoT (Internet of Things)
     */
    IOT("IoT"),

    /**
     * その他
     */
    OTHER("その他");

    private final String displayName;

    ProjectCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * カテゴリの表示名を取得する。
     * @return カテゴリの表示名
     */
    public String getDisplayName() {
        return displayName;
    }
}