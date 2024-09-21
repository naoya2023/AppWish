package com.example.appwish.model.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 表示名からProjectCategoryを取得する。
     * @param displayName 表示名
     * @return 対応するProjectCategory、見つからない場合はnull
     */
    @JsonCreator
    public static ProjectCategory fromDisplayName(String displayName) {
        for (ProjectCategory category : ProjectCategory.values()) {
            if (category.getDisplayName().equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 名前（列挙型の定数名）からProjectCategoryを取得する。
     * @param name 名前
     * @return 対応するProjectCategory
     * @throws IllegalArgumentException 対応するProjectCategoryが見つからない場合
     */
    public static ProjectCategory fromString(String name) {
        try {
            return ProjectCategory.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fromDisplayName(name);
        }
    }
}